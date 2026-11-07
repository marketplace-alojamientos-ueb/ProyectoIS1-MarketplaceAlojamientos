package co.edu.unbosque.marketplace.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Detalle de un alojamiento para el huésped: los datos de la búsqueda y sus imágenes. */
@Getter
@Setter
public class PublicacionDetalleDTO extends PublicacionBusquedaDTO {

    private List<String> imagenes;
}
