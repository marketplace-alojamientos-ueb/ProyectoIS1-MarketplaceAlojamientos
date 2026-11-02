package co.edu.unbosque.marketplace.model.entity;

import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la entidad Publicacion: valores iniciales, igualdad por id y política de cancelación. */
class PublicacionTest {

    @Test
    void unaPublicacionNuevaEmpiezaEnBorradorYSinTarifaDeLimpieza() {
        Publicacion publicacion = new Publicacion();

        assertEquals(EstadoPublicacion.BORRADOR, publicacion.getEstado());
        assertEquals(0.0, publicacion.getTarifaLimpieza());
    }

    @Test
    void dosPublicacionesSonIgualesSiTienenElMismoId() {
        Publicacion a = new Publicacion();
        a.setIdPublicacion(1);
        Publicacion b = new Publicacion();
        b.setIdPublicacion(1);
        Publicacion c = new Publicacion();
        c.setIdPublicacion(2);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(new Publicacion(), new Publicacion());
    }

    @Test
    void laPoliticaDeCancelacionSeAsignaYSeConsulta() {
        Publicacion publicacion = new Publicacion();
        publicacion.setPoliticaCancelacion(new PoliticaFlexible());

        assertInstanceOf(PoliticaFlexible.class, publicacion.getPoliticaCancelacion());
    }

    @Test
    void sinPoliticaAsignadaNoHayPoliticaDeCancelacion() {
        assertNull(new Publicacion().getPoliticaCancelacion());
    }
}
