package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.HuespedActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.HuespedRequestDTO;
import co.edu.unbosque.marketplace.model.dto.HuespedResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import java.util.List;

/** Todo lo del huésped (tabla HUESPED): CRUD e inicio de sesión. */
public interface HuespedService {

    List<HuespedResponseDTO> listarTodos();

    /** Crea el usuario y su rol de huésped. Responde 409 si la cédula o el correo ya existen. */
    HuespedResponseDTO registrar(HuespedRequestDTO dto);

    HuespedResponseDTO buscarPorId(Integer idUsuario);

    HuespedResponseDTO actualizar(Integer idUsuario, HuespedActualizarDTO dto);

    /** Borrado lógico: deja la cuenta en estado DESACTIVADA. */
    void desactivar(Integer idUsuario);

    LoginResponseDTO iniciarSesion(LoginRequestDTO dto);
}
