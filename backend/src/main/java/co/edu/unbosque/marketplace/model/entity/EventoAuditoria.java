package co.edu.unbosque.marketplace.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "evento_auditoria")
@Getter
@Setter
public class EventoAuditoria {

    public static final String CANCELAR_RESERVA = "CANCELAR_RESERVA";
    public static final String ENTIDAD_RESERVA = "RESERVA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Integer idEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "operacion", nullable = false, length = 50)
    private String operacion;

    @Column(name = "entidad_afectada", nullable = false, length = 50)
    private String entidadAfectada;

    @Column(name = "id_entidad")
    private Integer idEntidad;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "resultado", nullable = false, length = 50)
    private String resultado;

    @Column(name = "detalle", length = 1000)
    private String detalle;

    public EventoAuditoria() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EventoAuditoria otro)) return false;
        return idEvento != null && idEvento.equals(otro.idEvento);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idEvento);
    }
}
