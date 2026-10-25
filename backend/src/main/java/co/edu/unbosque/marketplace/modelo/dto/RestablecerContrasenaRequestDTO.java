package co.edu.unbosque.marketplace.modelo.dto;

import jakarta.validation.constraints.*;

public class RestablecerContrasenaRequestDTO {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 200, message = "El correo admite máximo 200 caracteres")
    private String email;

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El código debe tener 6 dígitos")
    private String codigo;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe tener al menos una letra y un número")
    private String nuevaContrasena;

    public RestablecerContrasenaRequestDTO() {}

    public RestablecerContrasenaRequestDTO(String email, String codigo, String nuevaContrasena) {
        this.email = email;
        this.codigo = codigo;
        this.nuevaContrasena = nuevaContrasena;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNuevaContrasena() { return nuevaContrasena; }
    public void setNuevaContrasena(String nuevaContrasena) { this.nuevaContrasena = nuevaContrasena; }
}
