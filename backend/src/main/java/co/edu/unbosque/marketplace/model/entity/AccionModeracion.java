package co.edu.unbosque.marketplace.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/** Acción que toma un administrador al moderar (por ahora, ocultar una reseña). */
@Entity
@Table(name = "accion_moderacion")
@Getter
@Setter
public class AccionModeracion {

    public static final String OCULTAMIENTO_REVIEW = "OCULTAMIENTO_REVIEW";
    public static final String ENTIDAD_RESENA = "RESENA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_accion")
    private Integer idAccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_admin", nullable = false)
    private Admin admin;

    @Column(name = "tipo_accion", nullable = false, length = 30)
    private String tipoAccion;

    @Column(name = "entidad_afectada", nullable = false, length = 50)
    private String entidadAfectada;

    @Column(name = "id_entidad", nullable = false)
    private Integer idEntidad;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public AccionModeracion() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AccionModeracion otra)) return false;
        return idAccion != null && idAccion.equals(otra.idAccion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idAccion);
    }
}
