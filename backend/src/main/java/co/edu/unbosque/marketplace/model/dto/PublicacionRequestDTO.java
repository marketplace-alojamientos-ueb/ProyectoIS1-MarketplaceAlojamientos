package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos que envía el anfitrión al crear una publicación: los de DatosPublicacionDTO, el id del anfitrión y los
 * períodos que ofrece. El anfitrión viene en el body mientras el proyecto no tenga autenticación.
 */
@Getter
@Setter
public class PublicacionRequestDTO extends DatosPublicacionDTO {

    @NotNull(message = "El anfitrión es obligatorio")
    private Integer idAnfitrion;

    /** Sin períodos la publicación queda como BORRADOR; los que se envíen sí deben traer sus dos fechas. */
    private List<@NotNull(message = "El período no puede estar vacío") @Valid PeriodoDisponibilidadRequestDTO> periodos;
}
