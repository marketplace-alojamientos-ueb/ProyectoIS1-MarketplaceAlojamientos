package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Publicación que el backend devuelve al front: los datos de DatosPublicacionDTO, el id y el estado que asigna el
 * sistema, el anfitrión como DTO (nunca la entidad ni la contraseña) y sus períodos.
 */
@Getter
@Setter
public class PublicacionResponseDTO extends DatosPublicacionDTO {

    private Integer idPublicacion;
    private EstadoPublicacion estado;
    private AnfitrionResponseDTO anfitrion;
    private List<PeriodoDisponibilidadResponseDTO> periodos;
}
