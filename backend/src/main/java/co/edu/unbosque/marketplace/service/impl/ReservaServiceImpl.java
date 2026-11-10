package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.model.entity.Notificacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.NotificacionRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import co.edu.unbosque.marketplace.service.PublicacionService;
import co.edu.unbosque.marketplace.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final AnfitrionRepository anfitrionRepository;
    private final PublicacionService publicacionService;
    private final NotificacionRepository notificacionRepository;

    public ReservaServiceImpl(ReservaRepository reservaRepository, AnfitrionRepository anfitrionRepository,
                              PublicacionService publicacionService, NotificacionRepository notificacionRepository) {
        this.reservaRepository = reservaRepository;
        this.anfitrionRepository = anfitrionRepository;
        this.publicacionService = publicacionService;
        this.notificacionRepository = notificacionRepository;
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
    public MensajeResponseDTO aceptar(Integer idReserva, Integer idAnfitrion) {
        Reserva reserva = obtenerPendienteDelAnfitrion(idReserva, idAnfitrion);
        publicacionService.bloquearPeriodoReservado(reserva.getPublicacion().getIdPublicacion(),
                reserva.getFechaLlegada(), reserva.getFechaSalida());
        reserva.confirmar();
        reservaRepository.save(reserva);
        notificarAlHuesped(reserva, "RESERVA_CONFIRMADA", "confirmada");
        return mensaje("La reserva fue confirmada y se notificó al huésped");
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
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(reserva.getHuesped().getUsuario());
        notificacion.setTipo(tipo);
        notificacion.setMensaje("Tu reserva en " + reserva.getPublicacion().getTitulo() + " del "
                + reserva.getFechaLlegada() + " al " + reserva.getFechaSalida() + " fue " + resultado);
        notificacionRepository.save(notificacion);
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
