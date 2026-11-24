package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Reserva en una lista, con los datos que el huésped y el anfitrión necesitan para verla y cancelarla. */
@Getter
@Setter
public class ReservaListadoDTO {

    private Integer idReserva;
    private EstadoReserva estado;
    private LocalDate fechaLlegada;
    private LocalDate fechaSalida;
    private Integer numHuespedes;
    private Double total;
    private Integer idPublicacion;
    private String tituloPublicacion;
    private Integer idHuesped;
    private String nombreHuesped;
    private Integer idAnfitrion;
    private String nombreAnfitrion;
}
