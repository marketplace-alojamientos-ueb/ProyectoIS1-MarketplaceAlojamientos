package co.edu.unbosque.marketplace.model.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Servicio (amenity) que puede ofrecer un alojamiento, como wifi o parqueadero (tabla SERVICIO).
 * No es lo mismo que el servicio adicional, que tiene costo y se escoge al reservar.
 */
@Entity
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Integer idServicio;

    @Column(name = "nombre_servicio", nullable = false, length = 50, unique = true)
    private String nombre;

    public Servicio() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Servicio otro)) return false;
        return idServicio != null && idServicio.equals(otro.idServicio);
    }

    @Override
    public int hashCode() { return Objects.hashCode(idServicio); }

    public Integer getIdServicio() { return idServicio; }
    public void setIdServicio(Integer idServicio) { this.idServicio = idServicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
