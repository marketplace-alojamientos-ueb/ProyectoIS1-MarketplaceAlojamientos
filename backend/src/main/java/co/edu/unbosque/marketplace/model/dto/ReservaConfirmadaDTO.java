package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Resultado de confirmar una reserva con pago aprobado: la reserva, lo que se cobró y un mensaje para mostrar. */
@Getter
@Setter
public class ReservaConfirmadaDTO {

    private Integer idReserva;
    private EstadoReserva estado;
    private LocalDate fechaLlegada;
    private LocalDate fechaSalida;
    private Integer numHuespedes;
    private Integer idPublicacion;
    private String tituloPublicacion;
    private Integer noches;
    private Double precioBase;
    private Double tarifaLimpieza;
    private Double total;
    private String idTransaccion;
    private String mensaje;
}
