package co.edu.unbosque.marketplace.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Sin la llave STRIPE_SECRET_KEY no se llama a Stripe: se responde 503 con un mensaje claro. */
class StripeServiceImplTest {

    private void assertSinConfigurar(Executable accion) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, accion);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
        assertTrue(error.getReason().contains("STRIPE_SECRET_KEY"));
    }

    @Test
    void sinLlaveNoSePuedeCrearUnaSesionDePago() {
        StripeServiceImpl stripe = new StripeServiceImpl("", "cop");

        assertSinConfigurar(() -> stripe.crearSesionDePago(500000.0, "Reserva", "3 noches", "luis@correo.com",
                "http://localhost:4200/ok", "http://localhost:4200/cancelar", Map.of("idHuesped", "20")));
    }

    @Test
    void sinLlaveNoSePuedeConsultarNiReembolsar() {
        StripeServiceImpl stripe = new StripeServiceImpl(null, "cop");

        assertSinConfigurar(() -> stripe.consultarSesion("cs_test_123"));
        assertSinConfigurar(() -> stripe.reembolsar("pi_test_456", "reembolso-cs_test_123"));
    }
}
