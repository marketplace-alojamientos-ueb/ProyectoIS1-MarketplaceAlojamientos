package co.edu.unbosque.marketplace.model.politica;

import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaCancelacion;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la política flexible: su código y, mientras no exista la fórmula, el aviso de devolución pendiente. */
class PoliticaFlexibleTest {

    @Test
    void suCodigoEsFlexible() {
        assertEquals("FLEXIBLE", new PoliticaFlexible().codigo());
    }

    @Test
    void calcularDevolucionAunNoEstaImplementado() {
        PoliticaCancelacion politica = new PoliticaFlexible();
        Reserva reserva = new Reserva();

        assertThrows(UnsupportedOperationException.class, () -> politica.calcularDevolucion(reserva));
    }
}
