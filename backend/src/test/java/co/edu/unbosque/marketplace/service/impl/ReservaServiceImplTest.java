package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas de las reservas en espera de aprobación (MA-60). El anfitrión 7 es quien consulta. */
class ReservaServiceImplTest {

    private ReservaRepository reservaRepository;
    private AnfitrionRepository anfitrionRepository;
    private ReservaServiceImpl servicio;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        anfitrionRepository = mock(AnfitrionRepository.class);
        servicio = new ReservaServiceImpl(reservaRepository, anfitrionRepository);

        Usuario usuario = new Usuario("1012345678", "Ana", "María", "Gómez", "Rojas", "ana@correo.com", "hash");
        usuario.setIdUsuario(7);
        Anfitrion anfitrion = new Anfitrion(usuario);
        anfitrion.setIdUsuario(7);
        when(anfitrionRepository.findById(7)).thenReturn(Optional.of(anfitrion));
    }

    @Test
    void listarPendientesDevuelveLasReservasPendientesDelAnfitrion() {
        Publicacion publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        publicacion.setTitulo("Cabaña con vista al lago");
        Usuario usuarioHuesped = new Usuario("1098765432", "Luis", null, "Pérez", "Soto", "luis@correo.com", "hash");
        Huesped huesped = new Huesped(usuarioHuesped);
        huesped.setIdUsuario(20);
        Reserva reserva = new Reserva();
        reserva.setIdReserva(31);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaLlegada(LocalDate.parse("2026-12-20"));
        reserva.setFechaSalida(LocalDate.parse("2026-12-23"));
        reserva.setNumHuespedes(3);
        reserva.setPublicacion(publicacion);
        reserva.setHuesped(huesped);
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
}
