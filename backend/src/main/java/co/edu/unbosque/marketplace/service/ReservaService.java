package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCanceladaDTO;
import co.edu.unbosque.marketplace.model.dto.SimulacionCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarAnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmadaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaListadoDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;

import java.util.List;

public interface ReservaService {

    List<ReservaPendienteDTO> listarPendientes(Integer idAnfitrion);

    List<ReservaListadoDTO> listarDelHuesped(Integer idHuesped, EstadoReserva estado);

    List<ReservaListadoDTO> listarDelAnfitrion(Integer idAnfitrion, EstadoReserva estado);

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

    /**
     * Pago con Stripe, paso 1: revisa huésped, fechas, disponibilidad y capacidad, calcula la cotización y crea la
     * sesión de pago en Stripe. No crea la reserva. Responde 400, 403, 404 o 409 igual que confirmar, 503 si Stripe
     * no está configurado y 502 si Stripe falla.
     */
    ReservaCheckoutDTO iniciarPagoStripe(ReservaCheckoutRequestDTO dto);

    /**
     * Pago con Stripe, paso 2: si Stripe confirma que la sesión se pagó, crea la reserva (como confirmar). Es
     * idempotente por el id de la sesión. Responde 402 si no se ha pagado, 403 si la sesión no es del huésped, 404 si
     * la sesión no existe y 409 (con devolución del pago en Stripe) si se perdió la disponibilidad o cambió el precio.
     */
    ReservaConfirmadaDTO confirmarPagoStripe(String idSesion, Integer idHuesped);

    SimulacionCancelacionDTO simularCancelacion(Integer idReserva, Integer idHuesped);

    ReservaCanceladaDTO cancelar(Integer idReserva, ReservaCancelarRequestDTO dto);

    ReservaCanceladaDTO cancelarComoAnfitrion(Integer idReserva, ReservaCancelarAnfitrionRequestDTO dto);
}
