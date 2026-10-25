package co.edu.unbosque.marketplace.modelo.entidad;

import jakarta.persistence.*;

/**
 * Rol de anfitrión (tabla ANFITRION). Comparte la cédula con su Usuario.
 * Las publicaciones del anfitrión se agregarán cuando exista la entidad Publicacion.
 */
@Entity
@Table(name = "anfitrion")
public class Anfitrion {

    @Id
    @Column(name = "cedula")
    private Long cedula;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @MapsId
    @JoinColumn(name = "cedula")
    private Usuario usuario;

    public Anfitrion() {}

    public Anfitrion(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getCedula() { return cedula; }
    public void setCedula(Long cedula) { this.cedula = cedula; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
