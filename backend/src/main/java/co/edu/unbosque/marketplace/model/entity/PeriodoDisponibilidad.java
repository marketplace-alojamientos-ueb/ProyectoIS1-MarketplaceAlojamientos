package co.edu.unbosque.marketplace.model.entity;

import co.edu.unbosque.marketplace.model.enums.TipoPeriodo;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

/** Rango de fechas de una publicación, disponible, bloqueado o reservado (tabla PERIODO_DISPONIBILIDAD). */
@Entity
@Table(name = "periodo_disponibilidad")
public class PeriodoDisponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_periodo")
    private Integer idPeriodo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoPeriodo tipo;

    public PeriodoDisponibilidad() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PeriodoDisponibilidad otro)) return false;
        return idPeriodo != null && idPeriodo.equals(otro.idPeriodo);
    }

    @Override
    public int hashCode() { return Objects.hashCode(idPeriodo); }

    public Integer getIdPeriodo() { return idPeriodo; }
    public void setIdPeriodo(Integer idPeriodo) { this.idPeriodo = idPeriodo; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public TipoPeriodo getTipo() { return tipo; }
    public void setTipo(TipoPeriodo tipo) { this.tipo = tipo; }
}
