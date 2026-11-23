package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Lo que manda el huésped al confirmar una reserva con pago simulado. Que las fechas tengan sentido, que quepan los
 * huéspedes y que el período siga disponible lo revisa ReservaService.
 */
@Getter
@Setter
public class ReservaConfirmarRequestDTO {

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

    /** La genera el front una vez por intento de reserva: si la misma petición llega dos veces, no se cobra dos veces. */
    @NotBlank(message = "La clave de idempotencia es obligatoria")
    @Size(max = 100, message = "La clave de idempotencia no puede tener más de 100 caracteres")
    private String claveIdempotencia;

    /** Tarjeta del pago simulado. Las que terminan en 0002 se rechazan (como la tarjeta de prueba de Stripe). */
    @NotBlank(message = "El número de tarjeta es obligatorio")
    @Pattern(regexp = "\\d{16}", message = "El número de tarjeta debe tener 16 dígitos")
    private String numeroTarjeta;
}
