package co.edu.unbosque.marketplace.model.politica;

import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticasCancelacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la relación entre el código de la base de datos y la implementación de la política. */
class PoliticasCancelacionTest {

    @Test
    void elCodigoFlexibleDevuelveLaPoliticaFlexible() {
        assertInstanceOf(PoliticaFlexible.class, PoliticasCancelacion.deCodigo("FLEXIBLE"));
    }

    @Test
    void unCodigoSinImplementacionLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> PoliticasCancelacion.deCodigo("INEXISTENTE"));
    }

    @Test
    void unCodigoNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> PoliticasCancelacion.deCodigo(null));
    }

    @Test
    void existeSoloParaLosCodigosImplementados() {
        assertTrue(PoliticasCancelacion.existe("FLEXIBLE"));
        assertFalse(PoliticasCancelacion.existe("INEXISTENTE"));
        assertFalse(PoliticasCancelacion.existe(null));
    }
}
