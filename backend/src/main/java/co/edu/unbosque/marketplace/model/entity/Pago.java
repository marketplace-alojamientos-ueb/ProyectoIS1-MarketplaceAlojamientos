package co.edu.unbosque.marketplace.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Movimiento de dinero de una reserva (tabla PAGO). Por ahora el pago es simulado: no se llama a Stripe y en
 * id_transaccion_stripe se guarda un código generado.
 */
@Entity
@Table(name = "pago")
@Getter
@Setter
public class Pago {

    public static final String TIPO_PAGO = "PAGO";
    public static final String APROBADO = "APROBADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    /** PAGO o DEVOLUCION. */
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    /** La columna es DECIMAL(12,2). Se usa Double como en Publicacion. */
    @Column(name = "valor", nullable = false)
    private Double valor;

    @Column(name = "emisor", nullable = false, length = 50)
    private String emisor;

    @Column(name = "receptor", nullable = false, length = 50)
    private String receptor;

    @Column(name = "id_transaccion_stripe", length = 100, unique = true)
    private String idTransaccion;

    /** APROBADO o RECHAZADO. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Pago() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pago otro)) return false;
        return idPago != null && idPago.equals(otro.idPago);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idPago);
    }
}
