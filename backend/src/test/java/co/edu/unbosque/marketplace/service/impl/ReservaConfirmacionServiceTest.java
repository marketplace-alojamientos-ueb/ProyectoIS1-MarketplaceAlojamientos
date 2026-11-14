package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.ReservaConfirmadaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmarRequestDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Cotizacion;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Notificacion;
import co.edu.unbosque.marketplace.model.entity.Pago;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoCuenta;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.HuespedRepository;
import co.edu.unbosque.marketplace.repository.NotificacionRepository;
import co.edu.unbosque.marketplace.repository.PagoRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import co.edu.unbosque.marketplace.service.PublicacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas de confirmar una reserva con pago simulado (MA-102) y de evitar reservas duplicadas ante reintentos (MA-103).
 * El huésped 20 reserva la publicación 15.
 */
class ReservaConfirmacionServiceTest {

    private static final String TARJETA_APROBADA = "4242424242424242";
    private static final String TARJETA_RECHAZADA = "4000000000000002";

    private ReservaRepository reservaRepository;
    private PublicacionService publicacionService;
    private NotificacionRepository notificacionRepository;
    private HuespedRepository huespedRepository;
    private PagoRepository pagoRepository;
    private ReservaServiceImpl servicio;
    private Publicacion publicacion;
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
        servicio = new ReservaServiceImpl(reservaRepository, mock(AnfitrionRepository.class), publicacionService,
                notificacionRepository, huespedRepository, pagoRepository);

        Usuario usuarioHuesped = new Usuario("1098765432", "Luis", null, "Pérez", "Soto", "luis@correo.com", "hash");
        usuarioHuesped.setIdUsuario(20);
        huesped = new Huesped(usuarioHuesped);
        huesped.setIdUsuario(20);
        when(huespedRepository.findById(20)).thenReturn(Optional.of(huesped));

        Usuario usuarioAnfitrion = new Usuario("1012345678", "Ana", null, "Gómez", "Rojas", "ana@correo.com", "hash");
        usuarioAnfitrion.setIdUsuario(7);
        Anfitrion anfitrion = new Anfitrion(usuarioAnfitrion);
        anfitrion.setIdUsuario(7);

        publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        publicacion.setTitulo("Cabaña con vista al lago");
        publicacion.setCapacidad(4);
        publicacion.setPrecioPorNoche(150000.0);
        publicacion.setTarifaLimpieza(50000.0);
        publicacion.setModoAceptacion(ModoAceptacion.INMEDIATA);
        publicacion.setAnfitrion(anfitrion);

        llegada = LocalDate.now().plusDays(10);
        salida = llegada.plusDays(3);
        when(publicacionService.obtenerParaReservar(15, llegada, salida)).thenReturn(publicacion);
    }

    private ReservaConfirmarRequestDTO solicitud(String tarjeta) {
        ReservaConfirmarRequestDTO dto = new ReservaConfirmarRequestDTO();
        dto.setIdHuesped(20);
        dto.setIdPublicacion(15);
        dto.setFechaLlegada(llegada);
        dto.setFechaSalida(salida);
        dto.setNumHuespedes(3);
        dto.setClaveIdempotencia("clave-1");
        dto.setNumeroTarjeta(tarjeta);
        return dto;
    }

    private ResponseStatusException assertFalla(HttpStatus esperado, ReservaConfirmarRequestDTO dto) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.confirmar(dto));
        assertEquals(esperado, error.getStatusCode());
        return error;
    }

    private void assertNadaCreado() {
        verify(reservaRepository, never()).save(any());
        verify(publicacionService, never()).bloquearPeriodoReservado(any(), any(), any());
        verifyNoInteractions(pagoRepository, notificacionRepository);
    }

    // Escenario 1
    @Test
    void conPeriodoDisponibleYPagoAprobadoCreaLaReservaConfirmadaBloqueaElPeriodoYRegistraLaConfirmacion() {
        ReservaConfirmadaDTO respuesta = servicio.confirmar(solicitud(TARJETA_APROBADA));

        ArgumentCaptor<Reserva> reserva = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).save(reserva.capture());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getValue().getEstado());
        assertEquals(huesped, reserva.getValue().getHuesped());
        assertEquals(3, reserva.getValue().getCotizacion().getNoches());
        assertEquals(500000.0, reserva.getValue().getCotizacion().getTotal());
        verify(publicacionService).bloquearPeriodoReservado(15, llegada, salida);

        ArgumentCaptor<Pago> pago = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pago.capture());
        assertEquals(Pago.APROBADO, pago.getValue().getEstado());
        assertEquals(Pago.TIPO_PAGO, pago.getValue().getTipoMovimiento());
        assertEquals(500000.0, pago.getValue().getValor());
        assertTrue(pago.getValue().getIdTransaccion().startsWith("SIM-"));

        ArgumentCaptor<Notificacion> notificaciones = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(2)).save(notificaciones.capture());
        List<String> tipos = notificaciones.getAllValues().stream().map(Notificacion::getTipo).toList();
        assertEquals(List.of("RESERVA_CONFIRMADA", "NUEVA_RESERVA"), tipos);

        assertEquals(EstadoReserva.CONFIRMADA, respuesta.getEstado());
        assertEquals(500000.0, respuesta.getTotal());
        assertEquals(pago.getValue().getIdTransaccion(), respuesta.getIdTransaccion());
        assertEquals("El pago fue aprobado y la reserva quedó confirmada", respuesta.getMensaje());
    }

    // Escenario 2
    @Test
    void siElPeriodoDejoDeEstarDisponibleRechazaSinProcesarElPago() {
        when(publicacionService.obtenerParaReservar(15, llegada, salida)).thenThrow(new ResponseStatusException(
                HttpStatus.CONFLICT, "Se perdió la disponibilidad: las fechas seleccionadas ya no están disponibles"));

        ResponseStatusException error = assertFalla(HttpStatus.CONFLICT, solicitud(TARJETA_APROBADA));

        assertTrue(error.getReason().contains("Se perdió la disponibilidad"));
        assertNadaCreado();
    }

    // Escenario 3
    @Test
    void siElPagoEsRechazadoNoCreaLaReservaNiBloqueaElPeriodo() {
        ResponseStatusException error = assertFalla(HttpStatus.PAYMENT_REQUIRED, solicitud(TARJETA_RECHAZADA));

        assertTrue(error.getReason().contains("El pago fue rechazado"));
        assertNadaCreado();
    }

    @Test
    void conPublicacionConAprobacionLaReservaQuedaPendienteSinBloquearElPeriodo() {
        publicacion.setModoAceptacion(ModoAceptacion.CON_APROBACION);

        ReservaConfirmadaDTO respuesta = servicio.confirmar(solicitud(TARJETA_APROBADA));

        assertEquals(EstadoReserva.PENDIENTE, respuesta.getEstado());
        verify(publicacionService, never()).bloquearPeriodoReservado(any(), any(), any());
        verify(pagoRepository).save(any(Pago.class));
        verify(notificacionRepository, times(2)).save(any(Notificacion.class));
    }

    // ---- MA-103: evitar reservas duplicadas ante reintentos ----

    private Reserva reservaYaPagada() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setNoches(3);
        cotizacion.setTotal(500000.0);
        Reserva hecha = new Reserva();
        hecha.setIdReserva(31);
        hecha.setEstado(EstadoReserva.CONFIRMADA);
        hecha.setHuesped(huesped);
        hecha.setPublicacion(publicacion);
        hecha.setCotizacion(cotizacion);
        Pago pago = new Pago();
        pago.setIdTransaccion("SIM-original");
        when(pagoRepository.findFirstByReserva_IdReservaAndTipoMovimiento(31, Pago.TIPO_PAGO))
                .thenReturn(Optional.of(pago));
        return hecha;
    }

    // Escenario 1
    @Test
    void unReintentoDevuelveLaReservaYaCreadaYNoGeneraOtra() {
        Reserva hecha = reservaYaPagada();
        when(reservaRepository.findByClaveIdempotencia("clave-1")).thenReturn(Optional.of(hecha));

        ReservaConfirmadaDTO respuesta = servicio.confirmar(solicitud(TARJETA_APROBADA));

        assertEquals(31, respuesta.getIdReserva());
        assertEquals(EstadoReserva.CONFIRMADA, respuesta.getEstado());
        verify(reservaRepository, never()).save(any());
        verifyNoInteractions(publicacionService, notificacionRepository);
    }

    // Escenario 1, con el reintento llegando al mismo tiempo que la original: al pasar el turno ya la encuentra
    @Test
    void unReintentoQueEsperoElTurnoDevuelveLaReservaQueTerminoMientrasEsperaba() {
        Reserva hecha = reservaYaPagada();
        when(reservaRepository.findByClaveIdempotencia("clave-1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(hecha));

        ReservaConfirmadaDTO respuesta = servicio.confirmar(solicitud(TARJETA_APROBADA));

        assertEquals(31, respuesta.getIdReserva());
        InOrder orden = inOrder(reservaRepository, publicacionService);
        orden.verify(reservaRepository).findByClaveIdempotencia("clave-1");
        orden.verify(publicacionService).esperarTurnoParaReservar(15);
        orden.verify(reservaRepository).findByClaveIdempotencia("clave-1");
        verify(publicacionService, never()).obtenerParaReservar(any(), any(), any());
        verify(reservaRepository, never()).save(any());
        verify(pagoRepository, never()).save(any());
        verifyNoInteractions(notificacionRepository);
    }

    // Escenario 2
    @Test
    void elReintentoDeUnaReservaYaPagadaNoProcesaElPagoOtraVez() {
        Reserva hecha = reservaYaPagada();
        when(reservaRepository.findByClaveIdempotencia("clave-1")).thenReturn(Optional.of(hecha));

        ReservaConfirmadaDTO respuesta = servicio.confirmar(solicitud(TARJETA_APROBADA));

        verify(pagoRepository, never()).save(any());
        assertEquals("SIM-original", respuesta.getIdTransaccion());
        assertEquals(500000.0, respuesta.getTotal());
        assertEquals("Esta reserva ya se había procesado, no se cobró de nuevo", respuesta.getMensaje());
    }

    // Escenario 3: la segunda solicitud (otra clave) pasa el turno cuando la primera ya bloqueó las fechas
    @Test
    void dosSolicitudesConFechasQueSeCruzanSoloConfirmanUnaYLaOtraEsRechazada() {
        when(publicacionService.obtenerParaReservar(15, llegada, salida))
                .thenReturn(publicacion)
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Se perdió la disponibilidad: las fechas seleccionadas ya no están disponibles"));
        ReservaConfirmarRequestDTO segunda = solicitud(TARJETA_APROBADA);
        segunda.setClaveIdempotencia("clave-2");
        segunda.setFechaLlegada(llegada.plusDays(1));
        segunda.setFechaSalida(salida.plusDays(1));
        when(publicacionService.obtenerParaReservar(15, llegada.plusDays(1), salida.plusDays(1)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Se perdió la disponibilidad: las fechas seleccionadas ya no están disponibles"));

        ReservaConfirmadaDTO primera = servicio.confirmar(solicitud(TARJETA_APROBADA));
        ResponseStatusException error = assertFalla(HttpStatus.CONFLICT, segunda);

        assertEquals(EstadoReserva.CONFIRMADA, primera.getEstado());
        assertTrue(error.getReason().contains("Se perdió la disponibilidad"));
        verify(reservaRepository, times(1)).save(any());
        verify(pagoRepository, times(1)).save(any());
        verify(publicacionService, times(1)).bloquearPeriodoReservado(any(), any(), any());
        // Las dos esperan su turno antes de revisar la disponibilidad
        InOrder orden = inOrder(publicacionService);
        orden.verify(publicacionService).esperarTurnoParaReservar(15);
        orden.verify(publicacionService).obtenerParaReservar(15, llegada, salida);
        orden.verify(publicacionService).esperarTurnoParaReservar(15);
        orden.verify(publicacionService).obtenerParaReservar(15, llegada.plusDays(1), salida.plusDays(1));
    }

    // Escenario 3: sin READ_COMMITTED, en MySQL la que esperó el turno no vería las fechas que bloqueó la otra
    @Test
    void confirmarYAceptarLeenLoQueDejoLaReservaQueTeniaElTurno() throws NoSuchMethodException {
        Method confirmar = ReservaServiceImpl.class.getMethod("confirmar", ReservaConfirmarRequestDTO.class);
        Method aceptar = ReservaServiceImpl.class.getMethod("aceptar", Integer.class, Integer.class);

        assertEquals(Isolation.READ_COMMITTED, confirmar.getAnnotation(Transactional.class).isolation());
        assertEquals(Isolation.READ_COMMITTED, aceptar.getAnnotation(Transactional.class).isolation());
    }

    @Test
    void unaClaveDeIdempotenciaDeOtroHuespedResponde409() {
        Huesped otro = new Huesped(new Usuario());
        otro.setIdUsuario(21);
        Reserva ajena = new Reserva();
        ajena.setHuesped(otro);
        when(reservaRepository.findByClaveIdempotencia("clave-1")).thenReturn(Optional.of(ajena));

        assertFalla(HttpStatus.CONFLICT, solicitud(TARJETA_APROBADA));
        assertNadaCreado();
    }

    @Test
    void conMasHuespedesQueLaCapacidadResponde400() {
        ReservaConfirmarRequestDTO dto = solicitud(TARJETA_APROBADA);
        dto.setNumHuespedes(5);

        assertFalla(HttpStatus.BAD_REQUEST, dto);
        assertNadaCreado();
    }

    @Test
    void conSalidaNoPosteriorALaLlegadaResponde400() {
        ReservaConfirmarRequestDTO dto = solicitud(TARJETA_APROBADA);
        dto.setFechaSalida(llegada);

        assertFalla(HttpStatus.BAD_REQUEST, dto);
        verifyNoInteractions(publicacionService);
    }

    @Test
    void conLlegadaEnElPasadoResponde400() {
        ReservaConfirmarRequestDTO dto = solicitud(TARJETA_APROBADA);
        dto.setFechaLlegada(LocalDate.now().minusDays(1));

        assertFalla(HttpStatus.BAD_REQUEST, dto);
        verifyNoInteractions(publicacionService);
    }

    @Test
    void unHuespedConLaCuentaDesactivadaResponde403() {
        huesped.getUsuario().setEstadoCuenta(EstadoCuenta.DESACTIVADA);

        assertFalla(HttpStatus.FORBIDDEN, solicitud(TARJETA_APROBADA));
        verifyNoInteractions(publicacionService);
    }

    @Test
    void unaPublicacionSinPrecioResponde409SinCobrar() {
        publicacion.setPrecioPorNoche(null);
        publicacion.setTarifaLimpieza(null);

        assertFalla(HttpStatus.CONFLICT, solicitud(TARJETA_APROBADA));
        assertNadaCreado();
    }
}
