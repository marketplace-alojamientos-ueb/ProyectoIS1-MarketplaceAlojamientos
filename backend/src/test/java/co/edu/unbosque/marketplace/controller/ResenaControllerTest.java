package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaResponseDTO;
import co.edu.unbosque.marketplace.service.ResenaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas del controller: revisa que llame al servicio y responda con el código HTTP correcto. */
class ResenaControllerTest {

    private ResenaService resenaService;
    private ResenaController controller;
    private ResenaResponseDTO resena;

    @BeforeEach
    void setUp() {
        resenaService = mock(ResenaService.class);
        controller = new ResenaController(resenaService);
        resena = new ResenaResponseDTO();
        resena.setIdResena(5);
    }

    @Test
    void crearResponde201ConLaResena() {
        ResenaRequestDTO dto = new ResenaRequestDTO();
        when(resenaService.crear(dto)).thenReturn(resena);

        ResponseEntity<ResenaResponseDTO> respuesta = controller.crear(dto);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(resena, respuesta.getBody());
    }

    @Test
    void obtenerResponde200() {
        when(resenaService.obtener(5)).thenReturn(resena);

        ResponseEntity<ResenaResponseDTO> respuesta = controller.obtener(5);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(resena, respuesta.getBody());
    }

    @Test
    void listarPorPublicacionResponde200() {
        when(resenaService.listarPorPublicacion(15)).thenReturn(List.of(resena));

        ResponseEntity<List<ResenaResponseDTO>> respuesta = controller.listarPorPublicacion(15);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(5, respuesta.getBody().get(0).getIdResena());
    }

    @Test
    void listarPorHuespedResponde200() {
        when(resenaService.listarPorHuesped(20)).thenReturn(List.of(resena));

        ResponseEntity<List<ResenaResponseDTO>> respuesta = controller.listarPorHuesped(20);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(5, respuesta.getBody().get(0).getIdResena());
    }

    @Test
    void actualizarResponde200() {
        ResenaActualizarDTO dto = new ResenaActualizarDTO();
        when(resenaService.actualizar(5, dto)).thenReturn(resena);

        ResponseEntity<ResenaResponseDTO> respuesta = controller.actualizar(5, dto);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(resena, respuesta.getBody());
    }

    @Test
    void eliminarResponde200ConElMensaje() {
        MensajeResponseDTO mensaje = new MensajeResponseDTO();
        mensaje.setMensaje("La reseña fue eliminada");
        when(resenaService.eliminar(5, 20)).thenReturn(mensaje);

        ResponseEntity<MensajeResponseDTO> respuesta = controller.eliminar(5, 20);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("La reseña fue eliminada", respuesta.getBody().getMensaje());
    }
}
