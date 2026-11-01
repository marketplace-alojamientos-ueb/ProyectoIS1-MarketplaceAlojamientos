package co.edu.unbosque.marketplace.model.entity;

import co.edu.unbosque.marketplace.model.enums.TipoPeriodo;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de las listas de la publicación (servicios, imágenes y períodos) y de las entidades que contienen. */
class PublicacionRelacionesTest {

    @Test
    void unaPublicacionNuevaEmpiezaSinServiciosImagenesNiPeriodos() {
        Publicacion publicacion = new Publicacion();

        assertTrue(publicacion.getServicios().isEmpty());
        assertTrue(publicacion.getServiciosAdicionales().isEmpty());
        assertTrue(publicacion.getImagenes().isEmpty());
        assertTrue(publicacion.getPeriodos().isEmpty());
    }

    @Test
    void laPublicacionGuardaSusServiciosAdicionalesConSuCosto() {
        ServicioAdicional desayuno = new ServicioAdicional();
        desayuno.setNombre("Desayuno");
        desayuno.setCosto(15000.0);
        Publicacion publicacion = new Publicacion();

        publicacion.getServiciosAdicionales().add(desayuno);

        assertEquals("Desayuno", publicacion.getServiciosAdicionales().get(0).getNombre());
        assertEquals(15000.0, publicacion.getServiciosAdicionales().get(0).getCosto());
    }

    @Test
    void laPublicacionGuardaSusServiciosImagenesYPeriodos() {
        Servicio wifi = new Servicio();
        wifi.setNombre("Wifi");
        Imagen foto = new Imagen();
        foto.setRuta("imagenes/cabana-1.jpg");
        PeriodoDisponibilidad periodo = new PeriodoDisponibilidad();
        periodo.setFechaInicio(LocalDate.of(2026, 12, 1));
        periodo.setFechaFin(LocalDate.of(2026, 12, 15));
        periodo.setTipo(TipoPeriodo.DISPONIBLE);
        Publicacion publicacion = new Publicacion();

        publicacion.getServicios().add(wifi);
        publicacion.getImagenes().add(foto);
        publicacion.getPeriodos().add(periodo);

        assertEquals("Wifi", publicacion.getServicios().get(0).getNombre());
        assertEquals("imagenes/cabana-1.jpg", publicacion.getImagenes().get(0).getRuta());
        assertEquals(TipoPeriodo.DISPONIBLE, publicacion.getPeriodos().get(0).getTipo());
        assertEquals(LocalDate.of(2026, 12, 15), publicacion.getPeriodos().get(0).getFechaFin());
    }

    @Test
    void lasListasSePuedenReemplazar() {
        Publicacion publicacion = new Publicacion();

        publicacion.setServicios(new java.util.ArrayList<>());
        publicacion.setImagenes(new java.util.ArrayList<>());
        publicacion.setPeriodos(new java.util.ArrayList<>());

        assertNotNull(publicacion.getServicios());
        assertNotNull(publicacion.getImagenes());
        assertNotNull(publicacion.getPeriodos());
    }

    @Test
    void ServicioImagenYPeriodoSonIgualesSiTienenElMismoId() {
        Servicio s1 = new Servicio();
        s1.setIdServicio(1);
        Servicio s2 = new Servicio();
        s2.setIdServicio(1);
        Imagen i1 = new Imagen();
        i1.setIdImagen(2);
        Imagen i2 = new Imagen();
        i2.setIdImagen(2);
        PeriodoDisponibilidad p1 = new PeriodoDisponibilidad();
        p1.setIdPeriodo(3);
        PeriodoDisponibilidad p2 = new PeriodoDisponibilidad();
        p2.setIdPeriodo(3);

        ServicioAdicional a1 = new ServicioAdicional();
        a1.setIdServicioAdicional(4);
        ServicioAdicional a2 = new ServicioAdicional();
        a2.setIdServicioAdicional(4);

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertEquals(i1, i2);
        assertEquals(i1.hashCode(), i2.hashCode());
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void sinIdNingunaEntidadEsIgualAOtra() {
        assertNotEquals(new Servicio(), new Servicio());
        assertNotEquals(new ServicioAdicional(), new ServicioAdicional());
        assertNotEquals(new Imagen(), new Imagen());
        assertNotEquals(new PeriodoDisponibilidad(), new PeriodoDisponibilidad());
    }
}
