package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmadaDTO;
import co.edu.unbosque.marketplace.model.dto.SesionStripeDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Cotizacion;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Pago;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.CancelacionRepository;
import co.edu.unbosque.marketplace.repository.EventoAuditoriaRepository;
import co.edu.unbosque.marketplace.repository.HuespedRepository;
import co.edu.unbosque.marketplace.repository.NotificacionRepository;
import co.edu.unbosque.marketplace.repository.PagoRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import co.edu.unbosque.marketplace.service.PublicacionService;
import co.edu.unbosque.marketplace.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pago de una reserva con Stripe Checkout: paso 1 (crear la sesión de pago) y paso 2 (confirmar el pago y crear la
 * reserva). Stripe está simulado con Mockito. El huésped 20 reserva la publicación 15.
 */
class ReservaPagoStripeServiceTest {

    private static final String SESION = "cs_test_123";
    private static final String COBRO = "pi_test_456";

    private ReservaRepository reservaRepository;
    private PublicacionService publicacionService;
    private NotificacionRepository notificacionRepository;
    private PagoRepository pagoRepository;
    private StripeService stripeService;
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
        pagoRepository = mock(PagoRepository.class);
        stripeService = mock(StripeService.class);
        HuespedRepository huespedRepository = mock(HuespedRepository.class);
        servicio = new ReservaServiceImpl(reservaRepository, mock(AnfitrionRepository.class), publicacionService,
                notificacionRepository, huespedRepository, pagoRepository, mock(CancelacionRepository.class),
                mock(EventoAuditoriaRepository.class), stripeService, "http://localhost:4200");

        Usuario usuarioHuesped = new Usuario("1098765432", "Luis", null, "Pérez", "Soto", "luis@correo.com", "hash");
        usuarioHuesped.setIdUsuario(20);
        huesped = new Huesped(usuarioHuesped);
        huesped.setIdUsuario(20);
        when(huespedRepository.findById(20)).thenReturn(Optional.of(huesped));

        Usuario usuarioOtro = new Usuario("1098765499", "Pedro", null, "Ruiz", "Mora", "pedro@correo.com", "hash");
        usuarioOtro.setIdUsuario(21);
        Huesped otro = new Huesped(usuarioOtro);
        otro.setIdUsuario(21);
        when(huespedRepository.findById(21)).thenReturn(Optional.of(otro));

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

    private ReservaCheckoutRequestDTO solicitud(int numHuespedes) {
        ReservaCheckoutRequestDTO dto = new ReservaCheckoutRequestDTO();
        dto.setIdHuesped(20);
        dto.setIdPublicacion(15);
        dto.setFechaLlegada(llegada);
        dto.setFechaSalida(salida);
        dto.setNumHuespedes(numHuespedes);
        return dto;
    }

    /** Sesión como la devolvería Stripe después de que el huésped 20 pagó 500.000 por 3 noches. */
    private SesionStripeDTO sesionPagada() {
        Map<String, String> datos = new HashMap<>();
        datos.put("idHuesped", "20");
        datos.put("idPublicacion", "15");
        datos.put("fechaLlegada", llegada.toString());
        datos.put("fechaSalida", salida.toString());
        datos.put("numHuespedes", "3");
        SesionStripeDTO sesion = new SesionStripeDTO();
        sesion.setIdSesion(SESION);
        sesion.setPagada(true);
        sesion.setIdTransaccion(COBRO);
        sesion.setTotalPagado(500000.0);
        sesion.setMetadatos(datos);
        when(stripeService.consultarSesion(SESION)).thenReturn(sesion);
        return sesion;
    }

    private void assertFalla(HttpStatus esperado, Executable accion) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, accion);
        assertEquals(esperado, error.getStatusCode());
    }

    private void assertNoSeCreoLaReserva() {
        verify(reservaRepository, never()).save(any());
        verify(publicacionService, never()).bloquearPeriodoReservado(any(), any(), any());
        verifyNoInteractions(pagoRepository, notificacionRepository);
    }

    // ---------- Paso 1: crear la sesión de pago ----------

    @Test
    @SuppressWarnings("unchecked")
    void creaLaSesionDePagoConElTotalDeLaCotizacionYSinCrearLaReserva() {
        SesionStripeDTO creada = new SesionStripeDTO();
        creada.setIdSesion(SESION);
        creada.setUrlPago("https://checkout.stripe.com/c/pay/" + SESION);
        when(stripeService.crearSesionDePago(anyDouble(), any(), any(), any(), any(), any(), any())).thenReturn(creada);

        ReservaCheckoutDTO respuesta = servicio.iniciarPagoStripe(solicitud(3));

        ArgumentCaptor<Map<String, String>> datos = ArgumentCaptor.forClass(Map.class);
        verify(stripeService).crearSesionDePago(eq(500000.0), eq("Reserva en Cabaña con vista al lago"),
                eq("3 noches, del " + llegada + " al " + salida + ", 3 huéspedes"), eq("luis@correo.com"),
                eq("http://localhost:4200/reservas/pago-exitoso?session_id={CHECKOUT_SESSION_ID}"),
                eq("http://localhost:4200/publicaciones/15?pago=cancelado"), datos.capture());
        assertEquals(Map.of("idHuesped", "20", "idPublicacion", "15", "fechaLlegada", llegada.toString(),
                "fechaSalida", salida.toString(), "numHuespedes", "3"), datos.getValue());

        assertEquals(SESION, respuesta.getIdSesion());
        assertEquals("https://checkout.stripe.com/c/pay/" + SESION, respuesta.getUrlPago());
        assertEquals(15, respuesta.getIdPublicacion());
        assertEquals(3, respuesta.getNoches());
        assertEquals(450000.0, respuesta.getPrecioBase());
        assertEquals(50000.0, respuesta.getTarifaLimpieza());
        assertEquals(500000.0, respuesta.getTotal());
        assertNoSeCreoLaReserva();
    }

    @Test
    void elDetalleDeUnaNocheYUnHuespedVaEnSingular() {
        LocalDate unaNoche = llegada.plusDays(1);
        when(publicacionService.obtenerParaReservar(15, llegada, unaNoche)).thenReturn(publicacion);
        when(stripeService.crearSesionDePago(anyDouble(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new SesionStripeDTO());
        ReservaCheckoutRequestDTO dto = solicitud(1);
        dto.setFechaSalida(unaNoche);

        servicio.iniciarPagoStripe(dto);

        verify(stripeService).crearSesionDePago(eq(200000.0), any(),
                eq("1 noche, del " + llegada + " al " + unaNoche + ", 1 huésped"), any(), any(), any(), any());
    }

    @Test
    void conMasHuespedesQueLaCapacidadNoCreaLaSesion() {
        assertFalla(HttpStatus.BAD_REQUEST, () -> servicio.iniciarPagoStripe(solicitud(5)));
        verifyNoInteractions(stripeService);
    }

    @Test
    void conFechasInvalidasNoCreaLaSesion() {
        ReservaCheckoutRequestDTO dto = solicitud(3);
        dto.setFechaSalida(llegada);

        assertFalla(HttpStatus.BAD_REQUEST, () -> servicio.iniciarPagoStripe(dto));
        verifyNoInteractions(stripeService);
    }

    @Test
    void siLasFechasNoEstanDisponiblesNoCreaLaSesion() {
        when(publicacionService.obtenerParaReservar(15, llegada, salida))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Se perdió la disponibilidad"));

        assertFalla(HttpStatus.CONFLICT, () -> servicio.iniciarPagoStripe(solicitud(3)));
        verifyNoInteractions(stripeService);
    }

    @Test
    void unHuespedInactivoNoPuedeEmpezarElPago() {
        huesped.getUsuario().desactivarCuenta();

        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.iniciarPagoStripe(solicitud(3)));
        verifyNoInteractions(stripeService);
    }

    // ---------- Paso 2: confirmar el pago ----------

    @Test
    void conLaSesionPagadaCreaLaReservaConfirmadaBloqueaElPeriodoYGuardaElCobroDeStripe() {
        sesionPagada();

        ReservaConfirmadaDTO respuesta = servicio.confirmarPagoStripe(SESION, 20);

        ArgumentCaptor<Reserva> reserva = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).save(reserva.capture());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getValue().getEstado());
        assertEquals(SESION, reserva.getValue().getClaveIdempotencia());
        assertEquals(3, reserva.getValue().getNumHuespedes());
        verify(publicacionService).bloquearPeriodoReservado(15, llegada, salida);

        ArgumentCaptor<Pago> pago = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pago.capture());
        assertEquals(COBRO, pago.getValue().getIdTransaccion());
        assertEquals(500000.0, pago.getValue().getValor());
        assertEquals(Pago.APROBADO, pago.getValue().getEstado());

        assertEquals(COBRO, respuesta.getIdTransaccion());
        assertEquals(500000.0, respuesta.getTotal());
        assertEquals("El pago fue aprobado y la reserva quedó confirmada", respuesta.getMensaje());
        verify(notificacionRepository, times(2)).save(any());
        verify(stripeService, never()).reembolsar(any(), any());
    }

    @Test
    void conAprobacionLaReservaQuedaPendienteSinBloquearElPeriodo() {
        publicacion.setModoAceptacion(ModoAceptacion.CON_APROBACION);
        sesionPagada();

        ReservaConfirmadaDTO respuesta = servicio.confirmarPagoStripe(SESION, 20);

        assertEquals(EstadoReserva.PENDIENTE, respuesta.getEstado());
        verify(publicacionService, never()).bloquearPeriodoReservado(any(), any(), any());
    }

    @Test
    void siYaSeConfirmoDevuelveLaMismaReservaSinCrearOtra() {
        sesionPagada();
        Reserva hecha = new Reserva();
        hecha.setIdReserva(31);
        hecha.setEstado(EstadoReserva.CONFIRMADA);
        hecha.setHuesped(huesped);
        hecha.setPublicacion(publicacion);
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setTotal(500000.0);
        hecha.setCotizacion(cotizacion);
        when(reservaRepository.findByClaveIdempotencia(SESION)).thenReturn(Optional.of(hecha));

        ReservaConfirmadaDTO respuesta = servicio.confirmarPagoStripe(SESION, 20);

        assertEquals(31, respuesta.getIdReserva());
        verify(reservaRepository, never()).save(any());
        verify(pagoRepository, never()).save(any());
        verifyNoInteractions(notificacionRepository);
        verify(publicacionService, never()).esperarTurnoParaReservar(any());
    }

    @Test
    void siOtraConfirmacionTerminoMientrasEsperabaDevuelveEsaReserva() {
        sesionPagada();
        Reserva hecha = new Reserva();
        hecha.setIdReserva(31);
        hecha.setHuesped(huesped);
        hecha.setPublicacion(publicacion);
        hecha.setCotizacion(new Cotizacion());
        when(reservaRepository.findByClaveIdempotencia(SESION)).thenReturn(Optional.empty(), Optional.of(hecha));

        assertEquals(31, servicio.confirmarPagoStripe(SESION, 20).getIdReserva());
        verify(publicacionService).esperarTurnoParaReservar(15);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void siLaSesionNoEstaPagadaNoCreaLaReserva() {
        sesionPagada().setPagada(false);

        assertFalla(HttpStatus.PAYMENT_REQUIRED, () -> servicio.confirmarPagoStripe(SESION, 20));
        assertNoSeCreoLaReserva();
        verify(stripeService, never()).reembolsar(any(), any());
    }

    @Test
    void unHuespedNoPuedeConfirmarLaSesionDeOtro() {
        sesionPagada();

        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.confirmarPagoStripe(SESION, 21));
        assertNoSeCreoLaReserva();
    }

    @Test
    void unaSesionSinDatosDeReservaNoSeAcepta() {
        sesionPagada().setMetadatos(null);

        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.confirmarPagoStripe(SESION, 20));
        assertNoSeCreoLaReserva();
    }

    @Test
    void siSePerdioLaDisponibilidadMientrasPagabaDevuelveElPagoYNoCreaLaReserva() {
        sesionPagada();
        when(publicacionService.obtenerParaReservar(15, llegada, salida)).thenThrow(new ResponseStatusException(
                HttpStatus.CONFLICT, "Se perdió la disponibilidad: las fechas seleccionadas ya no están disponibles"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.confirmarPagoStripe(SESION, 20));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertTrue(error.getReason().endsWith("se devolvió el pago completo a tu tarjeta"));
        verify(stripeService).reembolsar(COBRO, "reembolso-" + SESION);
        assertNoSeCreoLaReserva();
    }

    @Test
    void siCambioElPrecioMientrasPagabaDevuelveElPagoYNoCreaLaReserva() {
        sesionPagada();
        publicacion.setPrecioPorNoche(180000.0);

        assertFalla(HttpStatus.CONFLICT, () -> servicio.confirmarPagoStripe(SESION, 20));
        verify(stripeService).reembolsar(COBRO, "reembolso-" + SESION);
        assertNoSeCreoLaReserva();
    }

    @Test
    void unHuespedInactivoNoPuedeConfirmar() {
        huesped.getUsuario().desactivarCuenta();

        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.confirmarPagoStripe(SESION, 20));
        verifyNoInteractions(stripeService);
    }

    @Test
    void siLaSesionNoExisteEnStripeResponde404() {
        when(stripeService.consultarSesion("cs_test_no_existe"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "La sesión de pago no existe"));

        assertFalla(HttpStatus.NOT_FOUND, () -> servicio.confirmarPagoStripe("cs_test_no_existe", 20));
        assertNoSeCreoLaReserva();
    }
}
