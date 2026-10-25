package co.edu.unbosque.marketplace.modelo.entidad;

import co.edu.unbosque.marketplace.modelo.enums.EstadoCuenta;
import jakarta.persistence.*;

/**
 * Datos comunes de cualquier usuario de la plataforma (tabla USUARIO).
 * Anfitrion, Huesped y Admin se enlazan a esta tabla por la cédula.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @Column(name = "cedula")
    private Long cedula;

    @Column(name = "primer_nombre", nullable = false, length = 50)
    private String primerNombre;

    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @Column(name = "primer_apellido", nullable = false, length = 50)
    private String primerApellido;

    @Column(name = "segundo_apellido", nullable = false, length = 50)
    private String segundoApellido;

    @Column(name = "email", nullable = false, length = 200, unique = true)
    private String email;

    /** Hash BCrypt de la contraseña, nunca la contraseña en texto plano. */
    @Column(name = "contrasena", nullable = false, length = 100)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cuenta", nullable = false, length = 20)
    private EstadoCuenta estadoCuenta = EstadoCuenta.ACTIVA;

    @Column(name = "restringido", nullable = false)
    private Boolean restringido = false;

    public Usuario() {}

    public Usuario(Long cedula, String primerNombre, String segundoNombre, String primerApellido,
                   String segundoApellido, String email, String contrasena) {
        this.cedula = cedula;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.email = email;
        this.contrasena = contrasena;
    }

    public String getNombreCompleto() {
        String segundo = (segundoNombre != null && !segundoNombre.isBlank()) ? " " + segundoNombre : "";
        return primerNombre + segundo + " " + primerApellido + " " + segundoApellido;
    }

    public void desactivarCuenta() { this.estadoCuenta = EstadoCuenta.DESACTIVADA; }

    public void activarCuenta() { this.estadoCuenta = EstadoCuenta.ACTIVA; }

    public boolean estaActiva() { return estadoCuenta == EstadoCuenta.ACTIVA; }

    public Long getCedula() { return cedula; }
    public void setCedula(Long cedula) { this.cedula = cedula; }

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

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public EstadoCuenta getEstadoCuenta() { return estadoCuenta; }
    public void setEstadoCuenta(EstadoCuenta estadoCuenta) { this.estadoCuenta = estadoCuenta; }

    public Boolean getRestringido() { return restringido; }
    public void setRestringido(Boolean restringido) { this.restringido = restringido; }
}
