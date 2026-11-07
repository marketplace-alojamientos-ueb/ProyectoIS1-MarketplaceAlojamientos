package co.edu.unbosque.marketplace.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Alojamiento que devuelve la búsqueda. No incluye datos personales del anfitrión. */
@Getter
@Setter
public class PublicacionBusquedaDTO {

    private Integer idPublicacion;
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private String ciudad;
    private String tipoAlojamiento;
    private Integer capacidad;
    private Double precioPorNoche;
    private Double tarifaLimpieza;
    private String reglas;
    private String modoAceptacion;
    private String politicaCancelacion;
    private List<String> servicios;
}
