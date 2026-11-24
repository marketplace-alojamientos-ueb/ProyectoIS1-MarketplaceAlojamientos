package co.edu.unbosque.marketplace.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Lo que el backend necesita de una sesión de pago (Checkout Session) de Stripe. No se le envía al front: lo usa
 * ReservaService para crear el pago y para revisar si ya se pagó.
 */
@Getter
@Setter
public class SesionStripeDTO {

    /** Id de la sesión en Stripe (empieza por cs_test_). */
    private String idSesion;

    /** Página de pago de Stripe a la que el front redirige al huésped. */
    private String urlPago;

    /** true cuando Stripe confirma que el pago se hizo (payment_status = "paid"). */
    private boolean pagada;

    /** Id del cobro en Stripe (PaymentIntent, empieza por pi_). Es null mientras no se haya pagado. */
    private String idTransaccion;

    /** Valor cobrado en pesos (Stripe lo maneja en centavos). */
    private Double totalPagado;

    /** Datos de la reserva que se guardaron en la sesión al crearla. */
    private Map<String, String> metadatos;
}
