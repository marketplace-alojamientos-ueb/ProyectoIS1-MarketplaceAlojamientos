package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos del alojamiento que comparten la solicitud y la respuesta de una publicación. Los límites de texto
 * coinciden con las columnas de la tabla PUBLICACION. Las validaciones solo se aplican a los DTOs que llegan en un
 * body con @Valid (la solicitud). Servicios, imágenes y disponibilidad se agregan en otras historias.
 *
 * <p>Los campos mínimos (todos menos tarifaLimpieza y reglas) no son obligatorios aquí: si falta alguno, la
 * publicación se guarda como BORRADOR y el servicio devuelve cuáles faltan. Lo que sí se valida es el formato de lo
 * que se envía (longitudes y números positivos).</p>
 */
@Getter
@Setter
public class DatosPublicacionDTO {

    @Size(max = 52, message = "El título admite máximo 52 caracteres")
    private String titulo;

    @Size(max = 500, message = "La descripción admite máximo 500 caracteres")
    private String descripcion;

    @Size(max = 100, message = "La ubicación admite máximo 100 caracteres")
    private String ubicacion;

    @Size(max = 50, message = "La ciudad admite máximo 50 caracteres")
    private String ciudad;

    @Size(max = 20, message = "El tipo de alojamiento admite máximo 20 caracteres")
    private String tipoAlojamiento;

    @Positive(message = "La capacidad debe ser mayor que 0")
    private Integer capacidad;

    @Positive(message = "El precio por noche debe ser mayor que 0")
    private Double precioPorNoche;

    /** Opcional: es un double primitivo, así que si el front no la envía queda en 0. */
    @PositiveOrZero(message = "La tarifa de limpieza no puede ser negativa")
    private double tarifaLimpieza;

    @Size(max = 1000, message = "Las reglas admiten máximo 1000 caracteres")
    private String reglas;

    private ModoAceptacion modoAceptacion;

    /** Código de la política, uno de los que devuelve PublicacionService.listarPoliticas(). */
    private String politicaCancelacion;
}
