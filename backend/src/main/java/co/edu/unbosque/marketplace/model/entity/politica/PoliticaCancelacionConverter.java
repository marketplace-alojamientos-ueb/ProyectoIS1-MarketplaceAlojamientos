package co.edu.unbosque.marketplace.model.entity.politica;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Guarda la política de una publicación como su código (columna politica_cancelacion) y la reconstruye al leerla.
 */
@Converter
public class PoliticaCancelacionConverter implements AttributeConverter<PoliticaCancelacion, String> {

    @Override
    public String convertToDatabaseColumn(PoliticaCancelacion politica) {
        return politica == null ? null : politica.codigo();
    }

    @Override
    public PoliticaCancelacion convertToEntityAttribute(String codigo) {
        return codigo == null ? null : PoliticasCancelacion.deCodigo(codigo);
    }
}
