package co.edu.unbosque.marketplace.model.dto;

import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;

/**
 * Publicación que el backend devuelve al front: los datos de DatosPublicacionDTO, el id y el estado que asigna el
 * sistema, y el anfitrión como DTO (nunca la entidad ni la contraseña).
 */
public class PublicacionResponseDTO extends DatosPublicacionDTO {

    private Integer idPublicacion;
    private EstadoPublicacion estado;
    private AnfitrionResponseDTO anfitrion;

    public Integer getIdPublicacion() { return idPublicacion; }
    public void setIdPublicacion(Integer idPublicacion) { this.idPublicacion = idPublicacion; }

    public EstadoPublicacion getEstado() { return estado; }
    public void setEstado(EstadoPublicacion estado) { this.estado = estado; }

    public AnfitrionResponseDTO getAnfitrion() { return anfitrion; }
    public void setAnfitrion(AnfitrionResponseDTO anfitrion) { this.anfitrion = anfitrion; }
}
