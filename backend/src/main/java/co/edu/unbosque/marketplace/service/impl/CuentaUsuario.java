package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.DatosPersonalesDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.model.dto.RegistroUsuarioRequestDTO;
import co.edu.unbosque.marketplace.model.dto.UsuarioResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;
import java.util.Optional;

/**
 * Lógica de cuenta que comparten todos los roles (huésped y anfitrión): registro, actualización de datos,
 * inicio de sesión y armado de la respuesta. Cada servicio de rol la usa y solo agrega lo propio de su rol.
 */
final class CuentaUsuario {

    private CuentaUsuario() {
    }

    /** La cédula y el correo son únicos en toda la plataforma (tabla USUARIO), sin importar el rol. */
    static Usuario registrar(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                             RegistroUsuarioRequestDTO dto) {
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
        return usuarioRepository.save(usuario);
    }

    /** Cambia nombres, apellidos y correo. La cédula y la contraseña no se modifican aquí. */
    static void actualizarDatos(UsuarioRepository usuarioRepository, Usuario usuario, Integer idUsuario,
                                DatosPersonalesDTO dto) {
        String correo = normalizarCorreo(dto.getCorreo());
        if (usuarioRepository.existsByCorreoAndIdUsuarioNot(correo, idUsuario)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con el correo: " + correo);
        }

        usuario.setPrimerNombre(dto.getPrimerNombre().trim());
        usuario.setSegundoNombre(limpiarOpcional(dto.getSegundoNombre()));
        usuario.setPrimerApellido(dto.getPrimerApellido().trim());
        usuario.setSegundoApellido(dto.getSegundoApellido().trim());
        usuario.setCorreo(correo);
        usuarioRepository.save(usuario);
    }

    /**
     * Inicio de sesión. {@code candidato} es el usuario que tiene ese correo en la tabla del rol (vacío si el correo
     * no existe o no es de ese rol). Mismo mensaje en todos los fallos de credenciales, para no revelar qué correos
     * están registrados.
     */
    static LoginResponseDTO iniciarSesion(Optional<Usuario> candidato, PasswordEncoder passwordEncoder,
                                          LoginRequestDTO dto, String rol) {
        Usuario usuario = candidato
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
        respuesta.setRol(rol);
        return respuesta;
    }

    /** Llena la respuesta con los datos de la cuenta (nunca incluye la contraseña). */
    static <T extends UsuarioResponseDTO> T aRespuesta(Usuario u, T dto) {
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

    static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private static String limpiarOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
