package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos que envía el anfitrión al crear una publicación: los de DatosPublicacionDTO y el id del anfitrión.
 * El anfitrión viene en el body mientras el proyecto no tenga autenticación.
 */
@Getter
@Setter
public class PublicacionRequestDTO extends DatosPublicacionDTO {

    @NotNull(message = "El anfitrión es obligatorio")
    private Integer idAnfitrion;
}
