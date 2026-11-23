package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.ReservaCanceladaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarRequestDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Cancelacion;
import co.edu.unbosque.marketplace.model.entity.Cotizacion;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Notificacion;
import co.edu.unbosque.marketplace.model.entity.Pago;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import co.edu.unbosque.marketplace.model.enums.EstadoCuenta;
import co.edu.unbosque.marketplace.model.enums.EstadoDevolucion;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.CancelacionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas de cancelar una reserva como huésped (MA-95). El huésped 20 cancela la reserva 31 de la publicación 15,
 * cuyo anfitrión es el 7.
 */
class ReservaCancelacionServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private ReservaRepository reservaRepository;
    private PublicacionService publicacionService;
    private NotificacionRepository notificacionRepository;
    private HuespedRepository huespedRepository;
    private PagoRepository pagoRepository;
    private CancelacionRepository cancelacionRepository;
    private ReservaServiceImpl servicio;
    private Reserva reserva;
    private Huesped huesped;
    private LocalDate llegada;
    private LocalDate salida;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        publicacionService = mock(PublicacionService.class);
        notificacionRepository = mock(NotificacionRepository.class);
        huespedRepository = mock(HuespedRepository.class);
        pagoRepository = mock(PagoRepository.class);
        cancelacionRepository = mock(CancelacionRepository.class);
        servicio = new ReservaServiceImpl(reservaRepository, mock(AnfitrionRepository.class), publicacionService,
                notificacionRepository, huespedRepository, pagoRepository, cancelacionRepository);

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

        llegada = LocalDate.now().plusDays(10);
        salida = llegada.plusDays(3);
        reserva = new Reserva();
        reserva.setIdReserva(31);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaLlegada(llegada);
        reserva.setFechaSalida(salida);
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

    private ReservaCancelarRequestDTO solicitud(int idHuesped, String motivo) {
        ReservaCancelarRequestDTO dto = new ReservaCancelarRequestDTO();
        dto.setIdHuesped(idHuesped);
        dto.setMotivo(motivo);
        return dto;
    }

    private ResponseStatusException assertFalla(HttpStatus esperado, ReservaCancelarRequestDTO dto) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.cancelar(31, dto));
        assertEquals(esperado, error.getStatusCode());
        return error;
    }

    private void assertNadaCambio(EstadoReserva estadoEsperado) {
        assertEquals(estadoEsperado, reserva.getEstado());
        verify(reservaRepository, never()).save(any());
        verify(publicacionService, never()).liberarPeriodoReservado(any(), any(), any());
        verifyNoInteractions(cancelacionRepository, pagoRepository, notificacionRepository);
    }

    // Escenario 1
    @Test
    void cancelaUnaReservaConfirmadaAplicaLaPoliticaLiberaElPeriodoYNotifica() {
        ReservaCanceladaDTO respuesta = servicio.cancelar(31, solicitud(20, "Cambio de planes"));

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
        verify(publicacionService).liberarPeriodoReservado(15, llegada, salida);

        ArgumentCaptor<Cancelacion> cancelacion = ArgumentCaptor.forClass(Cancelacion.class);
        verify(cancelacionRepository).save(cancelacion.capture());
        assertEquals(reserva, cancelacion.getValue().getReserva());
        assertEquals(huesped.getUsuario(), cancelacion.getValue().getUsuario());
        assertEquals("Cambio de planes", cancelacion.getValue().getMotivo());
        assertEquals(500000.0, cancelacion.getValue().getValorDevolucion());
        assertEquals(EstadoDevolucion.DEVUELTA, cancelacion.getValue().getEstadoDevolucion());

        ArgumentCaptor<Pago> pago = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pago.capture());
        assertEquals(Pago.TIPO_DEVOLUCION, pago.getValue().getTipoMovimiento());
        assertEquals(Pago.APROBADO, pago.getValue().getEstado());
        assertEquals(500000.0, pago.getValue().getValor());
        assertEquals("MARKETPLACE", pago.getValue().getEmisor());
        assertEquals("HUESPED-20", pago.getValue().getReceptor());
        assertTrue(pago.getValue().getIdTransaccion().startsWith("SIM-"));

        ArgumentCaptor<Notificacion> notificaciones = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(2)).save(notificaciones.capture());
        List<Notificacion> guardadas = notificaciones.getAllValues();
        assertEquals(20, guardadas.get(0).getUsuario().getIdUsuario());
        assertEquals("RESERVA_CANCELADA", guardadas.get(0).getTipo());
        assertEquals(7, guardadas.get(1).getUsuario().getIdUsuario());
        assertEquals("RESERVA_CANCELADA_POR_HUESPED", guardadas.get(1).getTipo());

        assertEquals(31, respuesta.getIdReserva());
        assertEquals(EstadoReserva.CANCELADA, respuesta.getEstado());
        assertEquals(500000.0, respuesta.getValorDevolucion());
        assertEquals(EstadoDevolucion.DEVUELTA, respuesta.getEstadoDevolucion());
    }

    @Test
    void cancelandoSobreLaMarcaDeLaPoliticaNoHayDevolucionNiMovimientoDePago() {
        reserva.setFechaLlegada(LocalDate.now().plusDays(1));
        reserva.getPublicacion().setPoliticaCancelacion(new PoliticaFlexible(Clock.fixed(
                reserva.getFechaLlegada().atStartOfDay(ZONA).minusHours(2).toInstant(), ZONA)));

        ReservaCanceladaDTO respuesta = servicio.cancelar(31, solicitud(20, "Cambio de planes"));

        ArgumentCaptor<Cancelacion> cancelacion = ArgumentCaptor.forClass(Cancelacion.class);
        verify(cancelacionRepository).save(cancelacion.capture());
        assertEquals(0.0, cancelacion.getValue().getValorDevolucion());
        assertEquals(EstadoDevolucion.SIN_DEVOLUCION, cancelacion.getValue().getEstadoDevolucion());
        verifyNoInteractions(pagoRepository);
        assertEquals(EstadoDevolucion.SIN_DEVOLUCION, respuesta.getEstadoDevolucion());
        assertEquals(EstadoReserva.CANCELADA, respuesta.getEstado());
    }

    @Test
    void siLaPublicacionNoTienePoliticaCancelaSinDevolucion() {
        reserva.getPublicacion().setPoliticaCancelacion(null);

        ReservaCanceladaDTO respuesta = servicio.cancelar(31, solicitud(20, "Cambio de planes"));

        assertEquals(EstadoDevolucion.SIN_DEVOLUCION, respuesta.getEstadoDevolucion());
        verifyNoInteractions(pagoRepository);
    }

    @Test
    void unaReservaPendienteSeCancelaSinLiberarPeriodoPorqueNuncaSeBloqueo() {
        reserva.setEstado(EstadoReserva.PENDIENTE);

        ReservaCanceladaDTO respuesta = servicio.cancelar(31, solicitud(20, "Cambio de planes"));

        assertEquals(EstadoReserva.CANCELADA, respuesta.getEstado());
        verify(publicacionService, never()).liberarPeriodoReservado(any(), any(), any());
        verify(cancelacionRepository).save(any(Cancelacion.class));
    }

    @Test
    void elMotivoSeGuardaSinEspaciosSobrantes() {
        servicio.cancelar(31, solicitud(20, "  Cambio de planes  "));

        ArgumentCaptor<Cancelacion> cancelacion = ArgumentCaptor.forClass(Cancelacion.class);
        verify(cancelacionRepository).save(cancelacion.capture());
        assertEquals("Cambio de planes", cancelacion.getValue().getMotivo());
    }

    // Escenario 2
    @Test
    void siLaReservaNoEsDelHuespedResponde403YNoCambiaNada() {
        Huesped otro = huesped(21, "otro@correo.com");
        when(huespedRepository.findById(21)).thenReturn(Optional.of(otro));

        assertFalla(HttpStatus.FORBIDDEN, solicitud(21, "Cambio de planes"));

        assertNadaCambio(EstadoReserva.CONFIRMADA);
    }

    @Test
    void siElHuespedNoExisteResponde403() {
        assertFalla(HttpStatus.FORBIDDEN, solicitud(99, "Cambio de planes"));

        assertNadaCambio(EstadoReserva.CONFIRMADA);
    }

    @Test
    void siLaCuentaDelHuespedNoEstaActivaResponde403() {
        huesped.getUsuario().setEstadoCuenta(EstadoCuenta.DESACTIVADA);

        assertFalla(HttpStatus.FORBIDDEN, solicitud(20, "Cambio de planes"));

        assertNadaCambio(EstadoReserva.CONFIRMADA);
    }

    @Test
    void siElHuespedEstaRestringidoResponde403() {
        huesped.getUsuario().setRestringido(true);

        assertFalla(HttpStatus.FORBIDDEN, solicitud(20, "Cambio de planes"));

        assertNadaCambio(EstadoReserva.CONFIRMADA);
    }

    @Test
    void siLaReservaNoExisteResponde404() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.cancelar(99, solicitud(20, "Cambio de planes")));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(cancelacionRepository, pagoRepository, notificacionRepository);
    }

    // Escenario 3
    @Test
    void siLaReservaYaEstaCanceladaResponde409ConSuEstadoYNoCambiaNada() {
        reserva.setEstado(EstadoReserva.CANCELADA);

        ResponseStatusException error = assertFalla(HttpStatus.CONFLICT, solicitud(20, "Cambio de planes"));

        assertTrue(error.getReason().contains("CANCELADA"));
        assertNadaCambio(EstadoReserva.CANCELADA);
    }

    @Test
    void unaReservaRechazadaOCompletadaTampocoSePuedeCancelar() {
        for (EstadoReserva estado : List.of(EstadoReserva.RECHAZADA, EstadoReserva.COMPLETADA,
                EstadoReserva.EN_REVISION_MANUAL)) {
            reserva.setEstado(estado);

            ResponseStatusException error = assertFalla(HttpStatus.CONFLICT, solicitud(20, "Cambio de planes"));

            assertTrue(error.getReason().contains(estado.name()));
        }
        assertNadaCambio(EstadoReserva.EN_REVISION_MANUAL);
    }
}
