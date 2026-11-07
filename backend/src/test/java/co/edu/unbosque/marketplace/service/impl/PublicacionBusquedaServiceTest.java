package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaRequestDTO;
import co.edu.unbosque.marketplace.model.entity.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Servicio;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import co.edu.unbosque.marketplace.model.enums.TipoPeriodo;
import co.edu.unbosque.marketplace.repository.AdminRepository;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.PublicacionRepository;
import co.edu.unbosque.marketplace.repository.TipoPoliticaCancelacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @Test
    @SuppressWarnings("unchecked")
    void buscarConFechasSoloDevuelveLosAlojamientosDisponiblesEnTodoElRango() {
        Publicacion libre = publicacion(1, periodo(TipoPeriodo.DISPONIBLE, "2026-12-01", "2026-12-31"));
        Publicacion variosPeriodos = publicacion(2, periodo(TipoPeriodo.DISPONIBLE, "2026-12-01", "2026-12-22"),
                periodo(TipoPeriodo.DISPONIBLE, "2026-12-22", "2026-12-31"));
        Publicacion reservada = publicacion(3, periodo(TipoPeriodo.DISPONIBLE, "2026-12-01", "2026-12-21"),
                periodo(TipoPeriodo.RESERVADO, "2026-12-21", "2026-12-24"),
                periodo(TipoPeriodo.DISPONIBLE, "2026-12-24", "2026-12-31"));
        Publicacion conHueco = publicacion(4, periodo(TipoPeriodo.DISPONIBLE, "2026-12-01", "2026-12-21"),
                periodo(TipoPeriodo.DISPONIBLE, "2026-12-22", "2026-12-31"));
        when(publicacionRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(libre, variosPeriodos, reservada, conHueco));
        PublicacionBusquedaRequestDTO filtros = new PublicacionBusquedaRequestDTO();
        filtros.setLlegada(LocalDate.parse("2026-12-20"));
        filtros.setSalida(LocalDate.parse("2026-12-23"));

        List<PublicacionBusquedaDTO> resultado = servicio.buscar(filtros);

        assertEquals(List.of(1, 2), resultado.stream().map(PublicacionBusquedaDTO::getIdPublicacion).toList());
    }

    @Test
    void buscarConSalidaNoPosteriorALlegadaRespondeError400() {
        PublicacionBusquedaRequestDTO filtros = new PublicacionBusquedaRequestDTO();
        filtros.setLlegada(LocalDate.parse("2026-12-23"));
        filtros.setSalida(LocalDate.parse("2026-12-23"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.buscar(filtros));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void buscarConCapacidadCeroRespondeError400() {
        PublicacionBusquedaRequestDTO filtros = new PublicacionBusquedaRequestDTO();
        filtros.setCapacidad(0);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.buscar(filtros));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(publicacionRepository);
    }

    private Publicacion publicacion(int id, PeriodoDisponibilidad... periodos) {
        Publicacion publicacion = new Publicacion();
        publicacion.setIdPublicacion(id);
        publicacion.setEstado(EstadoPublicacion.ACTIVA);
        publicacion.setModoAceptacion(ModoAceptacion.INMEDIATA);
        publicacion.setPoliticaCancelacion(new PoliticaFlexible());
        publicacion.setPeriodos(List.of(periodos));
        return publicacion;
    }

    private PeriodoDisponibilidad periodo(TipoPeriodo tipo, String inicio, String fin) {
        PeriodoDisponibilidad periodo = new PeriodoDisponibilidad();
        periodo.setTipo(tipo);
        periodo.setFechaInicio(LocalDate.parse(inicio));
        periodo.setFechaFin(LocalDate.parse(fin));
        return periodo;
    }
}
