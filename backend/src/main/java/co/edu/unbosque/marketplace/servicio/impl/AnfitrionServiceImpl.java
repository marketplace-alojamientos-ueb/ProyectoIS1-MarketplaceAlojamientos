package co.edu.unbosque.marketplace.servicio.impl;

import co.edu.unbosque.marketplace.modelo.dto.AnfitrionActualizarDTO;
import co.edu.unbosque.marketplace.modelo.dto.AnfitrionRequestDTO;
import co.edu.unbosque.marketplace.modelo.dto.AnfitrionResponseDTO;
import co.edu.unbosque.marketplace.modelo.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.modelo.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.modelo.entidad.Anfitrion;
import co.edu.unbosque.marketplace.modelo.entidad.Usuario;
import co.edu.unbosque.marketplace.repositorio.AnfitrionRepository;
import co.edu.unbosque.marketplace.repositorio.UsuarioRepository;
import co.edu.unbosque.marketplace.servicio.AnfitrionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class AnfitrionServiceImpl implements AnfitrionService {

    private final AnfitrionRepository anfitrionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AnfitrionServiceImpl(AnfitrionRepository anfitrionRepository,
                                UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder) {
        this.anfitrionRepository = anfitrionRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnfitrionResponseDTO> listarTodos() {
        return anfitrionRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AnfitrionResponseDTO buscarPorCedula(Long cedula) {
        return toResponseDTO(obtenerAnfitrion(cedula));
    }

    @Override
    public AnfitrionResponseDTO registrar(AnfitrionRequestDTO dto) {
        String email = normalizarEmail(dto.getEmail());
        if (usuarioRepository.existsById(dto.getCedula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con la cédula: " + dto.getCedula());
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con el correo: " + email);
        }

        Usuario usuario = new Usuario(
                dto.getCedula(),
                dto.getPrimerNombre().trim(),
                limpiarOpcional(dto.getSegundoNombre()),
                dto.getPrimerApellido().trim(),
                dto.getSegundoApellido().trim(),
                email,
                passwordEncoder.encode(dto.getContrasena()));
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return toResponseDTO(anfitrionRepository.save(new Anfitrion(usuarioGuardado)));
    }

    @Override
    public AnfitrionResponseDTO actualizar(Long cedula, AnfitrionActualizarDTO dto) {
        Anfitrion anfitrion = obtenerAnfitrion(cedula);
        String email = normalizarEmail(dto.getEmail());
        if (usuarioRepository.existsByEmailAndCedulaNot(email, cedula)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con el correo: " + email);
        }

        Usuario usuario = anfitrion.getUsuario();
        usuario.setPrimerNombre(dto.getPrimerNombre().trim());
        usuario.setSegundoNombre(limpiarOpcional(dto.getSegundoNombre()));
        usuario.setPrimerApellido(dto.getPrimerApellido().trim());
        usuario.setSegundoApellido(dto.getSegundoApellido().trim());
        usuario.setEmail(email);
        usuarioRepository.save(usuario);
        return toResponseDTO(anfitrion);
    }

    @Override
    public void desactivar(Long cedula) {
        Usuario usuario = obtenerAnfitrion(cedula).getUsuario();
        usuario.desactivarCuenta();
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO iniciarSesion(LoginRequestDTO dto) {
        // Mismo mensaje si el correo no existe o la contraseña está mal, para no revelar qué correos existen.
        Usuario usuario = anfitrionRepository.findByUsuario_Email(normalizarEmail(dto.getEmail()))
                .map(Anfitrion::getUsuario)
                .filter(u -> passwordEncoder.matches(dto.getContrasena(), u.getContrasena()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (!usuario.estaActiva()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está desactivada");
        }
        if (Boolean.TRUE.equals(usuario.getRestringido())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está restringida por un administrador");
        }
        return new LoginResponseDTO(usuario.getCedula(), usuario.getNombreCompleto(), usuario.getEmail(), "ANFITRION");
    }

    private Anfitrion obtenerAnfitrion(Long cedula) {
        return anfitrionRepository.findById(cedula)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Anfitrión no encontrado con cédula: " + cedula));
    }

    static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String limpiarOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private AnfitrionResponseDTO toResponseDTO(Anfitrion anfitrion) {
        Usuario u = anfitrion.getUsuario();
        AnfitrionResponseDTO dto = new AnfitrionResponseDTO();
        dto.setCedula(u.getCedula());
        dto.setPrimerNombre(u.getPrimerNombre());
        dto.setSegundoNombre(u.getSegundoNombre());
        dto.setPrimerApellido(u.getPrimerApellido());
        dto.setSegundoApellido(u.getSegundoApellido());
        dto.setNombreCompleto(u.getNombreCompleto());
        dto.setEmail(u.getEmail());
        dto.setEstadoCuenta(u.getEstadoCuenta());
        dto.setRestringido(u.getRestringido());
        return dto;
    }
}
