package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.AnfitrionActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import co.edu.unbosque.marketplace.service.AnfitrionService;
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
    public AnfitrionResponseDTO buscarPorId(Integer idUsuario) {
        return toResponseDTO(obtenerAnfitrion(idUsuario));
    }

    @Override
    public AnfitrionResponseDTO registrar(AnfitrionRequestDTO dto) {
        String correo = normalizarCorreo(dto.getCorreo());
        if (usuarioRepository.existsByCedula(dto.getCedula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con esa cédula");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con el correo: " + correo);
        }

        Usuario usuario = new Usuario(
                dto.getCedula(),
                dto.getPrimerNombre().trim(),
                limpiarOpcional(dto.getSegundoNombre()),
                dto.getPrimerApellido().trim(),
                dto.getSegundoApellido().trim(),
                correo,
                passwordEncoder.encode(dto.getPassword()));
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return toResponseDTO(anfitrionRepository.save(new Anfitrion(usuarioGuardado)));
    }

    @Override
    public AnfitrionResponseDTO actualizar(Integer idUsuario, AnfitrionActualizarDTO dto) {
        Anfitrion anfitrion = obtenerAnfitrion(idUsuario);
        String correo = normalizarCorreo(dto.getCorreo());
        if (usuarioRepository.existsByCorreoAndIdUsuarioNot(correo, idUsuario)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con el correo: " + correo);
        }

        Usuario usuario = anfitrion.getUsuario();
        usuario.setPrimerNombre(dto.getPrimerNombre().trim());
        usuario.setSegundoNombre(limpiarOpcional(dto.getSegundoNombre()));
        usuario.setPrimerApellido(dto.getPrimerApellido().trim());
        usuario.setSegundoApellido(dto.getSegundoApellido().trim());
        usuario.setCorreo(correo);
        usuarioRepository.save(usuario);
        return toResponseDTO(anfitrion);
    }

    @Override
    public void desactivar(Integer idUsuario) {
        Usuario usuario = obtenerAnfitrion(idUsuario).getUsuario();
        usuario.desactivarCuenta();
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO iniciarSesion(LoginRequestDTO dto) {
        // Mismo mensaje si el correo no existe o la contraseña está mal, para no revelar qué correos existen.
        Usuario usuario = anfitrionRepository.findByUsuario_Correo(normalizarCorreo(dto.getCorreo()))
                .map(Anfitrion::getUsuario)
                .filter(u -> passwordEncoder.matches(dto.getPassword(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (!usuario.estaActiva()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está desactivada");
        }
        if (Boolean.TRUE.equals(usuario.getRestringido())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está restringida por un administrador");
        }
        return new LoginResponseDTO(usuario.getIdUsuario(), usuario.getNombreCompleto(), usuario.getCorreo(), "ANFITRION");
    }

    private Anfitrion obtenerAnfitrion(Integer idUsuario) {
        return anfitrionRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Anfitrión no encontrado con id: " + idUsuario));
    }

    static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private static String limpiarOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private AnfitrionResponseDTO toResponseDTO(Anfitrion anfitrion) {
        Usuario u = anfitrion.getUsuario();
        AnfitrionResponseDTO dto = new AnfitrionResponseDTO();
        dto.setIdUsuario(u.getIdUsuario());
        dto.setCedula(u.getCedula());
        dto.setPrimerNombre(u.getPrimerNombre());
        dto.setSegundoNombre(u.getSegundoNombre());
        dto.setPrimerApellido(u.getPrimerApellido());
        dto.setSegundoApellido(u.getSegundoApellido());
        dto.setNombreCompleto(u.getNombreCompleto());
        dto.setCorreo(u.getCorreo());
        dto.setEstadoCuenta(u.getEstadoCuenta());
        dto.setRestringido(u.getRestringido());
        return dto;
    }
}
