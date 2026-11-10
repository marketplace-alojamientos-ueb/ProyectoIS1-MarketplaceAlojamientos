package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
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

    public ReservaServiceImpl(ReservaRepository reservaRepository, AnfitrionRepository anfitrionRepository) {
        this.reservaRepository = reservaRepository;
        this.anfitrionRepository = anfitrionRepository;
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

    private void exigirAnfitrionHabilitado(Integer idAnfitrion) {
        anfitrionRepository.findById(idAnfitrion)
                .filter(a -> a.getUsuario().estaActiva() && !Boolean.TRUE.equals(a.getUsuario().getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo un anfitrión puede consultar sus reservas pendientes"));
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
