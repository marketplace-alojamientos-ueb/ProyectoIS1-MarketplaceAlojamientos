package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.HuespedRequestDTO;
import co.edu.unbosque.marketplace.model.dto.HuespedResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.repository.HuespedRepository;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import co.edu.unbosque.marketplace.service.HuespedService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@Service
@Transactional
public class HuespedServiceImpl implements HuespedService {

    private final HuespedRepository huespedRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public HuespedServiceImpl(HuespedRepository huespedRepository,
                              UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder) {
        this.huespedRepository = huespedRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public HuespedResponseDTO registrar(HuespedRequestDTO dto) {
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

        return toResponseDTO(huespedRepository.save(new Huesped(usuarioGuardado)));
    }

    @Override
    @Transactional(readOnly = true)
    public HuespedResponseDTO buscarPorId(Integer idUsuario) {
        return toResponseDTO(huespedRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Huésped no encontrado con id: " + idUsuario)));
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO iniciarSesion(LoginRequestDTO dto) {
        Usuario usuario = huespedRepository.findByUsuario_Correo(normalizarCorreo(dto.getCorreo()))
                .map(Huesped::getUsuario)
                .filter(u -> passwordEncoder.matches(dto.getPassword(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (!usuario.estaActiva()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está desactivada");
        }
        if (Boolean.TRUE.equals(usuario.getRestringido())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está restringida por un administrador");
        }
        LoginResponseDTO respuesta = new LoginResponseDTO();
        respuesta.setIdUsuario(usuario.getIdUsuario());
        respuesta.setNombreCompleto(usuario.getNombreCompleto());
        respuesta.setCorreo(usuario.getCorreo());
        respuesta.setRol("HUESPED");
        return respuesta;
    }

    private static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private static String limpiarOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private HuespedResponseDTO toResponseDTO(Huesped huesped) {
        Usuario u = huesped.getUsuario();
        HuespedResponseDTO dto = new HuespedResponseDTO();
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
