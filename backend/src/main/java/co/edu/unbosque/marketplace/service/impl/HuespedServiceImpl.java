package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.HuespedActualizarDTO;
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
import java.util.List;

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
    @Transactional(readOnly = true)
    public List<HuespedResponseDTO> listarTodos() {
        return huespedRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public HuespedResponseDTO registrar(HuespedRequestDTO dto) {
        Usuario usuario = CuentaUsuario.registrar(usuarioRepository, passwordEncoder, dto);
        return toResponseDTO(huespedRepository.save(new Huesped(usuario)));
    }

    @Override
    @Transactional(readOnly = true)
    public HuespedResponseDTO buscarPorId(Integer idUsuario) {
        return toResponseDTO(obtenerHuesped(idUsuario));
    }

    @Override
    public HuespedResponseDTO actualizar(Integer idUsuario, HuespedActualizarDTO dto) {
        Huesped huesped = obtenerHuesped(idUsuario);
        CuentaUsuario.actualizarDatos(usuarioRepository, huesped.getUsuario(), idUsuario, dto);
        return toResponseDTO(huesped);
    }

    @Override
    public void desactivar(Integer idUsuario) {
        Usuario usuario = obtenerHuesped(idUsuario).getUsuario();
        usuario.desactivarCuenta();
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO iniciarSesion(LoginRequestDTO dto) {
        return CuentaUsuario.iniciarSesion(
                huespedRepository.findByUsuario_Correo(CuentaUsuario.normalizarCorreo(dto.getCorreo()))
                        .map(Huesped::getUsuario),
                passwordEncoder, dto, "HUESPED");
    }

    private Huesped obtenerHuesped(Integer idUsuario) {
        return huespedRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Huésped no encontrado con id: " + idUsuario));
    }

    private HuespedResponseDTO toResponseDTO(Huesped huesped) {
        return CuentaUsuario.aRespuesta(huesped.getUsuario(), new HuespedResponseDTO());
    }
}
