package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Resena;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.repository.HuespedRepository;
import co.edu.unbosque.marketplace.repository.ResenaRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import co.edu.unbosque.marketplace.service.ResenaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ResenaServiceImpl implements ResenaService {

    private final ResenaRepository resenaRepository;
    private final ReservaRepository reservaRepository;
    private final HuespedRepository huespedRepository;

    public ResenaServiceImpl(ResenaRepository resenaRepository, ReservaRepository reservaRepository,
                             HuespedRepository huespedRepository) {
        this.resenaRepository = resenaRepository;
        this.reservaRepository = reservaRepository;
        this.huespedRepository = huespedRepository;
    }

    @Override
    public ResenaResponseDTO crear(ResenaRequestDTO dto) {
        Huesped huesped = huespedActivo(dto.getIdHuesped(), "Solo un huésped con la cuenta activa puede dejar una reseña");
        Reserva reserva = reservaRepository.findById(dto.getIdReserva())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe"));
        if (!reserva.getHuesped().getIdUsuario().equals(huesped.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el huésped que hizo la reserva puede dejar la reseña");
        }
        if (!estanciaCompletada(reserva)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo puedes dejar una reseña cuando la estancia se haya completado");
        }
        if (resenaRepository.existsByReserva_IdReserva(reserva.getIdReserva())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya dejaste una reseña para esta reserva");
        }

        Resena resena = new Resena();
        resena.setReserva(reserva);
        resena.setCalificacion(dto.getCalificacion());
        resena.setComentario(limpiarComentario(dto.getComentario()));
        resena.setFecha(LocalDate.now());
        resena.setVisible(true);
        return toResponseDTO(resenaRepository.save(resena));
    }

    @Override
    @Transactional(readOnly = true)
    public ResenaResponseDTO obtener(Integer idResena) {
        Resena resena = buscar(idResena);
        if (!Boolean.TRUE.equals(resena.getVisible())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La reseña no existe");
        }
        return toResponseDTO(resena);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaResponseDTO> listarPorPublicacion(Integer idPublicacion) {
        return resenaRepository
                .findByReserva_Publicacion_IdPublicacionAndVisibleTrueOrderByFechaDescIdResenaDesc(idPublicacion)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaResponseDTO> listarPorHuesped(Integer idHuesped) {
        huespedActivo(idHuesped, "Solo un huésped con la cuenta activa puede consultar sus reseñas");
        return resenaRepository.findByReserva_Huesped_IdUsuarioOrderByFechaDescIdResenaDesc(idHuesped)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public ResenaResponseDTO actualizar(Integer idResena, ResenaActualizarDTO dto) {
        Resena resena = resenaDelAutor(idResena, dto.getIdHuesped(), "Solo el autor puede editar la reseña");
        resena.setCalificacion(dto.getCalificacion());
        resena.setComentario(limpiarComentario(dto.getComentario()));
        return toResponseDTO(resenaRepository.save(resena));
    }

    @Override
    public MensajeResponseDTO eliminar(Integer idResena, Integer idHuesped) {
        Resena resena = resenaDelAutor(idResena, idHuesped, "Solo el autor puede eliminar la reseña");
        resenaRepository.delete(resena);
        MensajeResponseDTO respuesta = new MensajeResponseDTO();
        respuesta.setMensaje("La reseña fue eliminada");
        return respuesta;
    }

    /**
     * La estancia está completada si la reserva quedó COMPLETADA o si estaba CONFIRMADA y ya pasó la fecha de salida
     * (todavía no hay nada que pase las reservas a COMPLETADA de forma automática).
     */
    private boolean estanciaCompletada(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.COMPLETADA) {
            return true;
        }
        return reserva.getEstado() == EstadoReserva.CONFIRMADA && !reserva.getFechaSalida().isAfter(LocalDate.now());
    }

    private Resena resenaDelAutor(Integer idResena, Integer idHuesped, String mensajeSiNoEsSuya) {
        Huesped huesped = huespedActivo(idHuesped, "Solo un huésped con la cuenta activa puede modificar sus reseñas");
        Resena resena = buscar(idResena);
        if (!resena.getReserva().getHuesped().getIdUsuario().equals(huesped.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, mensajeSiNoEsSuya);
        }
        if (!Boolean.TRUE.equals(resena.getVisible())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La reseña fue ocultada por moderación y no se puede modificar");
        }
        return resena;
    }

    private Resena buscar(Integer idResena) {
        return resenaRepository.findById(idResena)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reseña no existe"));
    }

    private Huesped huespedActivo(Integer idHuesped, String mensajeSiNoPuede) {
        return huespedRepository.findById(idHuesped)
                .filter(h -> h.getUsuario().estaActiva() && !Boolean.TRUE.equals(h.getUsuario().getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, mensajeSiNoPuede));
    }

    /** Un comentario vacío o solo con espacios se guarda como null. */
    private String limpiarComentario(String comentario) {
        return comentario == null || comentario.isBlank() ? null : comentario.trim();
    }

    private ResenaResponseDTO toResponseDTO(Resena resena) {
        Reserva reserva = resena.getReserva();
        ResenaResponseDTO dto = new ResenaResponseDTO();
        dto.setIdResena(resena.getIdResena());
        dto.setIdReserva(reserva.getIdReserva());
        dto.setIdPublicacion(reserva.getPublicacion().getIdPublicacion());
        dto.setTituloPublicacion(reserva.getPublicacion().getTitulo());
        dto.setIdHuesped(reserva.getHuesped().getIdUsuario());
        dto.setNombreHuesped(reserva.getHuesped().getUsuario().getNombreCompleto());
        dto.setCalificacion(resena.getCalificacion());
        dto.setComentario(resena.getComentario());
        dto.setFecha(resena.getFecha());
        dto.setVisible(resena.getVisible());
        return dto;
    }
}
