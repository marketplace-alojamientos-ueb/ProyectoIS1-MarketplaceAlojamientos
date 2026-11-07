package co.edu.unbosque.marketplace.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/** Filtros de la búsqueda de alojamientos. Todos son opcionales. */
@Getter
@Setter
public class PublicacionBusquedaRequestDTO {

    private String ciudad;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate llegada;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate salida;

    private Integer capacidad;
    private String tipo;
    private Double precioMin;
    private Double precioMax;
    private List<String> servicios;
}
