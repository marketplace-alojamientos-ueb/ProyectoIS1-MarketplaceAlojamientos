package co.edu.unbosque.marketplace.modelo.dto;

import jakarta.validation.constraints.*;

/**
 * Datos editables del perfil del anfitrión.
 * La cédula no se puede cambiar y la contraseña se cambia con el flujo de recuperación.
 */
public class AnfitrionActualizarDTO {

    @NotBlank(message = "El primer nombre es obligatorio")
    @Size(max = 50, message = "El primer nombre admite máximo 50 caracteres")
    private String primerNombre;

    @Size(max = 50, message = "El segundo nombre admite máximo 50 caracteres")
    private String segundoNombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 50, message = "El primer apellido admite máximo 50 caracteres")
    private String primerApellido;

    @NotBlank(message = "El segundo apellido es obligatorio")
    @Size(max = 50, message = "El segundo apellido admite máximo 50 caracteres")
    private String segundoApellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 200, message = "El correo admite máximo 200 caracteres")
    private String email;

    public AnfitrionActualizarDTO() {}

    public AnfitrionActualizarDTO(String primerNombre, String segundoNombre, String primerApellido,
                                  String segundoApellido, String email) {
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.email = email;
    }

    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
