package co.edu.unbosque.marketplace.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Filtros de la búsqueda de alojamientos. Todos son opcionales. */
@Getter
@Setter
public class PublicacionBusquedaRequestDTO {

    private String ciudad;
    private Integer capacidad;
    private String tipo;
    private Double precioMin;
    private Double precioMax;
    private List<String> servicios;
}
