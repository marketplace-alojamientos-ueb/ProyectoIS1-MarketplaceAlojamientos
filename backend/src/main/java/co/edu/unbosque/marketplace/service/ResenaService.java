package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaResponseDTO;

import java.util.List;

public interface ResenaService {

    /**
     * Crea la reseña de una estancia completada (MA-81). Responde 403 si el huésped no está activo o la reserva no es
     * suya, 404 si la reserva no existe y 409 si la estancia no se ha completado o la reserva ya tiene reseña.
     */
    ResenaResponseDTO crear(ResenaRequestDTO dto);

    /** Una reseña visible. 404 si no existe o está oculta. */
    ResenaResponseDTO obtener(Integer idResena);

    /** Reseñas visibles de una publicación, de la más reciente a la más antigua. */
    List<ResenaResponseDTO> listarPorPublicacion(Integer idPublicacion);

    /** Reseñas que ha escrito un huésped, incluidas las ocultas por moderación. */
    List<ResenaResponseDTO> listarPorHuesped(Integer idHuesped);

    /** Edita la calificación y el comentario. Solo el autor, y no si la reseña está oculta (409). */
    ResenaResponseDTO actualizar(Integer idResena, ResenaActualizarDTO dto);

    /** Elimina la reseña. Solo el autor, y no si la reseña está oculta (409). */
    MensajeResponseDTO eliminar(Integer idResena, Integer idHuesped);
}
