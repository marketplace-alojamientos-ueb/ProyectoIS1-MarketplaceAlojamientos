package co.edu.unbosque.marketplace.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "cotizacion")
@Getter
@Setter
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cotizacion")
    private Integer idCotizacion;

    public Cotizacion() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Cotizacion otra)) return false;
        return idCotizacion != null && idCotizacion.equals(otra.idCotizacion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idCotizacion);
    }
}
