package co.edu.unbosque.marketplace.servicio;

import co.edu.unbosque.marketplace.modelo.dto.RecuperarContrasenaRequestDTO;
import co.edu.unbosque.marketplace.modelo.dto.RestablecerContrasenaRequestDTO;

/** Lo común a cualquier usuario (tabla USUARIO), sin importar si es anfitrión, huésped o admin. */
public interface UsuarioService {

    /** Genera un código de 6 dígitos y lo envía al correo, si el correo pertenece a un usuario activo. */
    void solicitarRecuperacion(RecuperarContrasenaRequestDTO dto);

    /** Cambia la contraseña si el código es correcto y sigue vigente. */
    void restablecerContrasena(RestablecerContrasenaRequestDTO dto);
}
