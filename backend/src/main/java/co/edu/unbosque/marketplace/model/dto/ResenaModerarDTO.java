package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Decisión del administrador sobre una reseña reportada (ocultarla o mantenerla) y su motivo. */
@Getter
@Setter
public class ResenaModerarDTO {

    @NotNull(message = "El administrador es obligatorio")
    private Integer idAdmin;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 500, message = "El motivo no puede tener más de 500 caracteres")
    private String motivo;
}
