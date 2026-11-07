package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionEditarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionResponseDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Todo lo de las publicaciones (tabla PUBLICACION). Por ahora, la creación y las políticas que se pueden escoger.
 */
public interface PublicacionService {

    /**
     * Crea la publicación de un anfitrión, con sus períodos como DISPONIBLE. Si trae todos los campos mínimos y al menos
     * un período queda en PENDIENTE_REVISION; si falta algo queda en BORRADOR y la respuesta trae en camposFaltantes
     * qué es. Responde 403 si el id no es de un anfitrión con la cuenta activa y sin restricciones, y 400 si la política
     * de cancelación enviada no existe o si algún período enviado es inválido (fin no posterior al inicio, inicio en el
     * pasado o cruce con otro período).
     */
    PublicacionResponseDTO crear(PublicacionRequestDTO dto);

    /**
     * Políticas de cancelación que el anfitrión puede escoger al crear la publicación, con su nombre y descripción.
     * Solo incluye las que tienen una clase implementada.
     */
    List<PoliticaCancelacionDTO> listarPoliticas();

    PublicacionResponseDTO obtenerParaEditar(Integer idPublicacion, Integer idUsuario);

    PublicacionResponseDTO editar(Integer idPublicacion, PublicacionEditarRequestDTO dto);

    List<PublicacionResponseDTO> listar(Integer idAnfitrion);

    PublicacionResponseDTO obtener(Integer idPublicacion);

    void eliminar(Integer idPublicacion, Integer idUsuario);

    PublicacionResponseDTO pausar(Integer idPublicacion, Integer idUsuario);

    List<PublicacionBusquedaDTO> buscar(PublicacionBusquedaRequestDTO filtros);

    /**
     * Marca como RESERVADO el rango de una reserva confirmada; lo que sobre de los períodos disponibles sigue
     * disponible. No tiene endpoint: lo debe llamar el servicio de reservas dentro de su misma transacción.
     * Responde 400 si la salida no es posterior a la llegada, 404 si la publicación no existe y 409 si alguna noche del
     * rango no está disponible.
     */
    void bloquearPeriodoReservado(Integer idPublicacion, LocalDate fechaLlegada, LocalDate fechaSalida);
}
