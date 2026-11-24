package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Datos para editar una reseña: solo la calificación y el comentario. */
@Getter
@Setter
public class ResenaActualizarDTO {

    @NotNull(message = "El huésped es obligatorio")
    private Integer idHuesped;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación debe estar entre 1 y 5")
    @Max(value = 5, message = "La calificación debe estar entre 1 y 5")
    private Integer calificacion;

    @Size(max = 1000, message = "El comentario no puede tener más de 1000 caracteres")
    private String comentario;
}
