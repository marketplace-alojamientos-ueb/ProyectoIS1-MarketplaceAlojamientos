package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.EstadoCuenta;

/**
 * Datos del huésped que el backend devuelve al front (nunca incluye la contraseña).
 * Nombres, apellidos y correo se heredan de DatosPersonalesDTO.
 */
public class HuespedResponseDTO extends DatosPersonalesDTO {

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
