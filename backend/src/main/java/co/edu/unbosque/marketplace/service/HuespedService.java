package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.HuespedRequestDTO;
import co.edu.unbosque.marketplace.model.dto.HuespedResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;

/** Todo lo del huésped (tabla HUESPED): registro, consulta e inicio de sesión. */
public interface HuespedService {

    /** Crea el usuario y su rol de huésped. Responde 409 si la cédula o el correo ya existen. */
    HuespedResponseDTO registrar(HuespedRequestDTO dto);

    HuespedResponseDTO buscarPorId(Integer idUsuario);

    LoginResponseDTO iniciarSesion(LoginRequestDTO dto);
}
