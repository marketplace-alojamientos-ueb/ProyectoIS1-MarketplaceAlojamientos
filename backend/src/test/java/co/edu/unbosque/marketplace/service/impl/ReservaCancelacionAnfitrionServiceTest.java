package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.ReservaCanceladaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarAnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Cancelacion;
import co.edu.unbosque.marketplace.model.entity.Cotizacion;
import co.edu.unbosque.marketplace.model.entity.EventoAuditoria;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservaCancelacionAnfitrionServiceTest {

    private ReservaRepository reservaRepository;
    private AnfitrionRepository anfitrionRepository;
    private PublicacionService publicacionService;
    private NotificacionRepository notificacionRepository;
    private PagoRepository pagoRepository;
    private CancelacionRepository cancelacionRepository;
    private EventoAuditoriaRepository eventoAuditoriaRepository;
    private ReservaServiceImpl servicio;
    private Reserva reserva;
    private Anfitrion anfitrion;
    private LocalDate llegada;
    private LocalDate salida;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        anfitrionRepository = mock(AnfitrionRepository.class);
        publicacionService = mock(PublicacionService.class);
        notificacionRepository = mock(NotificacionRepository.class);
        pagoRepository = mock(PagoRepository.class);
        cancelacionRepository = mock(CancelacionRepository.class);
        eventoAuditoriaRepository = mock(EventoAuditoriaRepository.class);
        servicio = new ReservaServiceImpl(reservaRepository, anfitrionRepository, publicacionService,
                notificacionRepository, mock(HuespedRepository.class), pagoRepository, cancelacionRepository,
                eventoAuditoriaRepository);

        anfitrion = anfitrion(7, "ana@correo.com");
        when(anfitrionRepository.findById(7)).thenReturn(Optional.of(anfitrion));

        Usuario usuarioHuesped = new Usuario("1098765432", "Luis", null, "Pérez", "Soto", "luis@correo.com", "hash");
        usuarioHuesped.setIdUsuario(20);
        Huesped huesped = new Huesped(usuarioHuesped);
        huesped.setIdUsuario(20);

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

    private Anfitrion anfitrion(int id, String correo) {
        Usuario usuario = new Usuario("10123456" + id, "Ana", null, "Gómez", "Rojas", correo, "hash");
        usuario.setIdUsuario(id);
        Anfitrion nuevo = new Anfitrion(usuario);
        nuevo.setIdUsuario(id);
        return nuevo;
    }

    private ReservaCancelarAnfitrionRequestDTO solicitud(int idAnfitrion, String motivo) {
        ReservaCancelarAnfitrionRequestDTO dto = new ReservaCancelarAnfitrionRequestDTO();
        dto.setIdAnfitrion(idAnfitrion);
        dto.setMotivo(motivo);
        return dto;
    }

    private ResponseStatusException assertFalla(HttpStatus esperado, ReservaCancelarAnfitrionRequestDTO dto) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.cancelarComoAnfitrion(31, dto));
        assertEquals(esperado, error.getStatusCode());
        return error;
    }

    private void assertNadaCambio(EstadoReserva estadoEsperado) {
        assertEquals(estadoEsperado, reserva.getEstado());
        verify(reservaRepository, never()).save(any());
        verify(publicacionService, never()).liberarPeriodoReservado(any(), any(), any());
        verifyNoInteractions(cancelacionRepository, pagoRepository, notificacionRepository, eventoAuditoriaRepository);
    }

    @Test
    void cancelaUnaReservaConfirmadaLiberaElPeriodoRegistraLaCancelacionYNotificaAlHuesped() {
        ReservaCanceladaDTO respuesta = servicio.cancelarComoAnfitrion(31, solicitud(7, "Imprevisto en la propiedad"));

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
        verify(publicacionService).liberarPeriodoReservado(15, llegada, salida);

        ArgumentCaptor<Cancelacion> cancelacion = ArgumentCaptor.forClass(Cancelacion.class);
        verify(cancelacionRepository).save(cancelacion.capture());
        assertEquals(reserva, cancelacion.getValue().getReserva());
        assertEquals(anfitrion.getUsuario(), cancelacion.getValue().getUsuario());
        assertEquals("Imprevisto en la propiedad", cancelacion.getValue().getMotivo());
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

        ArgumentCaptor<EventoAuditoria> evento = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(eventoAuditoriaRepository).save(evento.capture());
        assertEquals(anfitrion.getUsuario(), evento.getValue().getUsuario());
        assertEquals(EventoAuditoria.CANCELAR_RESERVA, evento.getValue().getOperacion());
        assertEquals(31, evento.getValue().getIdEntidad());
        assertEquals("CANCELADA", evento.getValue().getResultado());
        assertTrue(evento.getValue().getDetalle().contains("Imprevisto en la propiedad"));

        ArgumentCaptor<Notificacion> notificaciones = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(2)).save(notificaciones.capture());
        List<Notificacion> guardadas = notificaciones.getAllValues();
        assertEquals(20, guardadas.get(0).getUsuario().getIdUsuario());
        assertEquals("RESERVA_CANCELADA_POR_ANFITRION", guardadas.get(0).getTipo());
        assertTrue(guardadas.get(0).getMensaje().contains("Imprevisto en la propiedad"));
        assertTrue(guardadas.get(0).getMensaje().contains("500000.0"));
        assertEquals(7, guardadas.get(1).getUsuario().getIdUsuario());
        assertEquals("RESERVA_CANCELADA", guardadas.get(1).getTipo());

        assertEquals(31, respuesta.getIdReserva());
        assertEquals(EstadoReserva.CANCELADA, respuesta.getEstado());
        assertEquals(500000.0, respuesta.getValorDevolucion());
        assertEquals(EstadoDevolucion.DEVUELTA, respuesta.getEstadoDevolucion());
        assertEquals("La reserva fue cancelada y se devolvió 500000.0", respuesta.getMensaje());
    }

    @Test
    void elAnfitrionSiempreDevuelveElTotalAunqueFaltePocoParaLaLlegada() {
        reserva.setFechaLlegada(LocalDate.now().plusDays(1));
        reserva.getPublicacion().setPoliticaCancelacion(new PoliticaFlexible(java.time.Clock.fixed(
                reserva.getFechaLlegada().atStartOfDay(java.time.ZoneId.systemDefault()).minusHours(2).toInstant(),
                java.time.ZoneId.systemDefault())));

        ReservaCanceladaDTO respuesta = servicio.cancelarComoAnfitrion(31, solicitud(7, "Imprevisto"));

        assertEquals(500000.0, respuesta.getValorDevolucion());
        assertEquals(EstadoDevolucion.DEVUELTA, respuesta.getEstadoDevolucion());
    }

    @Test
    void siLaPublicacionNoTienePoliticaElAnfitrionIgualDevuelveElTotal() {
        reserva.getPublicacion().setPoliticaCancelacion(null);

        ReservaCanceladaDTO respuesta = servicio.cancelarComoAnfitrion(31, solicitud(7, "Imprevisto"));

        assertEquals(500000.0, respuesta.getValorDevolucion());
        verify(pagoRepository).save(any(Pago.class));
    }

    @Test
    void unaReservaPendienteSeCancelaSinLiberarPeriodoPorqueNuncaSeBloqueo() {
        reserva.setEstado(EstadoReserva.PENDIENTE);

        ReservaCanceladaDTO respuesta = servicio.cancelarComoAnfitrion(31, solicitud(7, "Imprevisto"));

        assertEquals(EstadoReserva.CANCELADA, respuesta.getEstado());
        verify(publicacionService, never()).liberarPeriodoReservado(any(), any(), any());
        verify(cancelacionRepository).save(any(Cancelacion.class));
    }

    @Test
    void elMotivoSeGuardaSinEspaciosSobrantes() {
        servicio.cancelarComoAnfitrion(31, solicitud(7, "  Imprevisto  "));

        ArgumentCaptor<Cancelacion> cancelacion = ArgumentCaptor.forClass(Cancelacion.class);
        verify(cancelacionRepository).save(cancelacion.capture());
        assertEquals("Imprevisto", cancelacion.getValue().getMotivo());
    }

    @Test
    void siLaReservaNoEsDeUnAlojamientoDelAnfitrionResponde403YNoCambiaNada() {
        Anfitrion otro = anfitrion(8, "otro@correo.com");
        when(anfitrionRepository.findById(8)).thenReturn(Optional.of(otro));

        assertFalla(HttpStatus.FORBIDDEN, solicitud(8, "Imprevisto"));

        assertNadaCambio(EstadoReserva.CONFIRMADA);
    }

    @Test
    void siElAnfitrionNoExisteResponde403() {
        assertFalla(HttpStatus.FORBIDDEN, solicitud(99, "Imprevisto"));

        assertNadaCambio(EstadoReserva.CONFIRMADA);
    }

    @Test
    void siLaCuentaDelAnfitrionNoEstaActivaResponde403() {
        anfitrion.getUsuario().setEstadoCuenta(EstadoCuenta.DESACTIVADA);

        assertFalla(HttpStatus.FORBIDDEN, solicitud(7, "Imprevisto"));

        assertNadaCambio(EstadoReserva.CONFIRMADA);
    }

    @Test
    void siElAnfitrionEstaRestringidoResponde403() {
        anfitrion.getUsuario().setRestringido(true);

        assertFalla(HttpStatus.FORBIDDEN, solicitud(7, "Imprevisto"));

        assertNadaCambio(EstadoReserva.CONFIRMADA);
    }

    @Test
    void siLaReservaNoExisteResponde404() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.cancelarComoAnfitrion(99, solicitud(7, "Imprevisto")));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(cancelacionRepository, pagoRepository, notificacionRepository, eventoAuditoriaRepository);
    }

    @Test
    void siLaReservaYaEstaCanceladaResponde409ConSuEstadoYNoCambiaNada() {
        reserva.setEstado(EstadoReserva.CANCELADA);

        ResponseStatusException error = assertFalla(HttpStatus.CONFLICT, solicitud(7, "Imprevisto"));

        assertTrue(error.getReason().contains("CANCELADA"));
        assertNadaCambio(EstadoReserva.CANCELADA);
    }

    @Test
    void unaReservaRechazadaOCompletadaTampocoSePuedeCancelar() {
        for (EstadoReserva estado : List.of(EstadoReserva.RECHAZADA, EstadoReserva.COMPLETADA)) {
            reserva.setEstado(estado);

            ResponseStatusException error = assertFalla(HttpStatus.CONFLICT, solicitud(7, "Imprevisto"));

            assertTrue(error.getReason().contains(estado.name()));
        }
        assertNadaCambio(EstadoReserva.COMPLETADA);
    }
}
