package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.*;

/**
 * Nombres, apellidos y correo: los datos personales que comparten los DTOs del anfitrión.
 * Las validaciones solo se aplican a los DTOs que llegan en un body con @Valid (registro y actualización).
 */
public abstract class DatosPersonalesDTO {

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
    private String correo;

    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
