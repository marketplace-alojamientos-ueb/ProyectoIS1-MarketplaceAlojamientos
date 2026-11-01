package co.edu.unbosque.marketplace.model.dto;

/**
 * Una opción de política de cancelación para el formulario de crear publicación. El front envía el código al
 * crear la publicación y muestra el nombre y la descripción al anfitrión.
 */
public class PoliticaCancelacionDTO {

    private String codigo;
    private String nombre;
    private String descripcion;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
