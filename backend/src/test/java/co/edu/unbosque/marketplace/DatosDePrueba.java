package co.edu.unbosque.marketplace;

import co.edu.unbosque.marketplace.model.dto.AnfitrionActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.model.dto.RecuperarContrasenaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.RestablecerContrasenaRequestDTO;

/** Arma los DTOs que usan las pruebas, llenándolos con sus setters. */
public final class DatosDePrueba {

    private DatosDePrueba() {}

    public static AnfitrionRequestDTO registro(String cedula, String primerNombre, String segundoNombre,
                                               String primerApellido, String segundoApellido,
                                               String correo, String password) {
        AnfitrionRequestDTO dto = new AnfitrionRequestDTO();
        dto.setCedula(cedula);
        dto.setPrimerNombre(primerNombre);
        dto.setSegundoNombre(segundoNombre);
        dto.setPrimerApellido(primerApellido);
        dto.setSegundoApellido(segundoApellido);
        dto.setCorreo(correo);
        dto.setPassword(password);
        return dto;
    }

    public static AnfitrionActualizarDTO actualizacion(String primerNombre, String segundoNombre,
                                                       String primerApellido, String segundoApellido, String correo) {
        AnfitrionActualizarDTO dto = new AnfitrionActualizarDTO();
        dto.setPrimerNombre(primerNombre);
        dto.setSegundoNombre(segundoNombre);
        dto.setPrimerApellido(primerApellido);
        dto.setSegundoApellido(segundoApellido);
        dto.setCorreo(correo);
        return dto;
    }

    public static LoginRequestDTO login(String correo, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setCorreo(correo);
        dto.setPassword(password);
        return dto;
    }

    public static LoginResponseDTO respuestaLogin(Integer idUsuario, String nombreCompleto, String correo, String rol) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setIdUsuario(idUsuario);
        dto.setNombreCompleto(nombreCompleto);
        dto.setCorreo(correo);
        dto.setRol(rol);
        return dto;
    }

    public static RecuperarContrasenaRequestDTO recuperar(String correo) {
        RecuperarContrasenaRequestDTO dto = new RecuperarContrasenaRequestDTO();
        dto.setCorreo(correo);
        return dto;
    }

    public static RestablecerContrasenaRequestDTO restablecer(String correo, String codigo, String nuevaPassword) {
        RestablecerContrasenaRequestDTO dto = new RestablecerContrasenaRequestDTO();
        dto.setCorreo(correo);
        dto.setCodigo(codigo);
        dto.setNuevaPassword(nuevaPassword);
        return dto;
    }
}
