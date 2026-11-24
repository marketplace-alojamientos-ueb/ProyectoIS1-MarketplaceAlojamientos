package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCanceladaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarAnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.SimulacionCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmadaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaListadoDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas del controller: revisa que llame al servicio y responda con el código HTTP correcto. */
class ReservaControllerTest {

    @Test
    void listarPendientesResponde200ConLasReservasDelAnfitrion() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        ReservaPendienteDTO reserva = new ReservaPendienteDTO();
        reserva.setIdReserva(31);
        when(reservaService.listarPendientes(7)).thenReturn(List.of(reserva));

        ResponseEntity<List<ReservaPendienteDTO>> respuesta = controller.listarPendientes(7);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(31, respuesta.getBody().get(0).getIdReserva());
    }

    @Test
    void listarDelHuespedResponde200ConSusReservas() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        ReservaListadoDTO reserva = new ReservaListadoDTO();
        reserva.setIdReserva(31);
        when(reservaService.listarDelHuesped(20, EstadoReserva.CONFIRMADA)).thenReturn(List.of(reserva));

        ResponseEntity<List<ReservaListadoDTO>> respuesta = controller.listarDelHuesped(20, EstadoReserva.CONFIRMADA);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(31, respuesta.getBody().get(0).getIdReserva());
    }

    @Test
    void listarDelAnfitrionResponde200ConLasReservasDeSusAlojamientos() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        ReservaListadoDTO reserva = new ReservaListadoDTO();
        reserva.setIdReserva(31);
        when(reservaService.listarDelAnfitrion(7, null)).thenReturn(List.of(reserva));

        ResponseEntity<List<ReservaListadoDTO>> respuesta = controller.listarDelAnfitrion(7, null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(31, respuesta.getBody().get(0).getIdReserva());
    }

    @Test
    void aceptarResponde200ConElMensaje() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        MensajeResponseDTO mensaje = new MensajeResponseDTO();
        mensaje.setMensaje("La reserva fue confirmada y se notificó al huésped");
        when(reservaService.aceptar(31, 7)).thenReturn(mensaje);

        ResponseEntity<MensajeResponseDTO> respuesta = controller.aceptar(31, 7);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("La reserva fue confirmada y se notificó al huésped", respuesta.getBody().getMensaje());
    }

    @Test
    void rechazarResponde200ConElMensaje() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        MensajeResponseDTO mensaje = new MensajeResponseDTO();
        mensaje.setMensaje("La reserva fue rechazada y se notificó al huésped");
        when(reservaService.rechazar(31, 7)).thenReturn(mensaje);

        ResponseEntity<MensajeResponseDTO> respuesta = controller.rechazar(31, 7);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("La reserva fue rechazada y se notificó al huésped", respuesta.getBody().getMensaje());
    }

    @Test
    void iniciarPagoStripeResponde201ConLaUrlDePago() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        ReservaCheckoutRequestDTO solicitud = new ReservaCheckoutRequestDTO();
        ReservaCheckoutDTO sesion = new ReservaCheckoutDTO();
        sesion.setUrlPago("https://checkout.stripe.com/c/pay/cs_test_123");
        when(reservaService.iniciarPagoStripe(solicitud)).thenReturn(sesion);

        ResponseEntity<ReservaCheckoutDTO> respuesta = controller.iniciarPagoStripe(solicitud);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals("https://checkout.stripe.com/c/pay/cs_test_123", respuesta.getBody().getUrlPago());
    }

    @Test
    void confirmarPagoStripeResponde201ConLaReserva() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        ReservaConfirmadaDTO confirmada = new ReservaConfirmadaDTO();
        confirmada.setIdReserva(31);
        when(reservaService.confirmarPagoStripe("cs_test_123", 20)).thenReturn(confirmada);

        ResponseEntity<ReservaConfirmadaDTO> respuesta = controller.confirmarPagoStripe("cs_test_123", 20);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(31, respuesta.getBody().getIdReserva());
    }

    @Test
    void confirmarResponde201ConLaReserva() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        ReservaConfirmarRequestDTO solicitud = new ReservaConfirmarRequestDTO();
        ReservaConfirmadaDTO confirmada = new ReservaConfirmadaDTO();
        confirmada.setIdReserva(31);
        when(reservaService.confirmar(solicitud)).thenReturn(confirmada);

        ResponseEntity<ReservaConfirmadaDTO> respuesta = controller.confirmar(solicitud);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(31, respuesta.getBody().getIdReserva());
    }

    @Test
    void cancelarResponde200ConLaReservaCancelada() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        ReservaCancelarRequestDTO solicitud = new ReservaCancelarRequestDTO();
        ReservaCanceladaDTO cancelada = new ReservaCanceladaDTO();
        cancelada.setIdReserva(31);
        when(reservaService.cancelar(31, solicitud)).thenReturn(cancelada);

        ResponseEntity<ReservaCanceladaDTO> respuesta = controller.cancelar(31, solicitud);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(31, respuesta.getBody().getIdReserva());
    }

    @Test
    void cancelarComoAnfitrionResponde200ConLaReservaCancelada() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        ReservaCancelarAnfitrionRequestDTO solicitud = new ReservaCancelarAnfitrionRequestDTO();
        ReservaCanceladaDTO cancelada = new ReservaCanceladaDTO();
        cancelada.setIdReserva(31);
        when(reservaService.cancelarComoAnfitrion(31, solicitud)).thenReturn(cancelada);

        ResponseEntity<ReservaCanceladaDTO> respuesta = controller.cancelarComoAnfitrion(31, solicitud);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(31, respuesta.getBody().getIdReserva());
    }

    @Test
    void simularCancelacionResponde200ConLaSimulacion() {
        ReservaService reservaService = mock(ReservaService.class);
        ReservaController controller = new ReservaController(reservaService);
        SimulacionCancelacionDTO simulacion = new SimulacionCancelacionDTO();
        simulacion.setIdReserva(31);
        when(reservaService.simularCancelacion(31, 20)).thenReturn(simulacion);

        ResponseEntity<SimulacionCancelacionDTO> respuesta = controller.simularCancelacion(31, 20);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(31, respuesta.getBody().getIdReserva());
    }
}
