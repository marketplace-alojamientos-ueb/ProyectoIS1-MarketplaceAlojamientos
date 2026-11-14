package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.entity.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas del bloqueo automático de las fechas de una reserva confirmada. */
class PublicacionBloqueoPeriodoServiceTest {

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

    /** Resume los períodos de la publicación, en orden de fecha, como "inicio-fin-TIPO". */
    private List<String> periodos() {
        return publicacion.getPeriodos().stream()
                .sorted(Comparator.comparing(PeriodoDisponibilidad::getFechaInicio))
                .map(p -> p.getFechaInicio().getDayOfMonth() + "-" + p.getFechaFin().getDayOfMonth() + "-" + p.getTipo())
                .toList();
    }

    private void assertRechazado(HttpStatus esperado, int llegada, int salida) {
        List<String> antes = periodos();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicio.bloquearPeriodoReservado(15, dia(llegada), dia(salida)));

        assertEquals(esperado, ex.getStatusCode());
        assertEquals(antes, periodos());
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void reservarElCentroDejaDisponibleLoDeAntesYLoDeDespues() {
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);

        servicio.bloquearPeriodoReservado(15, dia(5), dia(10));

        assertEquals(List.of("1-5-DISPONIBLE", "5-10-RESERVADO", "10-20-DISPONIBLE"), periodos());
        verify(publicacionRepository).save(publicacion);
    }

    @Test
    void reservarDesdeElInicioSoloDejaLoDeDespues() {
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);

        servicio.bloquearPeriodoReservado(15, dia(1), dia(10));

        assertEquals(List.of("1-10-RESERVADO", "10-20-DISPONIBLE"), periodos());
    }

    @Test
    void reservarHastaElFinSoloDejaLoDeAntes() {
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);

        servicio.bloquearPeriodoReservado(15, dia(10), dia(20));

        assertEquals(List.of("1-10-DISPONIBLE", "10-20-RESERVADO"), periodos());
    }

    @Test
    void reservarElPeriodoCompletoNoDejaNadaDisponible() {
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);

        servicio.bloquearPeriodoReservado(15, dia(1), dia(20));

        assertEquals(List.of("1-20-RESERVADO"), periodos());
    }

    @Test
    void sePuedeReservarSobreDosPeriodosSeguidos() {
        agregarPeriodo(1, 5, TipoPeriodo.DISPONIBLE);
        agregarPeriodo(5, 10, TipoPeriodo.DISPONIBLE);

        servicio.bloquearPeriodoReservado(15, dia(3), dia(7));

        assertEquals(List.of("1-3-DISPONIBLE", "3-7-RESERVADO", "7-10-DISPONIBLE"), periodos());
    }

    @Test
    void noTocaLosPeriodosQueNoHacenParteDelRango() {
        agregarPeriodo(1, 5, TipoPeriodo.DISPONIBLE);
        agregarPeriodo(10, 15, TipoPeriodo.DISPONIBLE);

        servicio.bloquearPeriodoReservado(15, dia(10), dia(12));

        assertEquals(List.of("1-5-DISPONIBLE", "10-12-RESERVADO", "12-15-DISPONIBLE"), periodos());
    }

    @Test
    void lasMismasFechasNoSePuedenReservarDosVeces() {
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);
        servicio.bloquearPeriodoReservado(15, dia(5), dia(10));
        clearInvocations(publicacionRepository);

        assertRechazado(HttpStatus.CONFLICT, 5, 10);
    }

    @Test
    void unaReservaQueSeCruzaConOtraRecibe409() {
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);
        servicio.bloquearPeriodoReservado(15, dia(5), dia(10));
        clearInvocations(publicacionRepository);

        assertRechazado(HttpStatus.CONFLICT, 8, 12);
    }

    @Test
    void sePuedeLlegarElDiaQueSaleLaReservaAnterior() {
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);
        servicio.bloquearPeriodoReservado(15, dia(5), dia(10));

        servicio.bloquearPeriodoReservado(15, dia(10), dia(12));

        assertEquals(List.of("1-5-DISPONIBLE", "5-10-RESERVADO", "10-12-RESERVADO", "12-20-DISPONIBLE"), periodos());
    }

    @Test
    void unRangoQuePasaDelPeriodoRecibe409() {
        agregarPeriodo(1, 10, TipoPeriodo.DISPONIBLE);

        assertRechazado(HttpStatus.CONFLICT, 5, 12);
    }

    @Test
    void unRangoQueEmpiezaAntesDelPeriodoRecibe409() {
        agregarPeriodo(5, 10, TipoPeriodo.DISPONIBLE);

        assertRechazado(HttpStatus.CONFLICT, 3, 8);
    }

    @Test
    void unRangoConUnHuecoEntrePeriodosRecibe409() {
        agregarPeriodo(1, 5, TipoPeriodo.DISPONIBLE);
        agregarPeriodo(7, 10, TipoPeriodo.DISPONIBLE);

        assertRechazado(HttpStatus.CONFLICT, 3, 8);
    }

    @Test
    void unPeriodoBloqueadoNoSePuedeReservar() {
        agregarPeriodo(1, 10, TipoPeriodo.BLOQUEADO);

        assertRechazado(HttpStatus.CONFLICT, 3, 5);
    }

    @Test
    void sinPeriodosRecibe409() {
        assertRechazado(HttpStatus.CONFLICT, 3, 5);
    }

    @Test
    void unaSalidaQueNoEsPosteriorALaLlegadaRecibe400() {
        agregarPeriodo(1, 10, TipoPeriodo.DISPONIBLE);

        assertRechazado(HttpStatus.BAD_REQUEST, 5, 5);
        assertRechazado(HttpStatus.BAD_REQUEST, 6, 5);
    }

    @Test
    void unaPublicacionQueNoExisteRecibe404() {
        when(publicacionRepository.buscarParaBloquearPeriodos(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicio.bloquearPeriodoReservado(99, dia(1), dia(2)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ---- obtenerParaReservar (MA-102): revisa la disponibilidad antes del pago, sin tocar los períodos ----

    @Test
    void obtenerParaReservarDevuelveLaPublicacionActivaSiLasFechasSiguenDisponibles() {
        publicacion.setEstado(EstadoPublicacion.ACTIVA);
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);

        assertSame(publicacion, servicio.obtenerParaReservar(15, dia(5), dia(10)));
        assertEquals(List.of("1-20-DISPONIBLE"), periodos());
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void obtenerParaReservarCuandoSePerdioLaDisponibilidadResponde409() {
        publicacion.setEstado(EstadoPublicacion.ACTIVA);
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);
        agregarPeriodo(6, 8, TipoPeriodo.RESERVADO);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicio.obtenerParaReservar(15, dia(5), dia(10)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Se perdió la disponibilidad"));
    }

    @Test
    void obtenerParaReservarUnaPublicacionNoActivaResponde409() {
        publicacion.setEstado(EstadoPublicacion.PAUSADA);
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicio.obtenerParaReservar(15, dia(5), dia(10)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // ---- esperarTurnoParaReservar (MA-103): solo toma el lock de la publicación ----

    @Test
    void esperarTurnoParaReservarTomaElLockSinTocarLosPeriodos() {
        agregarPeriodo(1, 20, TipoPeriodo.DISPONIBLE);

        servicio.esperarTurnoParaReservar(15);

        verify(publicacionRepository).buscarParaBloquearPeriodos(15);
        assertEquals(List.of("1-20-DISPONIBLE"), periodos());
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void esperarTurnoParaReservarUnaPublicacionQueNoExisteResponde404() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicio.esperarTurnoParaReservar(99));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void obtenerParaReservarUnaPublicacionQueNoExisteResponde404() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicio.obtenerParaReservar(99, dia(1), dia(2)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
