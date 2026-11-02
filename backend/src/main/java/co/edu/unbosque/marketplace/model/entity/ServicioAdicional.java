package co.edu.unbosque.marketplace.model.entity;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Servicio adicional con costo que el huésped puede escoger al reservar, como transporte o desayuno
 * (tabla SERVICIO_ADICIONAL). Se usa Double para el costo, igual que los precios de Publicacion.
 */
@Entity
@Table(name = "servicio_adicional")
@Getter
@Setter
public class ServicioAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio_adicional")
    private Integer idServicioAdicional;

    @Column(name = "nombre_servicio", nullable = false, length = 50)
    private String nombre;

    @Column(name = "costo", nullable = false)
    private Double costo;

    public ServicioAdicional() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ServicioAdicional otro)) return false;
        return idServicioAdicional != null && idServicioAdicional.equals(otro.idServicioAdicional);
    }

    @Override
    public int hashCode() { return Objects.hashCode(idServicioAdicional); }
}
