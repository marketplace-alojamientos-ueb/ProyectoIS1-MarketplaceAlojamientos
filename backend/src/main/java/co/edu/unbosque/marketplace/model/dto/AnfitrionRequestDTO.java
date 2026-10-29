package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.*;

/**
 * Datos que envía el front para registrar un anfitrión nuevo.
 * Nombres, apellidos y correo (con sus validaciones) se heredan de DatosPersonalesDTO.
 */
public class AnfitrionRequestDTO extends DatosPersonalesDTO {

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^\\d{6,10}$", message = "La cédula debe tener entre 6 y 10 dígitos, solo números")
    private String cedula;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe tener al menos una letra y un número")
    private String password;

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
