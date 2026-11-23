package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.ReservaCanceladaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.SimulacionCancelacionDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Cotizacion;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Pago;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import co.edu.unbosque.marketplace.model.enums.EstadoCuenta;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.CancelacionRepository;
import co.edu.unbosque.marketplace.repository.EventoAuditoriaRepository;
import co.edu.unbosque.marketplace.repository.HuespedRepository;
import co.edu.unbosque.marketplace.repository.NotificacionRepository;
import co.edu.unbosque.marketplace.repository.PagoRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import co.edu.unbosque.marketplace.service.PublicacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas de calcular la devolución simulada de una cancelación (MA-97). El huésped 20 consulta la reserva 31 de la
 * publicación 15, con un total de 500000.
 */
class ReservaSimulacionCancelacionServiceTest {

    private static final ZoneId ZONA = ZoneId.systemDefault();

    private ReservaRepository reservaRepository;
    private HuespedRepository huespedRepository;
    private PublicacionService publicacionService;
    private NotificacionRepository notificacionRepository;
    private PagoRepository pagoRepository;
    private CancelacionRepository cancelacionRepository;
    private EventoAuditoriaRepository eventoAuditoriaRepository;
    private ReservaServiceImpl servicio;
    private Reserva reserva;
    private Huesped huesped;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        huespedRepository = mock(HuespedRepository.class);
        publicacionService = mock(PublicacionService.class);
        notificacionRepository = mock(NotificacionRepository.class);
        pagoRepository = mock(PagoRepository.class);
        cancelacionRepository = mock(CancelacionRepository.class);
        eventoAuditoriaRepository = mock(EventoAuditoriaRepository.class);
        servicio = new ReservaServiceImpl(reservaRepository, mock(AnfitrionRepository.class), publicacionService,
                notificacionRepository, huespedRepository, pagoRepository, cancelacionRepository,
                eventoAuditoriaRepository);

        huesped = huesped(20, "luis@correo.com");
        when(huespedRepository.findById(20)).thenReturn(Optional.of(huesped));

        Usuario usuarioAnfitrion = new Usuario("1012345678", "Ana", null, "Gómez", "Rojas", "ana@correo.com", "hash");
        usuarioAnfitrion.setIdUsuario(7);
        Anfitrion anfitrion = new Anfitrion(usuarioAnfitrion);
        anfitrion.setIdUsuario(7);

        Publicacion publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        publicacion.setTitulo("Cabaña con vista al lago");
        publicacion.setAnfitrion(anfitrion);
        publicacion.setPoliticaCancelacion(new PoliticaFlexible());

        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setNoches(3);
        cotizacion.setTotal(500000.0);

        LocalDate llegada = LocalDate.now().plusDays(10);
        reserva = new Reserva();
        reserva.setIdReserva(31);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaLlegada(llegada);
        reserva.setFechaSalida(llegada.plusDays(3));
        reserva.setHuesped(huesped);
        reserva.setPublicacion(publicacion);
        reserva.setCotizacion(cotizacion);
        when(reservaRepository.findById(31)).thenReturn(Optional.of(reserva));
    }

    private Huesped huesped(int id, String correo) {
        Usuario usuario = new Usuario("10987654" + id, "Luis", null, "Pérez", "Soto", correo, "hash");
        usuario.setIdUsuario(id);
        Huesped nuevo = new Huesped(usuario);
        nuevo.setIdUsuario(id);
        return nuevo;
    }

    private void llegaraEnHoras(long horas) {
        reserva.setFechaLlegada(LocalDate.now().plusDays(1));
        reserva.setFechaSalida(LocalDate.now().plusDays(4));
        reserva.getPublicacion().setPoliticaCancelacion(new PoliticaFlexible(Clock.fixed(
                reserva.getFechaLlegada().atStartOfDay(ZONA).minusHours(horas).toInstant(), ZONA)));
    }

    private ResponseStatusException assertFalla(HttpStatus esperado, int idHuesped) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.simularCancelacion(31, idHuesped));
        assertEquals(esperado, error.getStatusCode());
        return error;
    }

    private void assertNoEscribioNada() {
        verify(reservaRepository, never()).save(any());
        verify(publicacionService, never()).liberarPeriodoReservado(any(), any(), any());
        verifyNoInteractions(cancelacionRepository, pagoRepository, notificacionRepository, eventoAuditoriaRepository);
    }

    // Escenario 1
    @Test
    void cancelandoDentroDelPlazoMuestraLaDevolucionSimulada() {
        SimulacionCancelacionDTO simulacion = servicio.simularCancelacion(31, 20);

        assertEquals(31, simulacion.getIdReserva());
        assertEquals(EstadoReserva.CONFIRMADA, simulacion.getEstado());
        assertEquals("FLEXIBLE", simulacion.getPolitica());
        assertEquals(500000.0, simulacion.getValorTotal());
        assertEquals(500000.0, simulacion.getValorDevolucion());
        assertTrue(simulacion.isAplicaDevolucion());
        assertEquals("Si cancelas ahora se te devolverían 500000.0", simulacion.getMensaje());
    }

    @Test
    void justoEnElLimiteDelPlazoTodaviaDevuelveElTotal() {
        llegaraEnHoras(24);

        SimulacionCancelacionDTO simulacion = servicio.simularCancelacion(31, 20);

        assertEquals(500000.0, simulacion.getValorDevolucion());
        assertTrue(simulacion.isAplicaDevolucion());
    }

    @Test
    void unaReservaPendienteTambienSePuedeSimular() {
        reserva.setEstado(EstadoReserva.PENDIENTE);

        SimulacionCancelacionDTO simulacion = servicio.simularCancelacion(31, 20);

        assertEquals(EstadoReserva.PENDIENTE, simulacion.getEstado());
        assertEquals(500000.0, simulacion.getValorDevolucion());
    }

    // Escenario 2
    @Test
    void cancelandoFueraDelPlazoLaDevolucionEsCeroYElMensajeLoIndica() {
        llegaraEnHoras(2);

        SimulacionCancelacionDTO simulacion = servicio.simularCancelacion(31, 20);

        assertEquals(0.0, simulacion.getValorDevolucion());
        assertFalse(simulacion.isAplicaDevolucion());
        assertEquals(500000.0, simulacion.getValorTotal());
        assertTrue(simulacion.getMensaje().contains("no recibirías devolución"));
        assertTrue(simulacion.getMensaje().contains("0"));
    }

    @Test
    void siLaPublicacionNoTienePoliticaLaDevolucionEsCeroYNoHayCodigoDePolitica() {
        reserva.getPublicacion().setPoliticaCancelacion(null);

        SimulacionCancelacionDTO simulacion = servicio.simularCancelacion(31, 20);

        assertEquals(0.0, simulacion.getValorDevolucion());
        assertFalse(simulacion.isAplicaDevolucion());
        assertNull(simulacion.getPolitica());
    }

    // Escenario 3
    @Test
    void elValorSimuladoEsElMismoQueSeDevuelveAlConfirmarLaCancelacion() {
        SimulacionCancelacionDTO simulacion = servicio.simularCancelacion(31, 20);
        ReservaCancelarRequestDTO solicitud = new ReservaCancelarRequestDTO();
        solicitud.setIdHuesped(20);
        solicitud.setMotivo("Cambio de planes");

        ReservaCanceladaDTO cancelada = servicio.cancelar(31, solicitud);

        assertEquals(simulacion.getValorDevolucion(), cancelada.getValorDevolucion());
        ArgumentCaptor<Pago> pago = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pago.capture());
        assertEquals(Pago.TIPO_DEVOLUCION, pago.getValue().getTipoMovimiento());
        assertEquals(500000.0, pago.getValue().getValor());
        assertTrue(pago.getValue().getIdTransaccion().startsWith("SIM-"));
    }

    @Test
    void elValorSimuladoFueraDelPlazoEsElMismoQueSeRegistraAlCancelar() {
        llegaraEnHoras(2);
        SimulacionCancelacionDTO simulacion = servicio.simularCancelacion(31, 20);
        ReservaCancelarRequestDTO solicitud = new ReservaCancelarRequestDTO();
        solicitud.setIdHuesped(20);
        solicitud.setMotivo("Cambio de planes");

        ReservaCanceladaDTO cancelada = servicio.cancelar(31, solicitud);

        assertEquals(0.0, simulacion.getValorDevolucion());
        assertEquals(simulacion.getValorDevolucion(), cancelada.getValorDevolucion());
        verifyNoInteractions(pagoRepository);
    }

    @Test
    void simularNoCancelaNiGuardaNada() {
        servicio.simularCancelacion(31, 20);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertNoEscribioNada();
    }

    @Test
    void siLaReservaNoEsDelHuespedResponde403() {
        Huesped otro = huesped(21, "otro@correo.com");
        when(huespedRepository.findById(21)).thenReturn(Optional.of(otro));

        assertFalla(HttpStatus.FORBIDDEN, 21);

        assertNoEscribioNada();
    }

    @Test
    void siElHuespedNoExisteResponde403() {
        assertFalla(HttpStatus.FORBIDDEN, 99);

        assertNoEscribioNada();
    }

    @Test
    void siLaCuentaDelHuespedNoEstaActivaResponde403() {
        huesped.getUsuario().setEstadoCuenta(EstadoCuenta.DESACTIVADA);

        assertFalla(HttpStatus.FORBIDDEN, 20);
    }

    @Test
    void siElHuespedEstaRestringidoResponde403() {
        huesped.getUsuario().setRestringido(true);

        assertFalla(HttpStatus.FORBIDDEN, 20);
    }

    @Test
    void siLaReservaNoExisteResponde404() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.simularCancelacion(99, 20));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void siLaReservaYaNoSePuedeCancelarResponde409ConSuEstado() {
        for (EstadoReserva estado : List.of(EstadoReserva.CANCELADA, EstadoReserva.RECHAZADA,
                EstadoReserva.COMPLETADA)) {
            reserva.setEstado(estado);

            ResponseStatusException error = assertFalla(HttpStatus.CONFLICT, 20);

            assertTrue(error.getReason().contains(estado.name()));
        }
        assertNoEscribioNada();
    }
}
