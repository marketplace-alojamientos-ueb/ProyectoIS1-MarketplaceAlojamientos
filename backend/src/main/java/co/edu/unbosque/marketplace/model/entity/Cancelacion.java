package co.edu.unbosque.marketplace.model.entity;

import co.edu.unbosque.marketplace.model.enums.EstadoDevolucion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "cancelacion")
@Getter
@Setter
public class Cancelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cancelacion")
    private Integer idCancelacion;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "valor_devolucion", nullable = false)
    private Double valorDevolucion = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_devolucion", nullable = false, length = 20)
    private EstadoDevolucion estadoDevolucion;

    public Cancelacion() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Cancelacion otra)) return false;
        return idCancelacion != null && idCancelacion.equals(otra.idCancelacion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idCancelacion);
    }
}
