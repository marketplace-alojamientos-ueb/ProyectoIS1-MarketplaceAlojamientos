package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Notificacion;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.NotificacionRepository;
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
import static org.mockito.Mockito.*;

/** Pruebas de las reservas en espera de aprobación (MA-60). El anfitrión 7 es quien decide. */
class ReservaServiceImplTest {

    private ReservaRepository reservaRepository;
    private AnfitrionRepository anfitrionRepository;
    private PublicacionService publicacionService;
    private NotificacionRepository notificacionRepository;
    private ReservaServiceImpl servicio;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        anfitrionRepository = mock(AnfitrionRepository.class);
        publicacionService = mock(PublicacionService.class);
        notificacionRepository = mock(NotificacionRepository.class);
        servicio = new ReservaServiceImpl(reservaRepository, anfitrionRepository, publicacionService,
                notificacionRepository);

        Anfitrion anfitrion = anfitrion(7, "1012345678", "ana@correo.com");
        Anfitrion otroAnfitrion = anfitrion(8, "1011111111", "otro@correo.com");
        when(anfitrionRepository.findById(7)).thenReturn(Optional.of(anfitrion));
        when(anfitrionRepository.findById(8)).thenReturn(Optional.of(otroAnfitrion));

        Publicacion publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        publicacion.setTitulo("Cabaña con vista al lago");
        publicacion.setAnfitrion(anfitrion);
        Usuario usuarioHuesped = new Usuario("1098765432", "Luis", null, "Pérez", "Soto", "luis@correo.com", "hash");
        usuarioHuesped.setIdUsuario(20);
        Huesped huesped = new Huesped(usuarioHuesped);
        huesped.setIdUsuario(20);

        reserva = new Reserva();
        reserva.setIdReserva(31);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaLlegada(LocalDate.parse("2026-12-20"));
        reserva.setFechaSalida(LocalDate.parse("2026-12-23"));
        reserva.setNumHuespedes(3);
        reserva.setPublicacion(publicacion);
        reserva.setHuesped(huesped);
        when(reservaRepository.findById(31)).thenReturn(Optional.of(reserva));
    }

    @Test
    void listarPendientesDevuelveLasReservasPendientesDelAnfitrion() {
        when(reservaRepository.findByPublicacion_Anfitrion_IdUsuarioAndEstadoOrderByFechaLlegadaAsc(
                7, EstadoReserva.PENDIENTE)).thenReturn(List.of(reserva));

        List<ReservaPendienteDTO> resultado = servicio.listarPendientes(7);

        assertEquals(1, resultado.size());
        assertEquals(31, resultado.get(0).getIdReserva());
        assertEquals(EstadoReserva.PENDIENTE, resultado.get(0).getEstado());
        assertEquals("Cabaña con vista al lago", resultado.get(0).getTituloPublicacion());
        assertEquals("Luis Pérez Soto", resultado.get(0).getNombreHuesped());
    }

    @Test
    void listarPendientesSinReservasDevuelveListaVacia() {
        assertTrue(servicio.listarPendientes(7).isEmpty());
    }

    @Test
    void listarPendientesDeUnUsuarioQueNoEsAnfitrionResponde403() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listarPendientes(50));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void aceptarConfirmaLaReservaBloqueaLasFechasYNotificaAlHuesped() {
        MensajeResponseDTO respuesta = servicio.aceptar(31, 7);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
        verify(publicacionService).bloquearPeriodoReservado(15, LocalDate.parse("2026-12-20"),
                LocalDate.parse("2026-12-23"));
        ArgumentCaptor<Notificacion> notificacion = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(notificacion.capture());
        assertEquals(20, notificacion.getValue().getUsuario().getIdUsuario());
        assertEquals("RESERVA_CONFIRMADA", notificacion.getValue().getTipo());
        assertEquals("La reserva fue confirmada y se notificó al huésped", respuesta.getMensaje());
    }

    @Test
    void rechazarMarcaLaReservaComoRechazadaNotificaAlHuespedYNoTocaLasFechas() {
        MensajeResponseDTO respuesta = servicio.rechazar(31, 7);

        assertEquals(EstadoReserva.RECHAZADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
        verifyNoInteractions(publicacionService);
        ArgumentCaptor<Notificacion> notificacion = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(notificacion.capture());
        assertEquals(20, notificacion.getValue().getUsuario().getIdUsuario());
        assertEquals("RESERVA_RECHAZADA", notificacion.getValue().getTipo());
        assertEquals("La reserva fue rechazada y se notificó al huésped", respuesta.getMensaje());
    }

    @Test
    void aceptarUnaReservaDeOtroAnfitrionResponde403SinCambios() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.aceptar(31, 8));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        verifyNoInteractions(publicacionService, notificacionRepository);
    }

    @Test
    void aceptarUnaReservaQueYaNoEstaPendienteResponde409SinCambios() {
        reserva.setEstado(EstadoReserva.RECHAZADA);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> servicio.aceptar(31, 7));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(EstadoReserva.RECHAZADA, reserva.getEstado());
        verifyNoInteractions(publicacionService, notificacionRepository);
    }

    private Anfitrion anfitrion(int id, String cedula, String correo) {
        Usuario usuario = new Usuario(cedula, "Ana", "María", "Gómez", "Rojas", correo, "hash");
        usuario.setIdUsuario(id);
        Anfitrion anfitrion = new Anfitrion(usuario);
        anfitrion.setIdUsuario(id);
        return anfitrion;
    }
}
