package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulacionCancelacionDTO {

    private Integer idReserva;
    private EstadoReserva estado;
    private String politica;
    private Double valorTotal;
    private Double valorDevolucion;
    private boolean aplicaDevolucion;
    private String mensaje;
}
