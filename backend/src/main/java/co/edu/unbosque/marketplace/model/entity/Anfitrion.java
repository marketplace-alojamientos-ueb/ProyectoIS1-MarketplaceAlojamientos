package co.edu.unbosque.marketplace.model.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Rol de anfitrión (tabla ANFITRION). Comparte el id_usuario con su Usuario.
 * Las publicaciones del anfitrión se agregarán cuando exista la entidad Publicacion.
 */
@Entity
@Table(name = "anfitrion")
public class Anfitrion {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    public Anfitrion() {}

    public Anfitrion(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Anfitrion otro)) return false;
        return idUsuario != null && idUsuario.equals(otro.idUsuario);
    }

    @Override
    public int hashCode() { return Objects.hashCode(idUsuario); }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
