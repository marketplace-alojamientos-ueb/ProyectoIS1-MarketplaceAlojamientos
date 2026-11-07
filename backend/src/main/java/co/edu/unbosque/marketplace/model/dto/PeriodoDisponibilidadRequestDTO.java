package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Rango de fechas que el anfitrión ofrece al crear una publicación. Que el rango tenga sentido (fin posterior al
 * inicio, sin fechas pasadas ni cruces con otros rangos) lo revisa PublicacionService.
 */
@Getter
@Setter
public class PeriodoDisponibilidadRequestDTO {

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;
}
