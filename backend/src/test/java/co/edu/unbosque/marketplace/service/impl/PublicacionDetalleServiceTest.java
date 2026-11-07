package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.PublicacionDetalleDTO;
import co.edu.unbosque.marketplace.model.entity.Imagen;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Servicio;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import co.edu.unbosque.marketplace.repository.AdminRepository;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.PublicacionRepository;
import co.edu.unbosque.marketplace.repository.TipoPoliticaCancelacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas del detalle de un alojamiento (MA-100). */
class PublicacionDetalleServiceTest {

    private PublicacionRepository publicacionRepository;
    private PublicacionServiceImpl servicio;

    @BeforeEach
    void setUp() {
        publicacionRepository = mock(PublicacionRepository.class);
        servicio = new PublicacionServiceImpl(publicacionRepository, mock(AnfitrionRepository.class),
                mock(TipoPoliticaCancelacionRepository.class), mock(AdminRepository.class));
    }

    @Test
    void consultarDetalleDevuelveLosDatosDelAlojamientoConSusServiciosEImagenes() {
        Servicio wifi = new Servicio();
        wifi.setNombre("WiFi");
        Imagen imagen = new Imagen();
        imagen.setRuta("https://imagenes.com/cabana.jpg");
        Publicacion publicacion = publicacion(EstadoPublicacion.ACTIVA);
        publicacion.setServicios(List.of(wifi));
        publicacion.setImagenes(List.of(imagen));
        when(publicacionRepository.findById(15)).thenReturn(Optional.of(publicacion));

        PublicacionDetalleDTO detalle = servicio.consultarDetalle(15);

        assertEquals("Cabaña con vista al lago", detalle.getTitulo());
        assertEquals("FLEXIBLE", detalle.getPoliticaCancelacion());
        assertEquals(List.of("WiFi"), detalle.getServicios());
        assertEquals(List.of("https://imagenes.com/cabana.jpg"), detalle.getImagenes());
    }

    @Test
    void consultarDetalleDeUnaPublicacionNoActivaResponde404() {
        when(publicacionRepository.findById(15)).thenReturn(Optional.of(publicacion(EstadoPublicacion.PAUSADA)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.consultarDetalle(15));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    private Publicacion publicacion(EstadoPublicacion estado) {
        Publicacion publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        publicacion.setTitulo("Cabaña con vista al lago");
        publicacion.setEstado(estado);
        publicacion.setModoAceptacion(ModoAceptacion.INMEDIATA);
        publicacion.setPoliticaCancelacion(new PoliticaFlexible());
        return publicacion;
    }
}
