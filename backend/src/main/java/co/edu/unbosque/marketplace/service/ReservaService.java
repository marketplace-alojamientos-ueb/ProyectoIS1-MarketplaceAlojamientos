package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmadaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;

import java.util.List;

public interface ReservaService {

    List<ReservaPendienteDTO> listarPendientes(Integer idAnfitrion);

    MensajeResponseDTO aceptar(Integer idReserva, Integer idAnfitrion);

    MensajeResponseDTO rechazar(Integer idReserva, Integer idAnfitrion);

    /**
     * Confirma una reserva con pago simulado (MA-102). Primero revisa que el período siga disponible y solo entonces
     * procesa el pago; si el pago se aprueba crea la reserva con su cotización, bloquea el período, guarda el pago y
     * notifica al huésped y al anfitrión. Si la publicación es CON_APROBACION la reserva queda PENDIENTE y el período
     * se bloquea cuando el anfitrión la acepta. Responde 400 si los datos no tienen sentido, 403 si el huésped no
     * puede reservar, 404 si la publicación no existe, 409 si se perdió la disponibilidad (sin procesar el pago) y
     * 402 si el pago es rechazado (sin crear la reserva ni bloquear el período).
     */
    ReservaConfirmadaDTO confirmar(ReservaConfirmarRequestDTO dto);
}
