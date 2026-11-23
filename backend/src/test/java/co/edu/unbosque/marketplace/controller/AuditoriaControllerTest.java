package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.EventoAuditoriaDTO;
import co.edu.unbosque.marketplace.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas del controller: revisa que llame al servicio y responda con el código HTTP correcto. */
class AuditoriaControllerTest {

    @Test
    void listarEventosResponde200ConLosEventos() {
        AuditoriaService auditoriaService = mock(AuditoriaService.class);
        AuditoriaController controller = new AuditoriaController(auditoriaService);
        EventoAuditoriaDTO evento = new EventoAuditoriaDTO();
        evento.setIdEvento(12);
        when(auditoriaService.listarEventos(4, "CANCELAR_RESERVA")).thenReturn(List.of(evento));

        ResponseEntity<List<EventoAuditoriaDTO>> respuesta = controller.listarEventos(4, "CANCELAR_RESERVA");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(12, respuesta.getBody().get(0).getIdEvento());
    }
}
