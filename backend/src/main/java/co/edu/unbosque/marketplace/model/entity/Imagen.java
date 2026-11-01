package co.edu.unbosque.marketplace.model.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Imagen de una publicación (tabla REFERENCIA_IMAGEN). Por ahora guarda solo la ruta o URL, como el diagrama.
 * La columna archivo_imagen de la tabla (para guardar el archivo) se mapeará cuando se defina cómo se suben las imágenes.
 */
@Entity
@Table(name = "referencia_imagen")
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Integer idImagen;

    @Column(name = "url_imagen", length = 2048)
    private String ruta;

    public Imagen() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Imagen otra)) return false;
        return idImagen != null && idImagen.equals(otra.idImagen);
    }

    @Override
    public int hashCode() { return Objects.hashCode(idImagen); }

    public Integer getIdImagen() { return idImagen; }
    public void setIdImagen(Integer idImagen) { this.idImagen = idImagen; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }
}
