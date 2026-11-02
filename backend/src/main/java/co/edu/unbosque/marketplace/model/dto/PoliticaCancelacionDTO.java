package co.edu.unbosque.marketplace.model.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Una opción de política de cancelación para el formulario de crear publicación. El front envía el código al
 * crear la publicación y muestra el nombre y la descripción al anfitrión.
 */
@Getter
@Setter
public class PoliticaCancelacionDTO {

    private String codigo;
    private String nombre;
    private String descripcion;
}
