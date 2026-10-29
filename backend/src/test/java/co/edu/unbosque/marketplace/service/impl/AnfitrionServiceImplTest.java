package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.AnfitrionActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoCuenta;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas del servicio del anfitrión. Los repositorios son simulados (mock), no se usa la BD. */
class AnfitrionServiceImplTest {

    private AnfitrionRepository anfitrionRepository;
    private UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AnfitrionServiceImpl servicio;

    @BeforeEach
    void setUp() {
        anfitrionRepository = mock(AnfitrionRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        servicio = new AnfitrionServiceImpl(anfitrionRepository, usuarioRepository, passwordEncoder);

        // save() devuelve el mismo objeto que recibe, como haría la BD
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(anfitrionRepository.save(any(Anfitrion.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /** Crea un anfitrión de ejemplo con contraseña "Clave1234". */
    private Anfitrion crearAnfitrion() {
        Usuario usuario = new Usuario("1012345678", "Ana", "María", "Gómez", "Rojas",
                "ana@correo.com", passwordEncoder.encode("Clave1234"));
        usuario.setIdUsuario(7);
        return new Anfitrion(usuario);
    }

    // ---------- listar y buscar ----------

    @Test
    void listarTodosDevuelveLosAnfitriones() {
        when(anfitrionRepository.findAll()).thenReturn(List.of(crearAnfitrion()));

        List<AnfitrionResponseDTO> lista = servicio.listarTodos();

        assertEquals(1, lista.size());
        assertEquals("Ana María Gómez Rojas", lista.get(0).getNombreCompleto());
    }

    @Test
    void buscarPorIdDevuelveElAnfitrion() {
        when(anfitrionRepository.findById(7)).thenReturn(Optional.of(crearAnfitrion()));

        AnfitrionResponseDTO dto = servicio.buscarPorId(7);

        assertEquals(7, dto.getIdUsuario());
        assertEquals("1012345678", dto.getCedula());
        assertEquals("ana@correo.com", dto.getCorreo());
        assertEquals(EstadoCuenta.ACTIVA, dto.getEstadoCuenta());
    }

    @Test
    void buscarPorIdQueNoExisteDa404() {
        when(anfitrionRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.buscarPorId(99));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    // ---------- registrar ----------

    @Test
    void registrarGuardaElAnfitrionConCorreoEnMinusculasYPasswordCifrada() {
        AnfitrionRequestDTO dto = new AnfitrionRequestDTO("1012345678", " Ana ", "  ", "Gómez", "Rojas",
                " ANA@Correo.com ", "Clave1234");

        AnfitrionResponseDTO respuesta = servicio.registrar(dto);

        assertEquals("ana@correo.com", respuesta.getCorreo());
        assertEquals("Ana", respuesta.getPrimerNombre());
        assertNull(respuesta.getSegundoNombre()); // el segundo nombre vacío se guarda como null
        verify(usuarioRepository).save(argThat(u -> passwordEncoder.matches("Clave1234", u.getPassword())));
    }

    @Test
    void registrarConCedulaRepetidaDa409() {
        when(usuarioRepository.existsByCedula("1012345678")).thenReturn(true);
        AnfitrionRequestDTO dto = new AnfitrionRequestDTO("1012345678", "Ana", null, "Gómez", "Rojas",
                "ana@correo.com", "Clave1234");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.registrar(dto));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test
    void registrarConCorreoRepetidoDa409() {
        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(true);
        AnfitrionRequestDTO dto = new AnfitrionRequestDTO("1012345678", "Ana", null, "Gómez", "Rojas",
                "ana@correo.com", "Clave1234");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.registrar(dto));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    // ---------- actualizar ----------

    @Test
    void actualizarCambiaLosDatos() {
        when(anfitrionRepository.findById(7)).thenReturn(Optional.of(crearAnfitrion()));
        AnfitrionActualizarDTO dto = new AnfitrionActualizarDTO("Laura", null, "Pérez", "Díaz", "laura@correo.com");

        AnfitrionResponseDTO respuesta = servicio.actualizar(7, dto);

        assertEquals("Laura Pérez Díaz", respuesta.getNombreCompleto());
        assertEquals("laura@correo.com", respuesta.getCorreo());
    }

    @Test
    void actualizarConCorreoDeOtroUsuarioDa409() {
        when(anfitrionRepository.findById(7)).thenReturn(Optional.of(crearAnfitrion()));
        when(usuarioRepository.existsByCorreoAndIdUsuarioNot("otro@correo.com", 7)).thenReturn(true);
        AnfitrionActualizarDTO dto = new AnfitrionActualizarDTO("Ana", null, "Gómez", "Rojas", "otro@correo.com");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.actualizar(7, dto));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    // ---------- desactivar ----------

    @Test
    void desactivarDejaLaCuentaDesactivada() {
        Anfitrion anfitrion = crearAnfitrion();
        when(anfitrionRepository.findById(7)).thenReturn(Optional.of(anfitrion));

        servicio.desactivar(7);

        assertEquals(EstadoCuenta.DESACTIVADA, anfitrion.getUsuario().getEstadoCuenta());
    }

    // ---------- iniciar sesión ----------

    @Test
    void iniciarSesionConDatosCorrectos() {
        when(anfitrionRepository.findByUsuario_Correo("ana@correo.com")).thenReturn(Optional.of(crearAnfitrion()));

        LoginResponseDTO respuesta = servicio.iniciarSesion(new LoginRequestDTO("ana@correo.com", "Clave1234"));

        assertEquals(7, respuesta.getIdUsuario());
        assertEquals("ANFITRION", respuesta.getRol());
    }

    @Test
    void iniciarSesionConPasswordIncorrectaDa401() {
        when(anfitrionRepository.findByUsuario_Correo("ana@correo.com")).thenReturn(Optional.of(crearAnfitrion()));
        LoginRequestDTO dto = new LoginRequestDTO("ana@correo.com", "OtraClave1");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.iniciarSesion(dto));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void iniciarSesionConCorreoQueNoExisteDa401() {
        when(anfitrionRepository.findByUsuario_Correo("nadie@correo.com")).thenReturn(Optional.empty());
        LoginRequestDTO dto = new LoginRequestDTO("nadie@correo.com", "Clave1234");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.iniciarSesion(dto));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void iniciarSesionConCuentaDesactivadaDa403() {
        Anfitrion anfitrion = crearAnfitrion();
        anfitrion.getUsuario().desactivarCuenta();
        when(anfitrionRepository.findByUsuario_Correo("ana@correo.com")).thenReturn(Optional.of(anfitrion));
        LoginRequestDTO dto = new LoginRequestDTO("ana@correo.com", "Clave1234");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.iniciarSesion(dto));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void iniciarSesionConCuentaRestringidaDa403() {
        Anfitrion anfitrion = crearAnfitrion();
        anfitrion.getUsuario().setRestringido(true);
        when(anfitrionRepository.findByUsuario_Correo("ana@correo.com")).thenReturn(Optional.of(anfitrion));
        LoginRequestDTO dto = new LoginRequestDTO("ana@correo.com", "Clave1234");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.iniciarSesion(dto));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }
}
