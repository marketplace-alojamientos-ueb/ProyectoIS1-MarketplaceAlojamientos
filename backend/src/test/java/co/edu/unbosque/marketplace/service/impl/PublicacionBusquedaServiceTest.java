package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaRequestDTO;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas de la búsqueda de alojamientos (MA-99). */
class PublicacionBusquedaServiceTest {

    private PublicacionRepository publicacionRepository;
    private PublicacionServiceImpl servicio;

    @BeforeEach
    void setUp() {
        publicacionRepository = mock(PublicacionRepository.class);
        servicio = new PublicacionServiceImpl(publicacionRepository, mock(AnfitrionRepository.class),
                mock(TipoPoliticaCancelacionRepository.class), mock(AdminRepository.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void buscarDevuelveLosAlojamientosSinDatosDelAnfitrion() {
        Servicio wifi = new Servicio();
        wifi.setNombre("WiFi");
        Publicacion publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        publicacion.setTitulo("Cabaña con vista al lago");
        publicacion.setCiudad("Guatavita");
        publicacion.setCapacidad(4);
        publicacion.setPrecioPorNoche(250000.0);
        publicacion.setEstado(EstadoPublicacion.ACTIVA);
        publicacion.setModoAceptacion(ModoAceptacion.INMEDIATA);
        publicacion.setPoliticaCancelacion(new PoliticaFlexible());
        publicacion.setServicios(List.of(wifi));
        when(publicacionRepository.findAll(any(Specification.class))).thenReturn(List.of(publicacion));

        List<PublicacionBusquedaDTO> resultado = servicio.buscar(new PublicacionBusquedaRequestDTO());

        assertEquals(1, resultado.size());
        assertEquals(15, resultado.get(0).getIdPublicacion());
        assertEquals("Guatavita", resultado.get(0).getCiudad());
        assertEquals("INMEDIATA", resultado.get(0).getModoAceptacion());
        assertEquals("FLEXIBLE", resultado.get(0).getPoliticaCancelacion());
        assertEquals(List.of("WiFi"), resultado.get(0).getServicios());
    }

    @Test
    @SuppressWarnings("unchecked")
    void buscarSinCoincidenciasDevuelveListaVacia() {
        when(publicacionRepository.findAll(any(Specification.class))).thenReturn(List.of());

        assertTrue(servicio.buscar(new PublicacionBusquedaRequestDTO()).isEmpty());
    }
}
