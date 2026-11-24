package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.ReservaListadoDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Cotizacion;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
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
import co.edu.unbosque.marketplace.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaListadoServiceTest {

    private ReservaRepository reservaRepository;
    private HuespedRepository huespedRepository;
    private AnfitrionRepository anfitrionRepository;
    private ReservaServiceImpl servicio;
    private Huesped huesped;
    private Anfitrion anfitrion;
    private Reserva confirmada;
    private Reserva cancelada;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        huespedRepository = mock(HuespedRepository.class);
        anfitrionRepository = mock(AnfitrionRepository.class);
        servicio = new ReservaServiceImpl(reservaRepository, anfitrionRepository, mock(PublicacionService.class),
                mock(NotificacionRepository.class), huespedRepository, mock(PagoRepository.class),
                mock(CancelacionRepository.class), mock(EventoAuditoriaRepository.class),
                mock(StripeService.class), "http://localhost:4200");

        Usuario usuarioHuesped = new Usuario("1098765432", "Luis", null, "Pérez", "Soto", "luis@correo.com", "hash");
        usuarioHuesped.setIdUsuario(20);
        huesped = new Huesped(usuarioHuesped);
        huesped.setIdUsuario(20);

        Usuario usuarioAnfitrion = new Usuario("1012345678", "Ana", null, "Gómez", "Rojas", "ana@correo.com", "hash");
        usuarioAnfitrion.setIdUsuario(7);
        anfitrion = new Anfitrion(usuarioAnfitrion);
        anfitrion.setIdUsuario(7);

        when(huespedRepository.findById(20)).thenReturn(Optional.of(huesped));
        when(anfitrionRepository.findById(7)).thenReturn(Optional.of(anfitrion));

        confirmada = reserva(31, EstadoReserva.CONFIRMADA, LocalDate.parse("2026-12-20"));
        cancelada = reserva(30, EstadoReserva.CANCELADA, LocalDate.parse("2026-11-10"));
        when(reservaRepository.findByHuesped_IdUsuarioOrderByFechaLlegadaDesc(20))
                .thenReturn(List.of(confirmada, cancelada));
        when(reservaRepository.findByPublicacion_Anfitrion_IdUsuarioOrderByFechaLlegadaDesc(7))
                .thenReturn(List.of(confirmada, cancelada));
    }

    private Reserva reserva(int id, EstadoReserva estado, LocalDate llegada) {
        Publicacion publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        publicacion.setTitulo("Cabaña con vista al lago");
        publicacion.setAnfitrion(anfitrion);

        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setTotal(500000.0);

        Reserva reserva = new Reserva();
        reserva.setIdReserva(id);
        reserva.setEstado(estado);
        reserva.setFechaLlegada(llegada);
        reserva.setFechaSalida(llegada.plusDays(3));
        reserva.setNumHuespedes(3);
        reserva.setHuesped(huesped);
        reserva.setPublicacion(publicacion);
        reserva.setCotizacion(cotizacion);
        return reserva;
    }

    private void desactivarUsuario(Usuario usuario) {
        usuario.setEstadoCuenta(EstadoCuenta.DESACTIVADA);
    }

    @Test
    void listarDelHuespedSinFiltroDevuelveTodasSusReservasConSusDatos() {
        List<ReservaListadoDTO> resultado = servicio.listarDelHuesped(20, null);

        assertEquals(2, resultado.size());
        ReservaListadoDTO primera = resultado.get(0);
        assertEquals(31, primera.getIdReserva());
        assertEquals(EstadoReserva.CONFIRMADA, primera.getEstado());
        assertEquals(LocalDate.parse("2026-12-20"), primera.getFechaLlegada());
        assertEquals(LocalDate.parse("2026-12-23"), primera.getFechaSalida());
        assertEquals(3, primera.getNumHuespedes());
        assertEquals(500000.0, primera.getTotal());
        assertEquals(15, primera.getIdPublicacion());
        assertEquals("Cabaña con vista al lago", primera.getTituloPublicacion());
        assertEquals(20, primera.getIdHuesped());
        assertEquals("Luis Pérez Soto", primera.getNombreHuesped());
        assertEquals(7, primera.getIdAnfitrion());
        assertEquals("Ana Gómez Rojas", primera.getNombreAnfitrion());
    }

    @Test
    void listarDelHuespedConEstadoSoloDevuelveLasDeEseEstado() {
        List<ReservaListadoDTO> resultado = servicio.listarDelHuesped(20, EstadoReserva.CANCELADA);

        assertEquals(1, resultado.size());
        assertEquals(30, resultado.get(0).getIdReserva());
    }

    @Test
    void listarDelHuespedSinReservasDelEstadoPedidoDevuelveListaVacia() {
        assertTrue(servicio.listarDelHuesped(20, EstadoReserva.PENDIENTE).isEmpty());
    }

    @Test
    void listarDelHuespedQueNoExisteResponde403() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listarDelHuesped(99, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void listarDelHuespedConLaCuentaInactivaResponde403() {
        desactivarUsuario(huesped.getUsuario());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listarDelHuesped(20, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void listarDelHuespedRestringidoResponde403() {
        huesped.getUsuario().setRestringido(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listarDelHuesped(20, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void listarDelAnfitrionSinFiltroDevuelveTodasLasReservasDeSusAlojamientos() {
        List<ReservaListadoDTO> resultado = servicio.listarDelAnfitrion(7, null);

        assertEquals(2, resultado.size());
        assertEquals(31, resultado.get(0).getIdReserva());
        assertEquals(30, resultado.get(1).getIdReserva());
        assertEquals("Luis Pérez Soto", resultado.get(0).getNombreHuesped());
        assertEquals(7, resultado.get(0).getIdAnfitrion());
    }

    @Test
    void listarDelAnfitrionConEstadoSoloDevuelveLasDeEseEstado() {
        List<ReservaListadoDTO> resultado = servicio.listarDelAnfitrion(7, EstadoReserva.CONFIRMADA);

        assertEquals(1, resultado.size());
        assertEquals(31, resultado.get(0).getIdReserva());
    }

    @Test
    void listarDelAnfitrionSinReservasDevuelveListaVacia() {
        when(reservaRepository.findByPublicacion_Anfitrion_IdUsuarioOrderByFechaLlegadaDesc(7)).thenReturn(List.of());

        assertTrue(servicio.listarDelAnfitrion(7, null).isEmpty());
    }

    @Test
    void listarDelAnfitrionQueNoExisteResponde403() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listarDelAnfitrion(99, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void listarDelAnfitrionRestringidoResponde403() {
        anfitrion.getUsuario().setRestringido(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listarDelAnfitrion(7, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(reservaRepository);
    }
}
