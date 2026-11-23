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

    @Column(name = "noches", nullable = false)
    private Integer noches;

    /** Las columnas de valores son DECIMAL(12,2). Se usa Double como en Publicacion. */
    @Column(name = "precio_base", nullable = false)
    private Double precioBase;

    @Column(name = "tarifa_limpieza", nullable = false)
    private Double tarifaLimpieza = 0.0;

    @Column(name = "tarifa_servicio", nullable = false)
    private Double tarifaServicio = 0.0;

    @Column(name = "descuentos", nullable = false)
    private Double descuentos = 0.0;

    @Column(name = "costo_servicios_adicionales", nullable = false)
    private Double costoServiciosAdicionales = 0.0;

    @Column(name = "total", nullable = false)
    private Double total;

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
