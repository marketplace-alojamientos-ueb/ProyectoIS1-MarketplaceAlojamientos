package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Reserva en espera de aprobación, tal como la ve el anfitrión dueño del alojamiento. */
@Getter
@Setter
public class ReservaPendienteDTO {

    private Integer idReserva;
    private EstadoReserva estado;
    private LocalDate fechaLlegada;
    private LocalDate fechaSalida;
    private Integer numHuespedes;
    private Integer idPublicacion;
    private String tituloPublicacion;
    private Integer idHuesped;
    private String nombreHuesped;
}
