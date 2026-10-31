package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.HuespedActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.HuespedRequestDTO;
import co.edu.unbosque.marketplace.model.dto.HuespedResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoCuenta;
import co.edu.unbosque.marketplace.repository.HuespedRepository;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas del servicio del huésped. Los repositorios son simulados (mock), no se usa la BD. */
class HuespedServiceImplTest {

    private HuespedRepository huespedRepository;
    private UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private HuespedServiceImpl servicio;

    @BeforeEach
    void setUp() {
        huespedRepository = mock(HuespedRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        servicio = new HuespedServiceImpl(huespedRepository, usuarioRepository, passwordEncoder);

        // save() devuelve el mismo objeto que recibe y, como la BD, le asigna el id.
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario usuario = inv.getArgument(0);
            usuario.setIdUsuario(12);
            return usuario;
        });
        when(huespedRepository.save(any(Huesped.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private HuespedRequestDTO crearRegistro(String cedula, String segundoNombre, String correo, String password) {
        HuespedRequestDTO dto = new HuespedRequestDTO();
        dto.setCedula(cedula);
        dto.setPrimerNombre(" Laura ");
        dto.setSegundoNombre(segundoNombre);
        dto.setPrimerApellido("Gómez");
        dto.setSegundoApellido("Pérez");
        dto.setCorreo(correo);
        dto.setPassword(password);
        return dto;
    }

    private HuespedActualizarDTO crearActualizacion(String primerNombre, String segundoNombre,
                                                    String primerApellido, String segundoApellido, String correo) {
        HuespedActualizarDTO dto = new HuespedActualizarDTO();
        dto.setPrimerNombre(primerNombre);
        dto.setSegundoNombre(segundoNombre);
        dto.setPrimerApellido(primerApellido);
        dto.setSegundoApellido(segundoApellido);
        dto.setCorreo(correo);
        return dto;
    }

    private LoginRequestDTO crearLogin(String correo, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setCorreo(correo);
        dto.setPassword(password);
        return dto;
    }

    /** Crea un huésped de ejemplo con contraseña "Clave1234". */
    private Huesped crearHuesped() {
        Usuario usuario = new Usuario("1012345678", "Laura", "Sofía", "Gómez", "Pérez",
                "laura@correo.com", passwordEncoder.encode("Clave1234"));
        usuario.setIdUsuario(12);
        return new Huesped(usuario);
    }

    // ---------- registrar (Escenario 1) ----------

    @Test
    void registrarCreaElHuespedActivoConCorreoEnMinusculasYPasswordCifrada() {
        HuespedRequestDTO dto = crearRegistro("1012345678", "  ", " LAURA@Correo.com ", "Clave1234");

        HuespedResponseDTO respuesta = servicio.registrar(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("laura@correo.com", guardado.getCorreo());
        assertEquals("Laura", guardado.getPrimerNombre());
        assertNull(guardado.getSegundoNombre());
        assertNotEquals("Clave1234", guardado.getPassword());
        assertTrue(passwordEncoder.matches("Clave1234", guardado.getPassword()));
        verify(huespedRepository).save(any(Huesped.class));

        assertEquals(12, respuesta.getIdUsuario());
        assertEquals("1012345678", respuesta.getCedula());
        assertEquals("Laura Gómez Pérez", respuesta.getNombreCompleto());
        assertEquals(EstadoCuenta.ACTIVA, respuesta.getEstadoCuenta());
        assertFalse(respuesta.getRestringido());
    }

    @Test
    void registrarConSegundoNombreLoConserva() {
        HuespedResponseDTO respuesta = servicio.registrar(
                crearRegistro("1012345678", "Sofía", "laura@correo.com", "Clave1234"));

        assertEquals("Sofía", respuesta.getSegundoNombre());
    }

    @Test
    void registrarConCedulaExistenteDa409() {
        when(usuarioRepository.existsByCedula("1012345678")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.registrar(crearRegistro("1012345678", null, "laura@correo.com", "Clave1234")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(huespedRepository, never()).save(any(Huesped.class));
    }

    @Test
    void registrarConCorreoExistenteDa409() {
        when(usuarioRepository.existsByCorreo("laura@correo.com")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.registrar(crearRegistro("1012345678", null, "Laura@Correo.com", "Clave1234")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(huespedRepository, never()).save(any(Huesped.class));
    }

    // ---------- buscar ----------

    @Test
    void buscarPorIdDevuelveElHuespedSinContrasena() {
        when(huespedRepository.findById(12)).thenReturn(Optional.of(crearHuesped()));

        HuespedResponseDTO dto = servicio.buscarPorId(12);

        assertEquals(12, dto.getIdUsuario());
        assertEquals("laura@correo.com", dto.getCorreo());
        assertEquals("Laura Sofía Gómez Pérez", dto.getNombreCompleto());
    }

    @Test
    void buscarPorIdQueNoExisteDa404() {
        when(huespedRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.buscarPorId(99));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    // ---------- listar ----------

    @Test
    void listarTodosDevuelveLosHuespedes() {
        when(huespedRepository.findAll()).thenReturn(List.of(crearHuesped()));

        List<HuespedResponseDTO> lista = servicio.listarTodos();

        assertEquals(1, lista.size());
        assertEquals("Laura Sofía Gómez Pérez", lista.get(0).getNombreCompleto());
    }

    // ---------- actualizar ----------

    @Test
    void actualizarCambiaLosDatosYNormalizaElCorreo() {
        Huesped huesped = crearHuesped();
        when(huespedRepository.findById(12)).thenReturn(Optional.of(huesped));
        HuespedActualizarDTO dto = crearActualizacion(" María ", "  ", "Rojas", " Díaz ", " NUEVO@Correo.com ");

        HuespedResponseDTO respuesta = servicio.actualizar(12, dto);

        assertEquals("María Rojas Díaz", respuesta.getNombreCompleto());
        assertNull(respuesta.getSegundoNombre());
        assertEquals("nuevo@correo.com", respuesta.getCorreo());
        assertEquals("1012345678", respuesta.getCedula()); // la cédula no cambia
        verify(usuarioRepository).save(huesped.getUsuario());
    }

    @Test
    void actualizarConCorreoDeOtroUsuarioDa409() {
        when(huespedRepository.findById(12)).thenReturn(Optional.of(crearHuesped()));
        when(usuarioRepository.existsByCorreoAndIdUsuarioNot("otro@correo.com", 12)).thenReturn(true);
        HuespedActualizarDTO dto = crearActualizacion("Laura", null, "Gómez", "Pérez", "otro@correo.com");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.actualizar(12, dto));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarHuespedQueNoExisteDa404() {
        when(huespedRepository.findById(99)).thenReturn(Optional.empty());
        HuespedActualizarDTO dto = crearActualizacion("Laura", null, "Gómez", "Pérez", "laura@correo.com");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.actualizar(99, dto));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    // ---------- desactivar ----------

    @Test
    void desactivarDejaLaCuentaDesactivada() {
        Huesped huesped = crearHuesped();
        when(huespedRepository.findById(12)).thenReturn(Optional.of(huesped));

        servicio.desactivar(12);

        assertEquals(EstadoCuenta.DESACTIVADA, huesped.getUsuario().getEstadoCuenta());
        verify(usuarioRepository).save(huesped.getUsuario());
    }

    @Test
    void desactivarHuespedQueNoExisteDa404() {
        when(huespedRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.desactivar(99));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ---------- iniciarSesion (Escenario 2) ----------

    @Test
    void iniciarSesionCorrectaDevuelveRolHuesped() {
        when(huespedRepository.findByUsuario_Correo("laura@correo.com")).thenReturn(Optional.of(crearHuesped()));

        LoginResponseDTO respuesta = servicio.iniciarSesion(crearLogin(" LAURA@correo.com ", "Clave1234"));

        assertEquals(12, respuesta.getIdUsuario());
        assertEquals("laura@correo.com", respuesta.getCorreo());
        assertEquals("HUESPED", respuesta.getRol());
    }

    @Test
    void iniciarSesionConPasswordIncorrectaDa401() {
        when(huespedRepository.findByUsuario_Correo("laura@correo.com")).thenReturn(Optional.of(crearHuesped()));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.iniciarSesion(crearLogin("laura@correo.com", "Otra1234")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void iniciarSesionConCorreoQueNoEsDeUnHuespedDa401() {
        when(huespedRepository.findByUsuario_Correo("ana@correo.com")).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.iniciarSesion(crearLogin("ana@correo.com", "Clave1234")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void iniciarSesionConCuentaDesactivadaDa403() {
        Huesped huesped = crearHuesped();
        huesped.getUsuario().desactivarCuenta();
        when(huespedRepository.findByUsuario_Correo("laura@correo.com")).thenReturn(Optional.of(huesped));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.iniciarSesion(crearLogin("laura@correo.com", "Clave1234")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void iniciarSesionConCuentaRestringidaDa403() {
        Huesped huesped = crearHuesped();
        huesped.getUsuario().setRestringido(true);
        when(huespedRepository.findByUsuario_Correo("laura@correo.com")).thenReturn(Optional.of(huesped));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.iniciarSesion(crearLogin("laura@correo.com", "Clave1234")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }
}
