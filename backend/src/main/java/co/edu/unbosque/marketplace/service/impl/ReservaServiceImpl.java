package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmadaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.model.entity.Cotizacion;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Notificacion;
import co.edu.unbosque.marketplace.model.entity.Pago;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.HuespedRepository;
import co.edu.unbosque.marketplace.repository.NotificacionRepository;
import co.edu.unbosque.marketplace.repository.PagoRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import co.edu.unbosque.marketplace.service.PublicacionService;
import co.edu.unbosque.marketplace.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final AnfitrionRepository anfitrionRepository;
    private final PublicacionService publicacionService;
    private final NotificacionRepository notificacionRepository;
    private final HuespedRepository huespedRepository;
    private final PagoRepository pagoRepository;

    public ReservaServiceImpl(ReservaRepository reservaRepository, AnfitrionRepository anfitrionRepository,
                              PublicacionService publicacionService, NotificacionRepository notificacionRepository,
                              HuespedRepository huespedRepository, PagoRepository pagoRepository) {
        this.reservaRepository = reservaRepository;
        this.anfitrionRepository = anfitrionRepository;
        this.publicacionService = publicacionService;
        this.notificacionRepository = notificacionRepository;
        this.huespedRepository = huespedRepository;
        this.pagoRepository = pagoRepository;
    }

    /**
     * READ_COMMITTED porque MySQL usa REPEATABLE READ por defecto: con ese nivel, después de esperar el lock de la
     * publicación la transacción seguiría viendo los períodos y las reservas de antes de esperar, y no lo que dejó la
     * reserva que tenía el lock (MA-103).
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaConfirmadaDTO confirmar(ReservaConfirmarRequestDTO dto) {
        exigirFechasValidas(dto.getFechaLlegada(), dto.getFechaSalida());
        Huesped huesped = huespedRepository.findById(dto.getIdHuesped())
                .filter(h -> h.getUsuario().estaActiva() && !Boolean.TRUE.equals(h.getUsuario().getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo un huésped con la cuenta activa puede reservar"));

        // MA-103 escenarios 1 y 2: si la misma petición llega otra vez (doble clic, reintento), se devuelve la reserva
        // ya hecha sin crear otra ni cobrar de nuevo
        Optional<Reserva> repetida = reservaRepository.findByClaveIdempotencia(dto.getClaveIdempotencia());
        if (repetida.isPresent()) {
            return reservaYaHecha(repetida.get(), huesped);
        }

        // MA-103 escenario 3: solo una reserva de la publicación avanza a la vez; las demás esperan aquí. Al pasar se
        // vuelve a buscar la clave, porque si el reintento llegó al mismo tiempo que la original, la original pudo
        // terminar mientras este esperaba
        publicacionService.esperarTurnoParaReservar(dto.getIdPublicacion());
        repetida = reservaRepository.findByClaveIdempotencia(dto.getClaveIdempotencia());
        if (repetida.isPresent()) {
            return reservaYaHecha(repetida.get(), huesped);
        }

        // Escenario 2: si el período ya no está disponible se rechaza aquí, antes de procesar el pago
        Publicacion publicacion = publicacionService.obtenerParaReservar(dto.getIdPublicacion(),
                dto.getFechaLlegada(), dto.getFechaSalida());
        if (publicacion.getCapacidad() != null && dto.getNumHuespedes() > publicacion.getCapacidad()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El alojamiento admite máximo " + publicacion.getCapacidad() + " huéspedes");
        }
        Cotizacion cotizacion = cotizar(publicacion, dto.getFechaLlegada(), dto.getFechaSalida());

        // Escenario 3: si el pago es rechazado no se crea la reserva ni se bloquea el período
        if (!pagoSimuladoAprobado(dto.getNumeroTarjeta())) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "El pago fue rechazado: la reserva no se creó. Intenta con otra tarjeta");
        }

        // Escenario 1: se crea la reserva, se bloquea el período y se registra el pago y la confirmación
        boolean conAprobacion = publicacion.getModoAceptacion() == ModoAceptacion.CON_APROBACION;
        Reserva reserva = new Reserva();
        reserva.setEstado(conAprobacion ? EstadoReserva.PENDIENTE : EstadoReserva.CONFIRMADA);
        reserva.setFechaLlegada(dto.getFechaLlegada());
        reserva.setFechaSalida(dto.getFechaSalida());
        reserva.setNumHuespedes(dto.getNumHuespedes());
        reserva.setClaveIdempotencia(dto.getClaveIdempotencia());
        reserva.setHuesped(huesped);
        reserva.setPublicacion(publicacion);
        reserva.setCotizacion(cotizacion);
        reservaRepository.save(reserva);
        if (!conAprobacion) {
            publicacionService.bloquearPeriodoReservado(publicacion.getIdPublicacion(),
                    dto.getFechaLlegada(), dto.getFechaSalida());
        }
        Pago pago = registrarPago(reserva, huesped);

        String fechas = " del " + reserva.getFechaLlegada() + " al " + reserva.getFechaSalida();
        if (conAprobacion) {
            notificar(huesped.getUsuario(), "RESERVA_PENDIENTE", "Tu pago fue aprobado y tu reserva en "
                    + publicacion.getTitulo() + fechas + " espera la aprobación del anfitrión");
            notificar(publicacion.getAnfitrion().getUsuario(), "RESERVA_POR_APROBAR",
                    "Tienes una reserva por aprobar en " + publicacion.getTitulo() + fechas);
        } else {
            notificar(huesped.getUsuario(), "RESERVA_CONFIRMADA",
                    "Tu reserva en " + publicacion.getTitulo() + fechas + " fue confirmada");
            notificar(publicacion.getAnfitrion().getUsuario(), "NUEVA_RESERVA",
                    "Se confirmó una reserva en " + publicacion.getTitulo() + fechas);
        }
        return toConfirmadaDTO(reserva, pago, conAprobacion
                ? "El pago fue aprobado y la reserva quedó en espera de que el anfitrión la acepte"
                : "El pago fue aprobado y la reserva quedó confirmada");
    }

    private void exigirFechasValidas(LocalDate llegada, LocalDate salida) {
        if (!salida.isAfter(llegada)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de salida debe ser posterior a la fecha de llegada");
        }
        if (llegada.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de llegada no puede estar en el pasado");
        }
    }

    private ReservaConfirmadaDTO reservaYaHecha(Reserva reserva, Huesped huesped) {
        if (!reserva.getHuesped().getIdUsuario().equals(huesped.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La clave de idempotencia ya fue usada");
        }
        Pago pago = pagoRepository.findFirstByReserva_IdReservaAndTipoMovimiento(reserva.getIdReserva(), Pago.TIPO_PAGO)
                .orElse(null);
        return toConfirmadaDTO(reserva, pago, "Esta reserva ya se había procesado, no se cobró de nuevo");
    }

    /** Sin servicios adicionales, descuentos ni tarifa de servicio por ahora: noches por precio más la limpieza. */
    private Cotizacion cotizar(Publicacion publicacion, LocalDate llegada, LocalDate salida) {
        int noches = (int) ChronoUnit.DAYS.between(llegada, salida);
        double precioNoche = publicacion.getPrecioPorNoche() == null ? 0 : publicacion.getPrecioPorNoche();
        double limpieza = publicacion.getTarifaLimpieza() == null ? 0 : publicacion.getTarifaLimpieza();
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setNoches(noches);
        cotizacion.setPrecioBase(precioNoche * noches);
        cotizacion.setTarifaLimpieza(limpieza);
        cotizacion.setTotal(cotizacion.getPrecioBase() + limpieza);
        if (cotizacion.getTotal() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La publicación no tiene un precio para cobrar");
        }
        return cotizacion;
    }

    /** Pago simulado: se rechazan las tarjetas que terminan en 0002 (como la tarjeta de prueba de Stripe). */
    private boolean pagoSimuladoAprobado(String numeroTarjeta) {
        return !numeroTarjeta.endsWith("0002");
    }

    private Pago registrarPago(Reserva reserva, Huesped huesped) {
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setTipoMovimiento(Pago.TIPO_PAGO);
        pago.setValor(reserva.getCotizacion().getTotal());
        pago.setEmisor("HUESPED-" + huesped.getIdUsuario());
        pago.setReceptor("MARKETPLACE");
        pago.setIdTransaccion("SIM-" + UUID.randomUUID());
        pago.setEstado(Pago.APROBADO);
        pagoRepository.save(pago);
        return pago;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaPendienteDTO> listarPendientes(Integer idAnfitrion) {
        exigirAnfitrionHabilitado(idAnfitrion);
        return reservaRepository
                .findByPublicacion_Anfitrion_IdUsuarioAndEstadoOrderByFechaLlegadaAsc(idAnfitrion, EstadoReserva.PENDIENTE)
                .stream()
                .map(this::toPendienteDTO)
                .toList();
    }

    /** READ_COMMITTED por lo mismo que confirmar: al bloquear el período tiene que ver lo que dejaron las otras reservas. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MensajeResponseDTO aceptar(Integer idReserva, Integer idAnfitrion) {
        Reserva reserva = obtenerPendienteDelAnfitrion(idReserva, idAnfitrion);
        publicacionService.bloquearPeriodoReservado(reserva.getPublicacion().getIdPublicacion(),
                reserva.getFechaLlegada(), reserva.getFechaSalida());
        reserva.confirmar();
        reservaRepository.save(reserva);
        notificarAlHuesped(reserva, "RESERVA_CONFIRMADA", "confirmada");
        return mensaje("La reserva fue confirmada y se notificó al huésped");
    }

    @Override
    public MensajeResponseDTO rechazar(Integer idReserva, Integer idAnfitrion) {
        Reserva reserva = obtenerPendienteDelAnfitrion(idReserva, idAnfitrion);
        reserva.rechazar();
        reservaRepository.save(reserva);
        notificarAlHuesped(reserva, "RESERVA_RECHAZADA", "rechazada");
        return mensaje("La reserva fue rechazada y se notificó al huésped");
    }

    private Reserva obtenerPendienteDelAnfitrion(Integer idReserva, Integer idAnfitrion) {
        exigirAnfitrionHabilitado(idAnfitrion);
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe"));
        if (!reserva.getPublicacion().getAnfitrion().getIdUsuario().equals(idAnfitrion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el anfitrión dueño del alojamiento puede decidir la reserva");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La reserva ya no está pendiente, su estado es " + reserva.getEstado());
        }
        return reserva;
    }

    private void exigirAnfitrionHabilitado(Integer idAnfitrion) {
        anfitrionRepository.findById(idAnfitrion)
                .filter(a -> a.getUsuario().estaActiva() && !Boolean.TRUE.equals(a.getUsuario().getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo un anfitrión puede gestionar sus reservas"));
    }

    private void notificarAlHuesped(Reserva reserva, String tipo, String resultado) {
        notificar(reserva.getHuesped().getUsuario(), tipo, "Tu reserva en " + reserva.getPublicacion().getTitulo()
                + " del " + reserva.getFechaLlegada() + " al " + reserva.getFechaSalida() + " fue " + resultado);
    }

    private void notificar(Usuario usuario, String tipo, String mensaje) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTipo(tipo);
        notificacion.setMensaje(mensaje);
        notificacionRepository.save(notificacion);
    }

    private ReservaConfirmadaDTO toConfirmadaDTO(Reserva reserva, Pago pago, String mensaje) {
        ReservaConfirmadaDTO dto = new ReservaConfirmadaDTO();
        dto.setIdReserva(reserva.getIdReserva());
        dto.setEstado(reserva.getEstado());
        dto.setFechaLlegada(reserva.getFechaLlegada());
        dto.setFechaSalida(reserva.getFechaSalida());
        dto.setNumHuespedes(reserva.getNumHuespedes());
        dto.setIdPublicacion(reserva.getPublicacion().getIdPublicacion());
        dto.setTituloPublicacion(reserva.getPublicacion().getTitulo());
        dto.setNoches(reserva.getCotizacion().getNoches());
        dto.setPrecioBase(reserva.getCotizacion().getPrecioBase());
        dto.setTarifaLimpieza(reserva.getCotizacion().getTarifaLimpieza());
        dto.setTotal(reserva.getCotizacion().getTotal());
        dto.setIdTransaccion(pago == null ? null : pago.getIdTransaccion());
        dto.setMensaje(mensaje);
        return dto;
    }

    private MensajeResponseDTO mensaje(String texto) {
        MensajeResponseDTO dto = new MensajeResponseDTO();
        dto.setMensaje(texto);
        return dto;
    }

    private ReservaPendienteDTO toPendienteDTO(Reserva reserva) {
        ReservaPendienteDTO dto = new ReservaPendienteDTO();
        dto.setIdReserva(reserva.getIdReserva());
        dto.setEstado(reserva.getEstado());
        dto.setFechaLlegada(reserva.getFechaLlegada());
        dto.setFechaSalida(reserva.getFechaSalida());
        dto.setNumHuespedes(reserva.getNumHuespedes());
        dto.setIdPublicacion(reserva.getPublicacion().getIdPublicacion());
        dto.setTituloPublicacion(reserva.getPublicacion().getTitulo());
        dto.setIdHuesped(reserva.getHuesped().getIdUsuario());
        dto.setNombreHuesped(reserva.getHuesped().getUsuario().getNombreCompleto());
        return dto;
    }
}
