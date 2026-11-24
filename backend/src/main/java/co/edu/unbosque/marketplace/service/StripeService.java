package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.SesionStripeDTO;

import java.util.Map;

/**
 * Conexión con Stripe en modo de pruebas. No tiene endpoint propio: lo usa ReservaService. Si la llave
 * STRIPE_SECRET_KEY no está configurada responde 503, y si Stripe falla responde 502.
 */
public interface StripeService {

    /**
     * Crea una sesión de pago (Stripe Checkout) por el total indicado y devuelve la URL de la página de pago.
     *
     * @param total       valor a cobrar en pesos
     * @param concepto    lo que verá el huésped en la página de Stripe (ej. "Reserva en Cabaña con vista al lago")
     * @param detalle     descripción corta (ej. "3 noches, del 2026-12-10 al 2026-12-13")
     * @param correo      correo del huésped, para que Stripe lo traiga ya escrito
     * @param urlExito    a dónde vuelve el huésped si paga. Debe contener {CHECKOUT_SESSION_ID}
     * @param urlCancelar a dónde vuelve el huésped si se arrepiente
     * @param metadatos   datos de la reserva que se guardan en la sesión para crearla cuando se confirme el pago
     */
    SesionStripeDTO crearSesionDePago(double total, String concepto, String detalle, String correo, String urlExito,
                                      String urlCancelar, Map<String, String> metadatos);

    /** Consulta una sesión de pago. 404 si no existe en Stripe. */
    SesionStripeDTO consultarSesion(String idSesion);

    /**
     * Devuelve el cobro completo. La clave de idempotencia hace que, si se pide dos veces la misma devolución, Stripe
     * no devuelva el dinero dos veces.
     */
    void reembolsar(String idTransaccion, String claveIdempotencia);
}
