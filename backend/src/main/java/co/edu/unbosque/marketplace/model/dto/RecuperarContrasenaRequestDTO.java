package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.*;

public class RecuperarContrasenaRequestDTO {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 200, message = "El correo admite máximo 200 caracteres")
    private String correo;

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
