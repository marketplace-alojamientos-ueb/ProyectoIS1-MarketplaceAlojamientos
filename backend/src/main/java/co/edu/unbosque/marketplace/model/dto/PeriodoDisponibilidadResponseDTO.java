package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.TipoPeriodo;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** Período de una publicación que el backend devuelve al front. */
@Getter
@Setter
public class PeriodoDisponibilidadResponseDTO {

    private Integer idPeriodo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private TipoPeriodo tipo;
}
