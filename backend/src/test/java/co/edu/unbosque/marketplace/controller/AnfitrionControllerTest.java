package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.AnfitrionActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.service.AnfitrionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas del controller: revisa que llame al servicio y responda con el código HTTP correcto. */
class AnfitrionControllerTest {

    private AnfitrionService anfitrionService;
    private AnfitrionController controller;
    private final AnfitrionResponseDTO anfitrion = new AnfitrionResponseDTO();

    @BeforeEach
    void setUp() {
        anfitrionService = mock(AnfitrionService.class);
        controller = new AnfitrionController(anfitrionService);
        anfitrion.setIdUsuario(7);
    }

    @Test
    void listarTodosResponde200() {
        when(anfitrionService.listarTodos()).thenReturn(List.of(anfitrion));

        ResponseEntity<List<AnfitrionResponseDTO>> respuesta = controller.listarTodos();

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, respuesta.getBody().size());
    }

    @Test
    void buscarPorIdResponde200() {
        when(anfitrionService.buscarPorId(7)).thenReturn(anfitrion);

        ResponseEntity<AnfitrionResponseDTO> respuesta = controller.buscarPorId(7);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(7, respuesta.getBody().getIdUsuario());
    }

    @Test
    void registrarResponde201() {
        AnfitrionRequestDTO dto = new AnfitrionRequestDTO();
        when(anfitrionService.registrar(dto)).thenReturn(anfitrion);

        ResponseEntity<AnfitrionResponseDTO> respuesta = controller.registrar(dto);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(anfitrion, respuesta.getBody());
    }

    @Test
    void actualizarResponde200() {
        AnfitrionActualizarDTO dto = new AnfitrionActualizarDTO();
        when(anfitrionService.actualizar(7, dto)).thenReturn(anfitrion);

        ResponseEntity<AnfitrionResponseDTO> respuesta = controller.actualizar(7, dto);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(anfitrion, respuesta.getBody());
    }

    @Test
    void desactivarResponde204() {
        ResponseEntity<Void> respuesta = controller.desactivar(7);

        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        verify(anfitrionService).desactivar(7);
    }

    @Test
    void iniciarSesionResponde200() {
        LoginRequestDTO dto = new LoginRequestDTO("ana@correo.com", "Clave1234");
        LoginResponseDTO login = new LoginResponseDTO(7, "Ana Gómez Rojas", "ana@correo.com", "ANFITRION");
        when(anfitrionService.iniciarSesion(dto)).thenReturn(login);

        ResponseEntity<LoginResponseDTO> respuesta = controller.iniciarSesion(dto);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("ANFITRION", respuesta.getBody().getRol());
    }
}
