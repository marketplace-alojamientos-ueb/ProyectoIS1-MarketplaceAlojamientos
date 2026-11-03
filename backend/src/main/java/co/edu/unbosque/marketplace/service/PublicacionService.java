package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionResponseDTO;

import java.util.List;

/**
 * Todo lo de las publicaciones (tabla PUBLICACION). Por ahora, la creación y las políticas que se pueden escoger.
 */
public interface PublicacionService {

    /**
     * Crea la publicación de un anfitrión. Si trae todos los campos mínimos queda en PENDIENTE_REVISION; si falta
     * alguno queda en BORRADOR y la respuesta trae en camposFaltantes cuáles son. Responde 403 si el id no es de un
     * anfitrión con la cuenta activa y sin restricciones, y 400 si la política de cancelación enviada no existe.
     */
    PublicacionResponseDTO crear(PublicacionRequestDTO dto);

    /**
     * Políticas de cancelación que el anfitrión puede escoger al crear la publicación, con su nombre y descripción.
     * Solo incluye las que tienen una clase implementada.
     */
    List<PoliticaCancelacionDTO> listarPoliticas();
}
