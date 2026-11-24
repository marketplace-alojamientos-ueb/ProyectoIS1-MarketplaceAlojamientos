package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Lo que manda el huésped para empezar a pagar una reserva con Stripe. A diferencia del pago simulado, no lleva
 * tarjeta (la escribe en la página de Stripe) ni clave de idempotencia (se usa el id de la sesión de Stripe).
 */
@Getter
@Setter
public class ReservaCheckoutRequestDTO {

    @NotNull(message = "El huésped es obligatorio")
    private Integer idHuesped;

    @NotNull(message = "La publicación es obligatoria")
    private Integer idPublicacion;

    @NotNull(message = "La fecha de llegada es obligatoria")
    private LocalDate fechaLlegada;

    @NotNull(message = "La fecha de salida es obligatoria")
    private LocalDate fechaSalida;

    @NotNull(message = "El número de huéspedes es obligatorio")
    @Positive(message = "El número de huéspedes debe ser mayor a cero")
    private Integer numHuespedes;
}
