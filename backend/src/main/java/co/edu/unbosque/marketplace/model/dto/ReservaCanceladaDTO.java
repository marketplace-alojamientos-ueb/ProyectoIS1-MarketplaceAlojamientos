package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.EstadoDevolucion;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservaCanceladaDTO {

    private Integer idReserva;
    private EstadoReserva estado;
    private Double valorDevolucion;
    private EstadoDevolucion estadoDevolucion;
    private String mensaje;
}
