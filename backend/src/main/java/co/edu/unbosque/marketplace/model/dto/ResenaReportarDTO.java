package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Datos para reportar una reseña: quién la reporta y por qué. */
@Getter
@Setter
public class ResenaReportarDTO {

    @NotNull(message = "El usuario es obligatorio")
    private Integer idUsuario;

    @NotBlank(message = "El motivo del reporte es obligatorio")
    @Size(max = 500, message = "El motivo no puede tener más de 500 caracteres")
    private String motivo;
}
