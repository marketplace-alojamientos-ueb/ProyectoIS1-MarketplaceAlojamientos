package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicacionEditarRequestDTO extends DatosPublicacionDTO {

    @NotNull(message = "El usuario que edita es obligatorio")
    private Integer idUsuario;
}
