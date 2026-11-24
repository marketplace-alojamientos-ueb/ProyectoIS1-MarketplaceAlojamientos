package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.EventoAuditoriaDTO;
import co.edu.unbosque.marketplace.model.entity.Admin;
import co.edu.unbosque.marketplace.model.entity.EventoAuditoria;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.repository.AdminRepository;
import co.edu.unbosque.marketplace.repository.EventoAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditoriaServiceImplTest {

    private EventoAuditoriaRepository eventoRepository;
    private AdminRepository adminRepository;
    private AuditoriaServiceImpl servicio;
    private Usuario usuarioAdmin;

    @BeforeEach
    void setUp() {
        eventoRepository = mock(EventoAuditoriaRepository.class);
        adminRepository = mock(AdminRepository.class);
        servicio = new AuditoriaServiceImpl(eventoRepository, adminRepository);

        usuarioAdmin = new Usuario("1000000004", "Admin", null, "Prueba", "Cuatro", "admin@correo.com", "hash");
        usuarioAdmin.setIdUsuario(4);
        Admin admin = new Admin(usuarioAdmin);
        admin.setIdUsuario(4);
        when(adminRepository.findById(4)).thenReturn(Optional.of(admin));
    }

    private EventoAuditoria evento(int id, Usuario usuario, String operacion) {
        EventoAuditoria evento = new EventoAuditoria();
        evento.setIdEvento(id);
        evento.setUsuario(usuario);
        evento.setOperacion(operacion);
        evento.setEntidadAfectada(EventoAuditoria.ENTIDAD_RESERVA);
        evento.setIdEntidad(31);
        evento.setFecha(LocalDateTime.of(2030, 1, 10, 9, 30));
        evento.setResultado("CANCELADA");
        evento.setDetalle("Motivo: Cambio de planes");
        return evento;
    }

    @Test
    void elAdministradorVeLosEventosConSuRegistroCompleto() {
        Usuario huesped = new Usuario("1098765432", "Luis", null, "Pérez", "Soto", "luis@correo.com", "hash");
        huesped.setIdUsuario(20);
        when(eventoRepository.findAllByOrderByFechaDescIdEventoDesc())
                .thenReturn(List.of(evento(12, huesped, EventoAuditoria.CANCELAR_RESERVA)));

        List<EventoAuditoriaDTO> eventos = servicio.listarEventos(4, null);

        assertEquals(1, eventos.size());
        EventoAuditoriaDTO dto = eventos.get(0);
        assertEquals(12, dto.getIdEvento());
        assertEquals(20, dto.getIdUsuario());
        assertEquals("CANCELAR_RESERVA", dto.getOperacion());
        assertEquals("RESERVA", dto.getEntidadAfectada());
        assertEquals(31, dto.getIdEntidad());
        assertEquals(LocalDateTime.of(2030, 1, 10, 9, 30), dto.getFecha());
        assertEquals("CANCELADA", dto.getResultado());
        assertEquals("Motivo: Cambio de planes", dto.getDetalle());
    }

    @Test
    void unEventoSinUsuarioSeMuestraConIdUsuarioNulo() {
        when(eventoRepository.findAllByOrderByFechaDescIdEventoDesc())
                .thenReturn(List.of(evento(13, null, "LOGIN_FALLIDO")));

        assertNull(servicio.listarEventos(4, "").get(0).getIdUsuario());
    }

    @Test
    void sinEventosResponderUnaListaVacia() {
        when(eventoRepository.findAllByOrderByFechaDescIdEventoDesc()).thenReturn(List.of());

        assertTrue(servicio.listarEventos(4, "  ").isEmpty());
    }

    @Test
    void conUnaOperacionSoloTraeLosEventosDeEsaOperacion() {
        when(eventoRepository.findByOperacionOrderByFechaDescIdEventoDesc("CANCELAR_RESERVA"))
                .thenReturn(List.of(evento(12, usuarioAdmin, EventoAuditoria.CANCELAR_RESERVA)));

        List<EventoAuditoriaDTO> eventos = servicio.listarEventos(4, " CANCELAR_RESERVA ");

        assertEquals(1, eventos.size());
        verify(eventoRepository, never()).findAllByOrderByFechaDescIdEventoDesc();
    }

    @Test
    void siElIdNoEsDeUnAdministradorResponde403() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listarEventos(99, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(eventoRepository);
    }

    @Test
    void siLaCuentaDelAdministradorNoEstaActivaResponde403() {
        usuarioAdmin.desactivarCuenta();

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listarEventos(4, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(eventoRepository);
    }

    @Test
    void siElAdministradorEstaRestringidoResponde403() {
        usuarioAdmin.setRestringido(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listarEventos(4, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(eventoRepository);
    }
}
