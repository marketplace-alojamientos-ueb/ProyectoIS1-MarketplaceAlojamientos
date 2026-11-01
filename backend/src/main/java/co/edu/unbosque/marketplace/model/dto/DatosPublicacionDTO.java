package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import jakarta.validation.constraints.*;

/**
 * Datos del alojamiento que comparten la solicitud y la respuesta de una publicación. Los límites de texto
 * coinciden con las columnas de la tabla PUBLICACION. Las validaciones solo se aplican a los DTOs que llegan en un
 * body con @Valid (la solicitud). Servicios, imágenes y disponibilidad se agregan en otras historias.
 */
public class DatosPublicacionDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 52, message = "El título admite máximo 52 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción admite máximo 500 caracteres")
    private String descripcion;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 100, message = "La ubicación admite máximo 100 caracteres")
    private String ubicacion;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 50, message = "La ciudad admite máximo 50 caracteres")
    private String ciudad;

    @NotBlank(message = "El tipo de alojamiento es obligatorio")
    @Size(max = 20, message = "El tipo de alojamiento admite máximo 20 caracteres")
    private String tipoAlojamiento;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor que 0")
    private Integer capacidad;

    @NotNull(message = "El precio por noche es obligatorio")
    @Positive(message = "El precio por noche debe ser mayor que 0")
    private Double precioPorNoche;

    /** Opcional: es un double primitivo, así que si el front no la envía queda en 0. */
    @PositiveOrZero(message = "La tarifa de limpieza no puede ser negativa")
    private double tarifaLimpieza;

    @Size(max = 1000, message = "Las reglas admiten máximo 1000 caracteres")
    private String reglas;

    @NotNull(message = "El modo de aceptación es obligatorio")
    private ModoAceptacion modoAceptacion;

    /** Código de la política, uno de los que devuelve PublicacionService.listarPoliticas(). */
    @NotBlank(message = "La política de cancelación es obligatoria")
    private String politicaCancelacion;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getTipoAlojamiento() { return tipoAlojamiento; }
    public void setTipoAlojamiento(String tipoAlojamiento) { this.tipoAlojamiento = tipoAlojamiento; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public Double getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(Double precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    public double getTarifaLimpieza() { return tarifaLimpieza; }
    public void setTarifaLimpieza(double tarifaLimpieza) { this.tarifaLimpieza = tarifaLimpieza; }

    public String getReglas() { return reglas; }
    public void setReglas(String reglas) { this.reglas = reglas; }

    public ModoAceptacion getModoAceptacion() { return modoAceptacion; }
    public void setModoAceptacion(ModoAceptacion modoAceptacion) { this.modoAceptacion = modoAceptacion; }

    public String getPoliticaCancelacion() { return politicaCancelacion; }
    public void setPoliticaCancelacion(String politicaCancelacion) { this.politicaCancelacion = politicaCancelacion; }
}
