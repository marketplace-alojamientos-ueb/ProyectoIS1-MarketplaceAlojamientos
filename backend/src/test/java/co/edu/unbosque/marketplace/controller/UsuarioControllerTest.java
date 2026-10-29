package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.RecuperarContrasenaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.RestablecerContrasenaRequestDTO;
import co.edu.unbosque.marketplace.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static co.edu.unbosque.marketplace.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas del controller de recuperación de contraseña. */
class UsuarioControllerTest {

    private UsuarioService usuarioService;
    private UsuarioController controller;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        controller = new UsuarioController(usuarioService);
    }

    @Test
    void solicitarRecuperacionResponde200ConMensaje() {
        RecuperarContrasenaRequestDTO dto = recuperar("ana@correo.com");

        ResponseEntity<MensajeResponseDTO> respuesta = controller.solicitarRecuperacion(dto);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody().getMensaje());
        verify(usuarioService).solicitarRecuperacion(dto);
    }

    @Test
    void restablecerContrasenaResponde200ConMensaje() {
        RestablecerContrasenaRequestDTO dto = restablecer("ana@correo.com", "123456", "NuevaClave1");

        ResponseEntity<MensajeResponseDTO> respuesta = controller.restablecerContrasena(dto);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Contraseña actualizada correctamente", respuesta.getBody().getMensaje());
        verify(usuarioService).restablecerContrasena(dto);
    }
}
