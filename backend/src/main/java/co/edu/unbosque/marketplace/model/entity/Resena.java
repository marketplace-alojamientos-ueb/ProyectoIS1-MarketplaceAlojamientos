package co.edu.unbosque.marketplace.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

/** Reseña que deja el huésped de una estancia completada. Hay máximo una por reserva. */
@Entity
@Table(name = "resena")
@Getter
@Setter
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Integer idResena;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    @Column(name = "calificacion", nullable = false)
    private Integer calificacion;

    @Column(name = "comentario", length = 1000)
    private String comentario;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha = LocalDate.now();

    /** false cuando un administrador la oculta (moderación). */
    @Column(name = "visible", nullable = false)
    private Boolean visible = true;

    @Column(name = "motivo_ocultamiento", length = 500)
    private String motivoOcultamiento;

    public Resena() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Resena otra)) return false;
        return idResena != null && idResena.equals(otra.idResena);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idResena);
    }
}
