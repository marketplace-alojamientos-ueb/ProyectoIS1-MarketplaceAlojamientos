package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCanceladaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarAnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.SimulacionCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmadaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaListadoDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.model.dto.SesionStripeDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Cancelacion;
import co.edu.unbosque.marketplace.model.entity.Cotizacion;
import co.edu.unbosque.marketplace.model.entity.EventoAuditoria;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Notificacion;
import co.edu.unbosque.marketplace.model.entity.Pago;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaCancelacion;
import co.edu.unbosque.marketplace.model.enums.EstadoDevolucion;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.CancelacionRepository;
import co.edu.unbosque.marketplace.repository.EventoAuditoriaRepository;
import co.edu.unbosque.marketplace.repository.HuespedRepository;
import co.edu.unbosque.marketplace.repository.NotificacionRepository;
import co.edu.unbosque.marketplace.repository.PagoRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import co.edu.unbosque.marketplace.service.PublicacionService;
import co.edu.unbosque.marketplace.service.ReservaService;
import co.edu.unbosque.marketplace.service.StripeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final CancelacionRepository cancelacionRepository;
    private final EventoAuditoriaRepository eventoAuditoriaRepository;
    private final StripeService stripeService;
    private final String urlFront;

    /** Nombres de los datos de la reserva que se guardan en la sesión de Stripe. */
    private static final String META_HUESPED = "idHuesped";
    private static final String META_PUBLICACION = "idPublicacion";
    private static final String META_LLEGADA = "fechaLlegada";
    private static final String META_SALIDA = "fechaSalida";
    private static final String META_HUESPEDES = "numHuespedes";

    public ReservaServiceImpl(ReservaRepository reservaRepository, AnfitrionRepository anfitrionRepository,
                              PublicacionService publicacionService, NotificacionRepository notificacionRepository,
                              HuespedRepository huespedRepository, PagoRepository pagoRepository,
                              CancelacionRepository cancelacionRepository,
                              EventoAuditoriaRepository eventoAuditoriaRepository, StripeService stripeService,
                              @Value("${app.front.url:http://localhost:4200}") String urlFront) {
        this.reservaRepository = reservaRepository;
        this.anfitrionRepository = anfitrionRepository;
        this.publicacionService = publicacionService;
        this.notificacionRepository = notificacionRepository;
        this.huespedRepository = huespedRepository;
        this.pagoRepository = pagoRepository;
        this.cancelacionRepository = cancelacionRepository;
        this.eventoAuditoriaRepository = eventoAuditoriaRepository;
        this.stripeService = stripeService;
        this.urlFront = urlFront;
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
        exigirCapacidad(publicacion, dto.getNumHuespedes());
        Cotizacion cotizacion = cotizar(publicacion, dto.getFechaLlegada(), dto.getFechaSalida());

        // Escenario 3: si el pago es rechazado no se crea la reserva ni se bloquea el período
        if (!pagoSimuladoAprobado(dto.getNumeroTarjeta())) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "El pago fue rechazado: la reserva no se creó. Intenta con otra tarjeta");
        }

        // Escenario 1: se crea la reserva, se bloquea el período y se registra el pago y la confirmación
        return crearReservaPagada(dto, huesped, publicacion, cotizacion, "SIM-" + UUID.randomUUID());
    }

    /**
     * Stripe paso 1: revisa lo mismo que confirmar (huésped, fechas, disponibilidad, capacidad y precio) y crea la
     * sesión de pago en Stripe con el total de la cotización. Todavía no crea la reserva ni bloquea el período: eso
     * pasa en confirmarPagoStripe, cuando Stripe confirma que se pagó.
     */
    @Override
    public ReservaCheckoutDTO iniciarPagoStripe(ReservaCheckoutRequestDTO dto) {
        exigirFechasValidas(dto.getFechaLlegada(), dto.getFechaSalida());
        Huesped huesped = huespedActivo(dto.getIdHuesped(), "Solo un huésped con la cuenta activa puede reservar");
        Publicacion publicacion = publicacionService.obtenerParaReservar(dto.getIdPublicacion(),
                dto.getFechaLlegada(), dto.getFechaSalida());
        exigirCapacidad(publicacion, dto.getNumHuespedes());
        Cotizacion cotizacion = cotizar(publicacion, dto.getFechaLlegada(), dto.getFechaSalida());

        Map<String, String> datosDeLaReserva = new HashMap<>();
        datosDeLaReserva.put(META_HUESPED, String.valueOf(huesped.getIdUsuario()));
        datosDeLaReserva.put(META_PUBLICACION, String.valueOf(publicacion.getIdPublicacion()));
        datosDeLaReserva.put(META_LLEGADA, dto.getFechaLlegada().toString());
        datosDeLaReserva.put(META_SALIDA, dto.getFechaSalida().toString());
        datosDeLaReserva.put(META_HUESPEDES, String.valueOf(dto.getNumHuespedes()));

        String detalle = cotizacion.getNoches() + (cotizacion.getNoches() == 1 ? " noche" : " noches") + ", del "
                + dto.getFechaLlegada() + " al " + dto.getFechaSalida() + ", " + dto.getNumHuespedes()
                + (dto.getNumHuespedes() == 1 ? " huésped" : " huéspedes");
        SesionStripeDTO sesion = stripeService.crearSesionDePago(cotizacion.getTotal(),
                "Reserva en " + publicacion.getTitulo(), detalle, huesped.getUsuario().getCorreo(),
                urlFront + "/reservas/pago-exitoso?session_id={CHECKOUT_SESSION_ID}",
                urlFront + "/publicaciones/" + publicacion.getIdPublicacion() + "?pago=cancelado",
                datosDeLaReserva);

        ReservaCheckoutDTO respuesta = new ReservaCheckoutDTO();
        respuesta.setIdSesion(sesion.getIdSesion());
        respuesta.setUrlPago(sesion.getUrlPago());
        respuesta.setIdPublicacion(publicacion.getIdPublicacion());
        respuesta.setTituloPublicacion(publicacion.getTitulo());
        respuesta.setNoches(cotizacion.getNoches());
        respuesta.setPrecioBase(cotizacion.getPrecioBase());
        respuesta.setTarifaLimpieza(cotizacion.getTarifaLimpieza());
        respuesta.setTotal(cotizacion.getTotal());
        return respuesta;
    }

    /**
     * Stripe paso 2: le pregunta a Stripe si la sesión quedó pagada y, si sí, crea la reserva con los datos que se
     * guardaron en la sesión. El id de la sesión es la clave de idempotencia: si el front llama dos veces (recarga la
     * página de éxito) se devuelve la misma reserva. Si mientras el huésped pagaba se perdió la disponibilidad o
     * cambió el precio, se le devuelve el pago completo en Stripe y no se crea la reserva.
     * READ_COMMITTED por lo mismo que confirmar.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaConfirmadaDTO confirmarPagoStripe(String idSesion, Integer idHuesped) {
        Huesped huesped = huespedActivo(idHuesped, "Solo un huésped con la cuenta activa puede reservar");
        SesionStripeDTO sesion = stripeService.consultarSesion(idSesion);
        Map<String, String> datos = sesion.getMetadatos();
        if (datos == null || !String.valueOf(idHuesped).equals(datos.get(META_HUESPED))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta sesión de pago no es tuya");
        }
        if (!sesion.isPagada()) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "El pago todavía no se ha completado en Stripe: la reserva no se creó");
        }

        ReservaConfirmarRequestDTO reservaPagada = new ReservaConfirmarRequestDTO();
        reservaPagada.setIdHuesped(idHuesped);
        reservaPagada.setIdPublicacion(Integer.valueOf(datos.get(META_PUBLICACION)));
        reservaPagada.setFechaLlegada(LocalDate.parse(datos.get(META_LLEGADA)));
        reservaPagada.setFechaSalida(LocalDate.parse(datos.get(META_SALIDA)));
        reservaPagada.setNumHuespedes(Integer.valueOf(datos.get(META_HUESPEDES)));
        reservaPagada.setClaveIdempotencia(idSesion);

        Optional<Reserva> repetida = reservaRepository.findByClaveIdempotencia(idSesion);
        if (repetida.isPresent()) {
            return reservaYaHecha(repetida.get(), huesped);
        }
        publicacionService.esperarTurnoParaReservar(reservaPagada.getIdPublicacion());
        repetida = reservaRepository.findByClaveIdempotencia(idSesion);
        if (repetida.isPresent()) {
            return reservaYaHecha(repetida.get(), huesped);
        }

        Publicacion publicacion;
        try {
            publicacion = publicacionService.obtenerParaReservar(reservaPagada.getIdPublicacion(),
                    reservaPagada.getFechaLlegada(), reservaPagada.getFechaSalida());
        } catch (ResponseStatusException e) {
            stripeService.reembolsar(sesion.getIdTransaccion(), "reembolso-" + idSesion);
            throw new ResponseStatusException(e.getStatusCode(), e.getReason()
                    + ". No se creó la reserva y se devolvió el pago completo a tu tarjeta");
        }
        Cotizacion cotizacion = cotizar(publicacion, reservaPagada.getFechaLlegada(), reservaPagada.getFechaSalida());
        if (sesion.getTotalPagado() == null || Math.abs(sesion.getTotalPagado() - cotizacion.getTotal()) >= 0.01) {
            stripeService.reembolsar(sesion.getIdTransaccion(), "reembolso-" + idSesion);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El precio del alojamiento cambió mientras pagabas. "
                            + "No se creó la reserva y se devolvió el pago completo a tu tarjeta");
        }
        return crearReservaPagada(reservaPagada, huesped, publicacion, cotizacion, sesion.getIdTransaccion());
    }

    /**
     * Crea la reserva ya pagada, bloquea el período (si no necesita aprobación), registra el pago y notifica al
     * huésped y al anfitrión. La usan el pago simulado (confirmar) y el pago con Stripe (confirmarPagoStripe).
     */
    private ReservaConfirmadaDTO crearReservaPagada(ReservaConfirmarRequestDTO dto, Huesped huesped,
                                                    Publicacion publicacion, Cotizacion cotizacion,
                                                    String idTransaccion) {
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
        Pago pago = registrarPago(reserva, huesped, idTransaccion);

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

    @Override
    @Transactional(readOnly = true)
    public SimulacionCancelacionDTO simularCancelacion(Integer idReserva, Integer idHuesped) {
        Huesped huesped = huespedActivo(idHuesped,
                "Solo un huésped con la cuenta activa puede consultar la devolución de una reserva");
        Reserva reserva = reservaDelHuesped(idReserva, huesped,
                "Solo el huésped que hizo la reserva puede consultar su devolución");
        exigirCancelable(reserva);

        double devolucion = devolucionSegunPolitica(reserva);
        PoliticaCancelacion politica = reserva.getPublicacion().getPoliticaCancelacion();
        SimulacionCancelacionDTO simulacion = new SimulacionCancelacionDTO();
        simulacion.setIdReserva(reserva.getIdReserva());
        simulacion.setEstado(reserva.getEstado());
        simulacion.setPolitica(politica == null ? null : politica.codigo());
        simulacion.setValorTotal(reserva.getCotizacion().getTotal());
        simulacion.setValorDevolucion(devolucion);
        simulacion.setAplicaDevolucion(devolucion > 0);
        simulacion.setMensaje(devolucion > 0
                ? "Si cancelas ahora se te devolverían " + devolucion
                : "Si cancelas ahora no recibirías devolución, el valor de la devolución es 0");
        return simulacion;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaCanceladaDTO cancelar(Integer idReserva, ReservaCancelarRequestDTO dto) {
        Huesped huesped = huespedActivo(dto.getIdHuesped(), "Solo un huésped con la cuenta activa puede cancelar una reserva");
        Reserva reserva = reservaDelHuesped(idReserva, huesped, "Solo el huésped que hizo la reserva puede cancelarla");
        exigirCancelable(reserva);

        double devolucion = devolucionSegunPolitica(reserva);
        Cancelacion cancelacion = completarCancelacion(reserva, huesped.getUsuario(), dto.getMotivo(), devolucion);

        String fechas = " del " + reserva.getFechaLlegada() + " al " + reserva.getFechaSalida();
        String titulo = reserva.getPublicacion().getTitulo();
        notificar(huesped.getUsuario(), "RESERVA_CANCELADA",
                "Cancelaste tu reserva en " + titulo + fechas + ". Devolución: " + devolucion);
        notificar(reserva.getPublicacion().getAnfitrion().getUsuario(), "RESERVA_CANCELADA_POR_HUESPED",
                "El huésped canceló su reserva en " + titulo + fechas);

        return toCanceladaDTO(reserva, cancelacion, devolucion > 0
                ? "La reserva fue cancelada y se devolvió " + devolucion
                : "La reserva fue cancelada sin devolución");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaCanceladaDTO cancelarComoAnfitrion(Integer idReserva, ReservaCancelarAnfitrionRequestDTO dto) {
        Anfitrion anfitrion = anfitrionRepository.findById(dto.getIdAnfitrion())
                .filter(a -> a.getUsuario().estaActiva() && !Boolean.TRUE.equals(a.getUsuario().getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo un anfitrión con la cuenta activa puede cancelar una reserva"));
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe"));
        if (!reserva.getPublicacion().getAnfitrion().getIdUsuario().equals(anfitrion.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el anfitrión de este alojamiento puede cancelar la reserva");
        }
        exigirCancelable(reserva);

        double devolucion = reserva.getCotizacion().getTotal();
        Cancelacion cancelacion = completarCancelacion(reserva, anfitrion.getUsuario(), dto.getMotivo(), devolucion);

        String fechas = " del " + reserva.getFechaLlegada() + " al " + reserva.getFechaSalida();
        String titulo = reserva.getPublicacion().getTitulo();
        notificar(reserva.getHuesped().getUsuario(), "RESERVA_CANCELADA_POR_ANFITRION",
                "El anfitrión canceló tu reserva en " + titulo + fechas + ". Motivo: " + cancelacion.getMotivo()
                        + ". Devolución: " + devolucion);
        notificar(anfitrion.getUsuario(), "RESERVA_CANCELADA", "Cancelaste la reserva en " + titulo + fechas);

        return toCanceladaDTO(reserva, cancelacion, devolucion > 0
                ? "La reserva fue cancelada y se devolvió " + devolucion
                : "La reserva fue cancelada sin devolución");
    }

    private Huesped huespedActivo(Integer idHuesped, String mensajeSiNoPuede) {
        return huespedRepository.findById(idHuesped)
                .filter(h -> h.getUsuario().estaActiva() && !Boolean.TRUE.equals(h.getUsuario().getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, mensajeSiNoPuede));
    }

    private Reserva reservaDelHuesped(Integer idReserva, Huesped huesped, String mensajeSiNoEsSuya) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe"));
        if (!reserva.getHuesped().getIdUsuario().equals(huesped.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, mensajeSiNoEsSuya);
        }
        return reserva;
    }

    private double devolucionSegunPolitica(Reserva reserva) {
        PoliticaCancelacion politica = reserva.getPublicacion().getPoliticaCancelacion();
        return politica == null ? 0.0 : politica.calcularDevolucion(reserva);
    }

    private void exigirCancelable(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA && reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La reserva no se puede cancelar porque su estado es " + reserva.getEstado());
        }
    }

    private Cancelacion completarCancelacion(Reserva reserva, Usuario quienCancela, String motivo, double devolucion) {
        boolean teniaElPeriodoBloqueado = reserva.getEstado() == EstadoReserva.CONFIRMADA;
        reserva.cancelar();
        reservaRepository.save(reserva);
        if (teniaElPeriodoBloqueado) {
            publicacionService.liberarPeriodoReservado(reserva.getPublicacion().getIdPublicacion(),
                    reserva.getFechaLlegada(), reserva.getFechaSalida());
        }

        Cancelacion cancelacion = new Cancelacion();
        cancelacion.setReserva(reserva);
        cancelacion.setUsuario(quienCancela);
        cancelacion.setMotivo(motivo.trim());
        cancelacion.setValorDevolucion(devolucion);
        cancelacion.setEstadoDevolucion(devolucion > 0 ? EstadoDevolucion.DEVUELTA : EstadoDevolucion.SIN_DEVOLUCION);
        cancelacionRepository.save(cancelacion);
        if (devolucion > 0) {
            registrarDevolucion(reserva, reserva.getHuesped(), devolucion);
        }
        registrarEventoDeCancelacion(reserva, cancelacion);
        return cancelacion;
    }

    private ReservaCanceladaDTO toCanceladaDTO(Reserva reserva, Cancelacion cancelacion, String mensaje) {
        ReservaCanceladaDTO respuesta = new ReservaCanceladaDTO();
        respuesta.setIdReserva(reserva.getIdReserva());
        respuesta.setEstado(reserva.getEstado());
        respuesta.setValorDevolucion(cancelacion.getValorDevolucion());
        respuesta.setEstadoDevolucion(cancelacion.getEstadoDevolucion());
        respuesta.setMensaje(mensaje);
        return respuesta;
    }

    private void registrarEventoDeCancelacion(Reserva reserva, Cancelacion cancelacion) {
        EventoAuditoria evento = new EventoAuditoria();
        evento.setUsuario(cancelacion.getUsuario());
        evento.setOperacion(EventoAuditoria.CANCELAR_RESERVA);
        evento.setEntidadAfectada(EventoAuditoria.ENTIDAD_RESERVA);
        evento.setIdEntidad(reserva.getIdReserva());
        evento.setResultado(reserva.getEstado().name());
        evento.setDetalle("Motivo: " + cancelacion.getMotivo() + ". Devolución: " + cancelacion.getValorDevolucion()
                + " (" + cancelacion.getEstadoDevolucion() + ")");
        eventoAuditoriaRepository.save(evento);
    }

    private void registrarDevolucion(Reserva reserva, Huesped huesped, double valor) {
        Pago devolucion = new Pago();
        devolucion.setReserva(reserva);
        devolucion.setTipoMovimiento(Pago.TIPO_DEVOLUCION);
        devolucion.setValor(valor);
        devolucion.setEmisor("MARKETPLACE");
        devolucion.setReceptor("HUESPED-" + huesped.getIdUsuario());
        devolucion.setIdTransaccion("SIM-" + UUID.randomUUID());
        devolucion.setEstado(Pago.APROBADO);
        pagoRepository.save(devolucion);
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

    private void exigirCapacidad(Publicacion publicacion, Integer numHuespedes) {
        if (publicacion.getCapacidad() != null && numHuespedes > publicacion.getCapacidad()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El alojamiento admite máximo " + publicacion.getCapacidad() + " huéspedes");
        }
    }

    /** idTransaccion: "SIM-..." en el pago simulado, o el id del cobro en Stripe (pi_...). */
    private Pago registrarPago(Reserva reserva, Huesped huesped, String idTransaccion) {
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setTipoMovimiento(Pago.TIPO_PAGO);
        pago.setValor(reserva.getCotizacion().getTotal());
        pago.setEmisor("HUESPED-" + huesped.getIdUsuario());
        pago.setReceptor("MARKETPLACE");
        pago.setIdTransaccion(idTransaccion);
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

    @Override
    @Transactional(readOnly = true)
    public List<ReservaListadoDTO> listarDelHuesped(Integer idHuesped, EstadoReserva estado) {
        huespedActivo(idHuesped, "Solo un huésped con la cuenta activa puede consultar sus reservas");
        return reservaRepository.findByHuesped_IdUsuarioOrderByFechaLlegadaDesc(idHuesped)
                .stream()
                .filter(r -> estado == null || r.getEstado() == estado)
                .map(this::toListadoDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaListadoDTO> listarDelAnfitrion(Integer idAnfitrion, EstadoReserva estado) {
        exigirAnfitrionHabilitado(idAnfitrion);
        return reservaRepository.findByPublicacion_Anfitrion_IdUsuarioOrderByFechaLlegadaDesc(idAnfitrion)
                .stream()
                .filter(r -> estado == null || r.getEstado() == estado)
                .map(this::toListadoDTO)
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

    private ReservaListadoDTO toListadoDTO(Reserva reserva) {
        ReservaListadoDTO dto = new ReservaListadoDTO();
        dto.setIdReserva(reserva.getIdReserva());
        dto.setEstado(reserva.getEstado());
        dto.setFechaLlegada(reserva.getFechaLlegada());
        dto.setFechaSalida(reserva.getFechaSalida());
        dto.setNumHuespedes(reserva.getNumHuespedes());
        dto.setTotal(reserva.getCotizacion().getTotal());
        dto.setIdPublicacion(reserva.getPublicacion().getIdPublicacion());
        dto.setTituloPublicacion(reserva.getPublicacion().getTitulo());
        dto.setIdHuesped(reserva.getHuesped().getIdUsuario());
        dto.setNombreHuesped(reserva.getHuesped().getUsuario().getNombreCompleto());
        dto.setIdAnfitrion(reserva.getPublicacion().getAnfitrion().getIdUsuario());
        dto.setNombreAnfitrion(reserva.getPublicacion().getAnfitrion().getUsuario().getNombreCompleto());
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
