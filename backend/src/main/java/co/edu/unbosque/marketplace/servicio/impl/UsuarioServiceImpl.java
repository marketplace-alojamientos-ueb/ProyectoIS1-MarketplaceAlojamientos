package co.edu.unbosque.marketplace.servicio.impl;

import co.edu.unbosque.marketplace.modelo.dto.RecuperarContrasenaRequestDTO;
import co.edu.unbosque.marketplace.modelo.dto.RestablecerContrasenaRequestDTO;
import co.edu.unbosque.marketplace.modelo.entidad.Usuario;
import co.edu.unbosque.marketplace.repositorio.UsuarioRepository;
import co.edu.unbosque.marketplace.servicio.CorreoService;
import co.edu.unbosque.marketplace.servicio.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Recuperación de contraseña. Como en la BD no hay tabla para los códigos, se guardan en memoria
 * (un Map correo -> código) mientras el backend esté encendido; si se reinicia, hay que pedir otro código.
 */
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private static final int MAX_INTENTOS_CODIGO = 5;
    private static final String MENSAJE_CODIGO_INVALIDO = "El código es inválido o ya expiró";

    private final UsuarioRepository usuarioRepository;
    private final CorreoService correoService;
    private final PasswordEncoder passwordEncoder;
    private final long minutosExpiracion;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, CodigoRecuperacion> codigosPorEmail = new ConcurrentHashMap<>();

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              CorreoService correoService,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.recuperacion.minutos-expiracion:15}") long minutosExpiracion) {
        this.usuarioRepository = usuarioRepository;
        this.correoService = correoService;
        this.passwordEncoder = passwordEncoder;
        this.minutosExpiracion = minutosExpiracion;
    }

    @Override
    @Transactional(readOnly = true)
    public void solicitarRecuperacion(RecuperarContrasenaRequestDTO dto) {
        // Si el correo no es de un usuario activo no se hace nada, para no revelar qué correos existen.
        String email = AnfitrionServiceImpl.normalizarEmail(dto.getEmail());
        usuarioRepository.findByEmail(email)
                .filter(Usuario::estaActiva)
                .ifPresent(usuario -> {
                    String codigo = String.format("%06d", random.nextInt(1_000_000));
                    // Guardar uno nuevo reemplaza el anterior del mismo correo.
                    codigosPorEmail.put(email, new CodigoRecuperacion(codigo, LocalDateTime.now().plusMinutes(minutosExpiracion)));
                    correoService.enviarCodigoRecuperacion(usuario.getEmail(), usuario.getPrimerNombre(), codigo, minutosExpiracion);
                });
    }

    @Override
    public void restablecerContrasena(RestablecerContrasenaRequestDTO dto) {
        String email = AnfitrionServiceImpl.normalizarEmail(dto.getEmail());
        CodigoRecuperacion guardado = codigosPorEmail.get(email);

        if (guardado == null || LocalDateTime.now().isAfter(guardado.expiracion)) {
            codigosPorEmail.remove(email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MENSAJE_CODIGO_INVALIDO);
        }
        if (!guardado.codigo.equals(dto.getCodigo())) {
            guardado.intentos++;
            if (guardado.intentos >= MAX_INTENTOS_CODIGO) {
                codigosPorEmail.remove(email);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Demasiados intentos fallidos, solicita un código nuevo");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MENSAJE_CODIGO_INVALIDO);
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, MENSAJE_CODIGO_INVALIDO));
        usuario.setContrasena(passwordEncoder.encode(dto.getNuevaContrasena()));
        usuarioRepository.save(usuario);
        codigosPorEmail.remove(email);
    }

    /** Código enviado al correo, su fecha de vencimiento y los intentos fallidos. */
    private static class CodigoRecuperacion {
        private final String codigo;
        private final LocalDateTime expiracion;
        private int intentos = 0;

        CodigoRecuperacion(String codigo, LocalDateTime expiracion) {
            this.codigo = codigo;
            this.expiracion = expiracion;
        }
    }
}
