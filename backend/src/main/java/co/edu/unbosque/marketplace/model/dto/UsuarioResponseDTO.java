package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.EstadoCuenta;

/**
 * Datos de una cuenta que el backend devuelve al front (nunca incluye la contraseña).
 * Nombres, apellidos y correo se heredan de DatosPersonalesDTO.
 * Es la base común de las respuestas de cada rol (huésped y anfitrión).
 */
public class UsuarioResponseDTO extends DatosPersonalesDTO {

    private Integer idUsuario;
    private String cedula;
    private String nombreCompleto;
    private EstadoCuenta estadoCuenta;
    private Boolean restringido;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public EstadoCuenta getEstadoCuenta() { return estadoCuenta; }
    public void setEstadoCuenta(EstadoCuenta estadoCuenta) { this.estadoCuenta = estadoCuenta; }

    public Boolean getRestringido() { return restringido; }
    public void setRestringido(Boolean restringido) { this.restringido = restringido; }
}
