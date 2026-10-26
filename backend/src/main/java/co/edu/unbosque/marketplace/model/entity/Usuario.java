package co.edu.unbosque.marketplace.model.entity;

import co.edu.unbosque.marketplace.model.enums.EstadoCuenta;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * Datos comunes de cualquier usuario de la plataforma (tabla USUARIO).
 * Anfitrion, Huesped y Admin se enlazan a esta tabla por el id_usuario.
 * Los atributos usan los nombres del diagrama de clases; @Column indica la columna real de la BD.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "cedula", nullable = false, length = 10, unique = true)
    private String cedula;

    @Column(name = "primer_nombre", nullable = false, length = 50)
    private String primerNombre;

    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @Column(name = "primer_apellido", nullable = false, length = 50)
    private String primerApellido;

    @Column(name = "segundo_apellido", nullable = false, length = 50)
    private String segundoApellido;

    @Column(name = "email", nullable = false, length = 200, unique = true)
    private String correo;

    /** Hash BCrypt de la contraseña, nunca la contraseña en texto plano. */
    @Column(name = "contrasena", nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cuenta", nullable = false, length = 20)
    private EstadoCuenta estadoCuenta = EstadoCuenta.ACTIVA;

    @Column(name = "restringido", nullable = false)
    private Boolean restringido = false;

    public Usuario() {}

    public Usuario(String cedula, String primerNombre, String segundoNombre, String primerApellido,
                   String segundoApellido, String correo, String password) {
        this.cedula = cedula;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.correo = correo;
        this.password = password;
    }

    public String getNombreCompleto() {
        String segundo = (segundoNombre != null && !segundoNombre.isBlank()) ? " " + segundoNombre : "";
        return primerNombre + segundo + " " + primerApellido + " " + segundoApellido;
    }

    public void desactivarCuenta() { this.estadoCuenta = EstadoCuenta.DESACTIVADA; }

    public void activarCuenta() { this.estadoCuenta = EstadoCuenta.ACTIVA; }

    public boolean estaActiva() { return estadoCuenta == EstadoCuenta.ACTIVA; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Usuario otro)) return false;
        return idUsuario != null && idUsuario.equals(otro.idUsuario);
    }

    @Override
    public int hashCode() { return Objects.hashCode(idUsuario); }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

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

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public EstadoCuenta getEstadoCuenta() { return estadoCuenta; }
    public void setEstadoCuenta(EstadoCuenta estadoCuenta) { this.estadoCuenta = estadoCuenta; }

    public Boolean getRestringido() { return restringido; }
    public void setRestringido(Boolean restringido) { this.restringido = restringido; }
}
