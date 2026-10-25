package co.edu.unbosque.marketplace.servicio;

import co.edu.unbosque.marketplace.modelo.dto.AnfitrionActualizarDTO;
import co.edu.unbosque.marketplace.modelo.dto.AnfitrionRequestDTO;
import co.edu.unbosque.marketplace.modelo.dto.AnfitrionResponseDTO;
import co.edu.unbosque.marketplace.modelo.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.modelo.dto.LoginResponseDTO;
import java.util.List;

/** Todo lo del anfitrión (tabla ANFITRION): CRUD e inicio de sesión. */
public interface AnfitrionService {

    List<AnfitrionResponseDTO> listarTodos();

    AnfitrionResponseDTO buscarPorCedula(Long cedula);

    /** Crea el usuario y su rol de anfitrión. Responde 409 si la cédula o el correo ya existen. */
    AnfitrionResponseDTO registrar(AnfitrionRequestDTO dto);

    AnfitrionResponseDTO actualizar(Long cedula, AnfitrionActualizarDTO dto);

    /** Borrado lógico: deja la cuenta en estado DESACTIVADA. */
    void desactivar(Long cedula);

    LoginResponseDTO iniciarSesion(LoginRequestDTO dto);
}
