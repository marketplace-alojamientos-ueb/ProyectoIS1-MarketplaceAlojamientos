package co.edu.unbosque.marketplace.model.politica;

import co.edu.unbosque.marketplace.model.entity.politica.PoliticaCancelacionConverter;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas del convertidor entre la política (objeto) y su código en la base de datos. */
class PoliticaCancelacionConverterTest {

    private final PoliticaCancelacionConverter convertidor = new PoliticaCancelacionConverter();

    @Test
    void guardaLaPoliticaComoSuCodigo() {
        assertEquals("FLEXIBLE", convertidor.convertToDatabaseColumn(new PoliticaFlexible()));
    }

    @Test
    void guardaNuloSiNoHayPolitica() {
        assertNull(convertidor.convertToDatabaseColumn(null));
    }

    @Test
    void reconstruyeLaPoliticaDesdeElCodigo() {
        assertInstanceOf(PoliticaFlexible.class, convertidor.convertToEntityAttribute("FLEXIBLE"));
    }

    @Test
    void devuelveNuloSiLaColumnaEsNula() {
        assertNull(convertidor.convertToEntityAttribute(null));
    }

    @Test
    void unCodigoDesconocidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> convertidor.convertToEntityAttribute("INEXISTENTE"));
    }
}
