package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
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
}
