package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaModerarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaReportadaDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaReportarDTO;
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

    /**
     * Reporta una reseña visible para que un administrador la revise (MA-82). El reporte queda en la auditoría.
     * Responde 403 si el usuario no está activo, 404 si la reseña no existe y 409 si ya está oculta, si es el autor
     * o si ya tiene un reporte suyo pendiente.
     */
    MensajeResponseDTO reportar(Integer idResena, ResenaReportarDTO dto);

    /** Reseñas visibles con reportes pendientes, del reporte más reciente al más antiguo. Solo administradores. */
    List<ResenaReportadaDTO> listarReportadas(Integer idAdmin);

    /**
     * El administrador oculta la reseña: deja de verse, se guarda el motivo, la acción de moderación y el evento de
     * auditoría, y se notifica al autor. 403 si no es administrador, 404 si no existe, 409 si ya estaba oculta.
     */
    ResenaResponseDTO ocultar(Integer idResena, ResenaModerarDTO dto);

    /**
     * El administrador revisa los reportes y decide dejar la reseña visible: los reportes pendientes se dan por
     * atendidos. 403 si no es administrador, 404 si no existe, 409 si está oculta o no tiene reportes pendientes.
     */
    MensajeResponseDTO mantener(Integer idResena, ResenaModerarDTO dto);
}
