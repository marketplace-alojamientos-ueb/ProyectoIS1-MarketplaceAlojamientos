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
        Usuario usuario = CuentaUsuario.registrar(usuarioRepository, passwordEncoder, dto);
        return toResponseDTO(anfitrionRepository.save(new Anfitrion(usuario)));
    }

    @Override
    public AnfitrionResponseDTO actualizar(Integer idUsuario, AnfitrionActualizarDTO dto) {
        Anfitrion anfitrion = obtenerAnfitrion(idUsuario);
        CuentaUsuario.actualizarDatos(usuarioRepository, anfitrion.getUsuario(), idUsuario, dto);
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
        return CuentaUsuario.iniciarSesion(
                anfitrionRepository.findByUsuario_Correo(normalizarCorreo(dto.getCorreo())).map(Anfitrion::getUsuario),
                passwordEncoder, dto, "ANFITRION");
    }

    private Anfitrion obtenerAnfitrion(Integer idUsuario) {
        return anfitrionRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Anfitrión no encontrado con id: " + idUsuario));
    }

    static String normalizarCorreo(String correo) {
        return CuentaUsuario.normalizarCorreo(correo);
    }

    private AnfitrionResponseDTO toResponseDTO(Anfitrion anfitrion) {
        return CuentaUsuario.aRespuesta(anfitrion.getUsuario(), new AnfitrionResponseDTO());
    }
}
