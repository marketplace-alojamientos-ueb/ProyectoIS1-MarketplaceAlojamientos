package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.entity.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.enums.TipoPeriodo;
import co.edu.unbosque.marketplace.repository.AdminRepository;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.PublicacionRepository;
import co.edu.unbosque.marketplace.repository.TipoPoliticaCancelacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicacionLiberarPeriodoServiceTest {

    private PublicacionRepository publicacionRepository;
    private PublicacionServiceImpl servicio;
    private Publicacion publicacion;

    @BeforeEach
    void setUp() {
        publicacionRepository = mock(PublicacionRepository.class);
        servicio = new PublicacionServiceImpl(publicacionRepository, mock(AnfitrionRepository.class),
                mock(TipoPoliticaCancelacionRepository.class), mock(AdminRepository.class));

        publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        when(publicacionRepository.buscarParaBloquearPeriodos(15)).thenReturn(Optional.of(publicacion));
    }

    private static LocalDate dia(int dia) {
        return LocalDate.of(2030, 1, dia);
    }

    private void agregarPeriodo(int inicio, int fin, TipoPeriodo tipo) {
        PeriodoDisponibilidad periodo = new PeriodoDisponibilidad();
        periodo.setFechaInicio(dia(inicio));
        periodo.setFechaFin(dia(fin));
        periodo.setTipo(tipo);
        publicacion.getPeriodos().add(periodo);
    }

    private List<String> periodos() {
        return publicacion.getPeriodos().stream()
                .sorted(Comparator.comparing(PeriodoDisponibilidad::getFechaInicio))
                .map(p -> p.getFechaInicio().getDayOfMonth() + "-" + p.getFechaFin().getDayOfMonth() + "-" + p.getTipo())
                .toList();
    }

    @Test
    void liberarElPeriodoReservadoExactoLoDejaDisponible() {
        agregarPeriodo(1, 10, TipoPeriodo.DISPONIBLE);
        agregarPeriodo(10, 15, TipoPeriodo.RESERVADO);
        agregarPeriodo(15, 20, TipoPeriodo.DISPONIBLE);

        servicio.liberarPeriodoReservado(15, dia(10), dia(15));

        assertEquals(List.of("1-10-DISPONIBLE", "10-15-DISPONIBLE", "15-20-DISPONIBLE"), periodos());
        verify(publicacionRepository).save(publicacion);
    }

    @Test
    void liberarSoloLoQueCorrespondeDejaReservadoElResto() {
        agregarPeriodo(10, 20, TipoPeriodo.RESERVADO);

        servicio.liberarPeriodoReservado(15, dia(12), dia(15));

        assertEquals(List.of("10-12-RESERVADO", "12-15-DISPONIBLE", "15-20-RESERVADO"), periodos());
    }

    @Test
    void noTocaLosPeriodosBloqueadosPorElAnfitrion() {
        agregarPeriodo(10, 15, TipoPeriodo.BLOQUEADO);

        servicio.liberarPeriodoReservado(15, dia(10), dia(15));

        assertEquals(List.of("10-15-BLOQUEADO"), periodos());
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void siNoHayPeriodoReservadoQueLoCubraNoCambiaNada() {
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);

        servicio.liberarPeriodoReservado(15, dia(10), dia(15));

        assertEquals(List.of("1-20-DISPONIBLE"), periodos());
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void siLaPublicacionNoExisteResponde404() {
        when(publicacionRepository.buscarParaBloquearPeriodos(99)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.liberarPeriodoReservado(99, dia(10), dia(15)));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
