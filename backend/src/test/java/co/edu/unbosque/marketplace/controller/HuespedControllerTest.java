package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.HuespedActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.HuespedRequestDTO;
import co.edu.unbosque.marketplace.model.dto.HuespedResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.service.HuespedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas del controller: revisa que llame al servicio y responda con el código HTTP correcto. */
class HuespedControllerTest {

    private HuespedService huespedService;
    private HuespedController controller;
    private final HuespedResponseDTO huesped = new HuespedResponseDTO();

    @BeforeEach
    void setUp() {
        huespedService = mock(HuespedService.class);
        controller = new HuespedController(huespedService);
        huesped.setIdUsuario(12);
    }

    @Test
    void listarTodosResponde200() {
        when(huespedService.listarTodos()).thenReturn(List.of(huesped));

        ResponseEntity<List<HuespedResponseDTO>> respuesta = controller.listarTodos();

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, respuesta.getBody().size());
    }

    @Test
    void registrarResponde201() {
        HuespedRequestDTO dto = new HuespedRequestDTO();
        when(huespedService.registrar(dto)).thenReturn(huesped);

        ResponseEntity<HuespedResponseDTO> respuesta = controller.registrar(dto);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(huesped, respuesta.getBody());
    }

    @Test
    void buscarPorIdResponde200() {
        when(huespedService.buscarPorId(12)).thenReturn(huesped);

        ResponseEntity<HuespedResponseDTO> respuesta = controller.buscarPorId(12);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(12, respuesta.getBody().getIdUsuario());
    }

    @Test
    void actualizarResponde200() {
        HuespedActualizarDTO dto = new HuespedActualizarDTO();
        when(huespedService.actualizar(12, dto)).thenReturn(huesped);

        ResponseEntity<HuespedResponseDTO> respuesta = controller.actualizar(12, dto);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(huesped, respuesta.getBody());
    }

    @Test
    void desactivarResponde204() {
        ResponseEntity<Void> respuesta = controller.desactivar(12);

        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        verify(huespedService).desactivar(12);
    }

    @Test
    void iniciarSesionResponde200() {
        LoginRequestDTO dto = new LoginRequestDTO();
        LoginResponseDTO login = new LoginResponseDTO();
        login.setRol("HUESPED");
        when(huespedService.iniciarSesion(dto)).thenReturn(login);

        ResponseEntity<LoginResponseDTO> respuesta = controller.iniciarSesion(dto);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("HUESPED", respuesta.getBody().getRol());
    }
}
