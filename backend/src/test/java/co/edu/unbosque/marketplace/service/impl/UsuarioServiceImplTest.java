package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.RecuperarContrasenaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.RestablecerContrasenaRequestDTO;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import co.edu.unbosque.marketplace.service.CorreoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

import static co.edu.unbosque.marketplace.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Pruebas de la recuperación de contraseña. El repositorio y el envío de correo son simulados. */
class UsuarioServiceImplTest {

    private static final String CORREO = "ana@correo.com";

    private UsuarioRepository usuarioRepository;
    private CorreoService correoService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        correoService = mock(CorreoService.class);

        usuario = new Usuario("1012345678", "Ana", null, "Gómez", "Rojas", CORREO, "hash-viejo");
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
    }

    /** Crea el servicio con los minutos de vigencia indicados (negativo = el código nace vencido). */
    private UsuarioServiceImpl crearServicio(long minutosExpiracion) {
        return new UsuarioServiceImpl(usuarioRepository, correoService, passwordEncoder, minutosExpiracion);
    }

    /** Pide un código y devuelve el que se "envió" por correo. */
    private String pedirCodigo(UsuarioServiceImpl servicio) {
        servicio.solicitarRecuperacion(recuperar(CORREO));
        ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
        verify(correoService).enviarCodigoRecuperacion(eq(CORREO), eq("Ana"), codigo.capture(), anyLong());
        return codigo.getValue();
    }

    // ---------- solicitar recuperación ----------

    @Test
    void solicitarRecuperacionEnviaUnCodigoDe6Digitos() {
        String codigo = pedirCodigo(crearServicio(15));

        assertTrue(codigo.matches("\\d{6}"));
    }

    @Test
    void solicitarRecuperacionConCorreoQueNoExisteNoEnviaNada() {
        when(usuarioRepository.findByCorreo("nadie@correo.com")).thenReturn(Optional.empty());

        crearServicio(15).solicitarRecuperacion(recuperar("nadie@correo.com"));

        verify(correoService, never()).enviarCodigoRecuperacion(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    void solicitarRecuperacionConCuentaDesactivadaNoEnviaNada() {
        usuario.desactivarCuenta();

        crearServicio(15).solicitarRecuperacion(recuperar(CORREO));

        verify(correoService, never()).enviarCodigoRecuperacion(anyString(), anyString(), anyString(), anyLong());
    }

    // ---------- restablecer contraseña ----------

    @Test
    void restablecerConCodigoCorrectoCambiaLaPassword() {
        UsuarioServiceImpl servicio = crearServicio(15);
        String codigo = pedirCodigo(servicio);

        servicio.restablecerContrasena(restablecer(CORREO, codigo, "NuevaClave1"));

        assertTrue(passwordEncoder.matches("NuevaClave1", usuario.getPassword()));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void restablecerSinHaberPedidoCodigoDa400() {
        UsuarioServiceImpl servicio = crearServicio(15);
        RestablecerContrasenaRequestDTO dto = restablecer(CORREO, "123456", "NuevaClave1");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.restablecerContrasena(dto));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void restablecerConCodigoVencidoDa400() {
        UsuarioServiceImpl servicio = crearServicio(-1);
        String codigo = pedirCodigo(servicio);
        RestablecerContrasenaRequestDTO dto = restablecer(CORREO, codigo, "NuevaClave1");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.restablecerContrasena(dto));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void restablecerConCodigoIncorrectoDa400() {
        UsuarioServiceImpl servicio = crearServicio(15);
        pedirCodigo(servicio);
        RestablecerContrasenaRequestDTO dto = restablecer(CORREO, "codigo-malo", "NuevaClave1");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.restablecerContrasena(dto));

        assertEquals("El código es inválido o ya expiró", error.getReason());
    }

    @Test
    void despuesDe5IntentosFallidosElCodigoSeAnula() {
        UsuarioServiceImpl servicio = crearServicio(15);
        String codigo = pedirCodigo(servicio);
        RestablecerContrasenaRequestDTO malo = restablecer(CORREO, "codigo-malo", "NuevaClave1");

        for (int i = 0; i < 4; i++) {
            assertThrows(ResponseStatusException.class, () -> servicio.restablecerContrasena(malo));
        }
        ResponseStatusException quinto = assertThrows(ResponseStatusException.class, () -> servicio.restablecerContrasena(malo));
        assertEquals("Demasiados intentos fallidos, solicita un código nuevo", quinto.getReason());

        // Ni siquiera el código correcto sirve ya
        RestablecerContrasenaRequestDTO bueno = restablecer(CORREO, codigo, "NuevaClave1");
        assertThrows(ResponseStatusException.class, () -> servicio.restablecerContrasena(bueno));
    }

    @Test
    void restablecerSiElUsuarioYaNoExisteDa400() {
        UsuarioServiceImpl servicio = crearServicio(15);
        String codigo = pedirCodigo(servicio);
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.empty());
        RestablecerContrasenaRequestDTO dto = restablecer(CORREO, codigo, "NuevaClave1");

        assertThrows(ResponseStatusException.class, () -> servicio.restablecerContrasena(dto));
        verify(usuarioRepository, never()).save(any());
    }
}
