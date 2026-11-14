package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionDetalleDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionEditarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Publicacion;

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

    PublicacionDetalleDTO consultarDetalle(Integer idPublicacion);

    /**
     * Marca como RESERVADO el rango de una reserva confirmada; lo que sobre de los períodos disponibles sigue
     * disponible. No tiene endpoint: lo debe llamar el servicio de reservas dentro de su misma transacción.
     * Responde 400 si la salida no es posterior a la llegada, 404 si la publicación no existe y 409 si alguna noche del
     * rango no está disponible.
     */
    void bloquearPeriodoReservado(Integer idPublicacion, LocalDate fechaLlegada, LocalDate fechaSalida);

    /**
     * Trae la publicación para reservarla, bloqueando su fila hasta que termine la transacción para que otra reserva no
     * tome las mismas fechas a la vez. No tiene endpoint: lo llama el servicio de reservas. Responde 404 si la
     * publicación no existe, y 409 si no está ACTIVA o si alguna noche del rango ya no está disponible.
     */
    Publicacion obtenerParaReservar(Integer idPublicacion, LocalDate fechaLlegada, LocalDate fechaSalida);

    /**
     * Bloquea la fila de la publicación hasta que termine la transacción, sin revisar nada más: si otra reserva de la
     * misma publicación está en curso, espera a que termine. No tiene endpoint: lo llama el servicio de reservas antes
     * de volver a revisar la clave de idempotencia. Responde 404 si la publicación no existe.
     */
    void esperarTurnoParaReservar(Integer idPublicacion);
}
