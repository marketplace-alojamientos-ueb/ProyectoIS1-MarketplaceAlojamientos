package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.AnfitrionActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import java.util.List;

/** Todo lo del anfitrión (tabla ANFITRION): CRUD e inicio de sesión. */
public interface AnfitrionService {

    List<AnfitrionResponseDTO> listarTodos();

    AnfitrionResponseDTO buscarPorId(Integer idUsuario);

    /** Crea el usuario y su rol de anfitrión. Responde 409 si la cédula o el correo ya existen. */
    AnfitrionResponseDTO registrar(AnfitrionRequestDTO dto);

    AnfitrionResponseDTO actualizar(Integer idUsuario, AnfitrionActualizarDTO dto);

    /** Borrado lógico: deja la cuenta en estado DESACTIVADA. */
    void desactivar(Integer idUsuario);

    LoginResponseDTO iniciarSesion(LoginRequestDTO dto);
}
