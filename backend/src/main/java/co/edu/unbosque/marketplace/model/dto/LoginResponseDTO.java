package co.edu.unbosque.marketplace.model.dto;

/**
 * Respuesta de un inicio de sesión correcto.
 * Provisional: todavía no hay token (JWT); el front guarda estos datos para saber quién inició sesión.
 */
public class LoginResponseDTO {

    private Integer idUsuario;
    private String nombreCompleto;
    private String correo;
    private String rol;

    public LoginResponseDTO() {}

    public LoginResponseDTO(Integer idUsuario, String nombreCompleto, String correo, String rol) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.rol = rol;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
