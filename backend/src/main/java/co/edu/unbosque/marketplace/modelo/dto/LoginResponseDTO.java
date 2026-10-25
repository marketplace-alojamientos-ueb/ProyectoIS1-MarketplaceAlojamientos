package co.edu.unbosque.marketplace.modelo.dto;

/**
 * Respuesta de un inicio de sesión correcto.
 * Provisional: todavía no hay token (JWT); el front guarda estos datos para saber quién inició sesión.
 */
public class LoginResponseDTO {

    private Long cedula;
    private String nombreCompleto;
    private String email;
    private String rol;

    public LoginResponseDTO() {}

    public LoginResponseDTO(Long cedula, String nombreCompleto, String email, String rol) {
        this.cedula = cedula;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
    }

    public Long getCedula() { return cedula; }
    public void setCedula(Long cedula) { this.cedula = cedula; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
