package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservaCancelarRequestDTO {

    @NotNull(message = "El huésped es obligatorio")
    private Integer idHuesped;

    @NotBlank(message = "El motivo de la cancelación es obligatorio")
    @Size(max = 500, message = "El motivo no puede tener más de 500 caracteres")
    private String motivo;
}
