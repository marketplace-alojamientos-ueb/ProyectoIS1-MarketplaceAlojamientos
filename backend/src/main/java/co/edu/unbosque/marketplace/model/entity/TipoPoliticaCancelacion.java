package co.edu.unbosque.marketplace.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Fila del catálogo de políticas de cancelación (tabla POLITICA_CANCELACION). Solo se lee: sirve para mostrar el
 * nombre y la descripción de cada política. Lo que hace cada política lo define su clase en model/politica.
 */
@Entity
@Table(name = "politica_cancelacion")
@Getter
@Setter
public class TipoPoliticaCancelacion {

    @Id
    @Column(name = "codigo", length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    public TipoPoliticaCancelacion() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TipoPoliticaCancelacion otra)) return false;
        return codigo != null && codigo.equals(otra.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
