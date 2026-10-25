package co.edu.unbosque.marketplace.modelo.dto;

import jakarta.validation.constraints.*;

public class RecuperarContrasenaRequestDTO {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 200, message = "El correo admite máximo 200 caracteres")
    private String email;

    public RecuperarContrasenaRequestDTO() {}

    public RecuperarContrasenaRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
