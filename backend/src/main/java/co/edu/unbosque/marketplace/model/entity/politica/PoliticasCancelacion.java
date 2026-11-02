package co.edu.unbosque.marketplace.model.entity.politica;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Relaciona el código guardado en la publicación (ej. 'FLEXIBLE') con la implementación de PoliticaCancelacion
 * que le corresponde. Al agregar una política nueva: crear su clase, agregarla a esta lista y insertar su fila en la
 * tabla politica_cancelacion de database/scripts.sql.
 */
public final class PoliticasCancelacion {

    private static final Map<String, PoliticaCancelacion> POR_CODIGO = Stream.of(new PoliticaFlexible())
            .collect(Collectors.toUnmodifiableMap(PoliticaCancelacion::codigo, Function.identity()));

    private PoliticasCancelacion() {
    }

    public static boolean existe(String codigo) {
        return codigo != null && POR_CODIGO.containsKey(codigo);
    }

    public static PoliticaCancelacion deCodigo(String codigo) {
        PoliticaCancelacion politica = codigo == null ? null : POR_CODIGO.get(codigo);
        if (politica == null) {
            throw new IllegalArgumentException("No hay una política de cancelación implementada para el código: " + codigo);
        }
        return politica;
    }
}
