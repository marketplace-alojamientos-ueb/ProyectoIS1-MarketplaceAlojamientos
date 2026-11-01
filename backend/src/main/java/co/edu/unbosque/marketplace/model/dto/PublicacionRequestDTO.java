package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Datos que envía el anfitrión al crear una publicación: los de DatosPublicacionDTO y el id del anfitrión.
 * El anfitrión viene en el body mientras el proyecto no tenga autenticación.
 */
public class PublicacionRequestDTO extends DatosPublicacionDTO {

    @NotNull(message = "El anfitrión es obligatorio")
    private Integer idAnfitrion;

    public Integer getIdAnfitrion() { return idAnfitrion; }
    public void setIdAnfitrion(Integer idAnfitrion) { this.idAnfitrion = idAnfitrion; }
}
