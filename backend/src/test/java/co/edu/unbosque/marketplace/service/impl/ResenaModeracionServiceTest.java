package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaModerarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaReportadaDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaReportarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaResponseDTO;
import co.edu.unbosque.marketplace.model.entity.AccionModeracion;
import co.edu.unbosque.marketplace.model.entity.Admin;
import co.edu.unbosque.marketplace.model.entity.EventoAuditoria;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Notificacion;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Resena;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.repository.AccionModeracionRepository;
import co.edu.unbosque.marketplace.repository.AdminRepository;
import co.edu.unbosque.marketplace.repository.EventoAuditoriaRepository;
import co.edu.unbosque.marketplace.repository.HuespedRepository;
import co.edu.unbosque.marketplace.repository.NotificacionRepository;
import co.edu.unbosque.marketplace.repository.ResenaRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** MA-82: reportar una reseña y que un administrador la modere (ocultarla o mantenerla). */
class ResenaModeracionServiceTest {

    private ResenaRepository resenaRepository;
    private UsuarioRepository usuarioRepository;
    private AdminRepository adminRepository;
    private AccionModeracionRepository accionModeracionRepository;
    private EventoAuditoriaRepository eventoAuditoriaRepository;
    private NotificacionRepository notificacionRepository;
    private ResenaServiceImpl servicio;
    private Resena resena;
    private Usuario autor;
    private Usuario anfitrion;
    private Admin admin;
    private List<EventoAuditoria> eventos;

    @BeforeEach
    void setUp() {
        resenaRepository = mock(ResenaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        adminRepository = mock(AdminRepository.class);
        accionModeracionRepository = mock(AccionModeracionRepository.class);
        eventoAuditoriaRepository = mock(EventoAuditoriaRepository.class);
        notificacionRepository = mock(NotificacionRepository.class);
        servicio = new ResenaServiceImpl(resenaRepository, mock(ReservaRepository.class), mock(HuespedRepository.class),
                usuarioRepository, adminRepository, accionModeracionRepository, eventoAuditoriaRepository,
                notificacionRepository);

        autor = usuario(20, "Luis");
        anfitrion = usuario(7, "Ana");
        Usuario usuarioAdmin = usuario(4, "Admin");
        admin = new Admin(usuarioAdmin);
        when(usuarioRepository.findById(20)).thenReturn(Optional.of(autor));
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(anfitrion));
        when(adminRepository.findById(4)).thenReturn(Optional.of(admin));

        Huesped huesped = new Huesped(autor);
        huesped.setIdUsuario(20);
        Publicacion publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        publicacion.setTitulo("Cabaña con vista al lago");
        Reserva reserva = new Reserva();
        reserva.setIdReserva(31);
        reserva.setEstado(EstadoReserva.COMPLETADA);
        reserva.setHuesped(huesped);
        reserva.setPublicacion(publicacion);

        resena = new Resena();
        resena.setIdResena(5);
        resena.setReserva(reserva);
        resena.setCalificacion(1);
        resena.setComentario("Pésimo");
        resena.setFecha(LocalDate.now());
        resena.setVisible(true);
        when(resenaRepository.findById(5)).thenReturn(Optional.of(resena));

        // Los eventos de auditoría "guardados" quedan en esta lista para que las consultas los vean
        eventos = new ArrayList<>();
        when(eventoAuditoriaRepository.save(any(EventoAuditoria.class))).thenAnswer(inv -> {
            EventoAuditoria evento = inv.getArgument(0);
            evento.setIdEvento(eventos.size() + 1);
            eventos.add(evento);
            return evento;
        });
        when(eventoAuditoriaRepository.findByEntidadAfectadaAndIdEntidadOrderByIdEventoAsc(
                eq(EventoAuditoria.ENTIDAD_RESENA), anyInt())).thenAnswer(inv -> eventos.stream()
                .filter(e -> e.getIdEntidad().equals(inv.getArgument(1)))
                .toList());
        when(eventoAuditoriaRepository.findByEntidadAfectadaOrderByIdEventoAsc(EventoAuditoria.ENTIDAD_RESENA))
                .thenAnswer(inv -> List.copyOf(eventos));
    }

    private Usuario usuario(int id, String nombre) {
        Usuario usuario = new Usuario("10987654" + id, nombre, null, "Pérez", "Soto", nombre + id + "@correo.com", "hash");
        usuario.setIdUsuario(id);
        return usuario;
    }

    private ResenaReportarDTO reporte(int idUsuario, String motivo) {
        ResenaReportarDTO dto = new ResenaReportarDTO();
        dto.setIdUsuario(idUsuario);
        dto.setMotivo(motivo);
        return dto;
    }

    private ResenaModerarDTO decision(int idAdmin, String motivo) {
        ResenaModerarDTO dto = new ResenaModerarDTO();
        dto.setIdAdmin(idAdmin);
        dto.setMotivo(motivo);
        return dto;
    }

    private void assertFalla(HttpStatus esperado, Executable accion) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, accion);
        assertEquals(esperado, error.getStatusCode());
    }

    // ---------- reportar ----------

    @Test
    void reportarDejaElReporteEnLaAuditoria() {
        MensajeResponseDTO respuesta = servicio.reportar(5, reporte(7, "  Lenguaje ofensivo  "));

        assertEquals("La reseña fue reportada, un administrador la va a revisar", respuesta.getMensaje());
        assertEquals(1, eventos.size());
        EventoAuditoria evento = eventos.get(0);
        assertEquals(EventoAuditoria.REPORTAR_RESENA, evento.getOperacion());
        assertEquals(EventoAuditoria.ENTIDAD_RESENA, evento.getEntidadAfectada());
        assertEquals(5, evento.getIdEntidad());
        assertSame(anfitrion, evento.getUsuario());
        assertEquals("Motivo: Lenguaje ofensivo", evento.getDetalle());
    }

    @Test
    void elAutorNoPuedeReportarSuPropiaResena() {
        assertFalla(HttpStatus.CONFLICT, () -> servicio.reportar(5, reporte(20, "Me arrepentí")));
        assertTrue(eventos.isEmpty());
    }

    @Test
    void unUsuarioNoPuedeReportarDosVecesLaMismaResenaPendiente() {
        servicio.reportar(5, reporte(7, "Ofensiva"));

        assertFalla(HttpStatus.CONFLICT, () -> servicio.reportar(5, reporte(7, "Otra vez")));
        assertEquals(1, eventos.size());
    }

    @Test
    void despuesDeQueElAdminDecideSePuedeVolverAReportar() {
        servicio.reportar(5, reporte(7, "Ofensiva"));
        servicio.mantener(5, decision(4, "No incumple las normas"));

        servicio.reportar(5, reporte(7, "Sigue siendo ofensiva"));

        assertEquals(3, eventos.size());
    }

    @Test
    void noSePuedeReportarUnaResenaOculta() {
        resena.setVisible(false);

        assertFalla(HttpStatus.CONFLICT, () -> servicio.reportar(5, reporte(7, "Ofensiva")));
    }

    @Test
    void unUsuarioInactivoRestringidoOInexistenteNoPuedeReportar() {
        anfitrion.desactivarCuenta();
        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.reportar(5, reporte(7, "Ofensiva")));

        anfitrion.activarCuenta();
        anfitrion.setRestringido(true);
        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.reportar(5, reporte(7, "Ofensiva")));

        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.reportar(5, reporte(99, "Ofensiva")));
    }

    @Test
    void reportarUnaResenaQueNoExisteResponde404() {
        assertFalla(HttpStatus.NOT_FOUND, () -> servicio.reportar(99, reporte(7, "Ofensiva")));
    }

    // ---------- listar reportadas ----------

    @Test
    void listaLasResenasConReportesPendientes() {
        Usuario otro = usuario(8, "Pedro");
        when(usuarioRepository.findById(8)).thenReturn(Optional.of(otro));
        servicio.reportar(5, reporte(7, "Ofensiva"));
        servicio.reportar(5, reporte(8, "Spam"));

        List<ResenaReportadaDTO> reportadas = servicio.listarReportadas(4);

        assertEquals(1, reportadas.size());
        ResenaReportadaDTO reportada = reportadas.get(0);
        assertEquals(5, reportada.getResena().getIdResena());
        assertEquals(2, reportada.getNumeroReportes());
        assertEquals(List.of("Motivo: Ofensiva", "Motivo: Spam"), reportada.getMotivos());
        assertNotNull(reportada.getUltimoReporte());
    }

    @Test
    void laMasRecienteReportadaSaleDePrimeras() {
        Resena otra = new Resena();
        otra.setIdResena(6);
        otra.setReserva(resena.getReserva());
        otra.setVisible(true);
        when(resenaRepository.findById(6)).thenReturn(Optional.of(otra));
        servicio.reportar(5, reporte(7, "Ofensiva"));
        servicio.reportar(6, reporte(7, "Spam"));
        eventos.get(0).setFecha(LocalDateTime.now().minusDays(1));

        List<ResenaReportadaDTO> reportadas = servicio.listarReportadas(4);

        assertEquals(List.of(6, 5), reportadas.stream().map(r -> r.getResena().getIdResena()).toList());
    }

    @Test
    void unaResenaMantenidaUOcultadaNoSaleEnLaLista() {
        servicio.reportar(5, reporte(7, "Ofensiva"));
        servicio.mantener(5, decision(4, "No incumple las normas"));
        assertTrue(servicio.listarReportadas(4).isEmpty());

        servicio.reportar(5, reporte(7, "Ofensiva otra vez"));
        servicio.ocultar(5, decision(4, "Contenido ofensivo"));
        assertTrue(servicio.listarReportadas(4).isEmpty());
    }

    @Test
    void siLaResenaFueEliminadaNoSaleEnLaLista() {
        servicio.reportar(5, reporte(7, "Ofensiva"));
        when(resenaRepository.findById(5)).thenReturn(Optional.empty());

        assertTrue(servicio.listarReportadas(4).isEmpty());
    }

    @Test
    void soloUnAdministradorActivoVeLasReportadas() {
        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.listarReportadas(99));

        admin.getUsuario().desactivarCuenta();
        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.listarReportadas(4));
    }

    // ---------- ocultar ----------

    @Test
    void ocultarGuardaElMotivoLaAccionLaAuditoriaYNotificaAlAutor() {
        ResenaResponseDTO oculta = servicio.ocultar(5, decision(4, "  Contenido ofensivo  "));

        assertFalse(oculta.getVisible());
        assertFalse(resena.getVisible());
        assertEquals("Contenido ofensivo", resena.getMotivoOcultamiento());
        verify(resenaRepository).save(resena);

        ArgumentCaptor<AccionModeracion> accion = ArgumentCaptor.forClass(AccionModeracion.class);
        verify(accionModeracionRepository).save(accion.capture());
        assertSame(admin, accion.getValue().getAdmin());
        assertEquals(AccionModeracion.OCULTAMIENTO_REVIEW, accion.getValue().getTipoAccion());
        assertEquals(AccionModeracion.ENTIDAD_RESENA, accion.getValue().getEntidadAfectada());
        assertEquals(5, accion.getValue().getIdEntidad());
        assertEquals("Contenido ofensivo", accion.getValue().getMotivo());

        assertEquals(EventoAuditoria.OCULTAR_RESENA, eventos.get(0).getOperacion());
        assertEquals("OCULTADA", eventos.get(0).getResultado());

        ArgumentCaptor<Notificacion> notificacion = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(notificacion.capture());
        assertSame(autor, notificacion.getValue().getUsuario());
        assertEquals("RESENA_OCULTADA", notificacion.getValue().getTipo());
        assertTrue(notificacion.getValue().getMensaje().contains("Cabaña con vista al lago"));
    }

    @Test
    void noSePuedeOcultarDosVeces() {
        resena.setVisible(false);

        assertFalla(HttpStatus.CONFLICT, () -> servicio.ocultar(5, decision(4, "Ofensiva")));
        verifyNoInteractions(accionModeracionRepository, notificacionRepository);
    }

    @Test
    void soloUnAdministradorPuedeOcultar() {
        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.ocultar(5, decision(99, "Ofensiva")));
        assertTrue(resena.getVisible());
        verifyNoInteractions(accionModeracionRepository);
    }

    @Test
    void ocultarUnaResenaQueNoExisteResponde404() {
        assertFalla(HttpStatus.NOT_FOUND, () -> servicio.ocultar(99, decision(4, "Ofensiva")));
    }

    // ---------- mantener ----------

    @Test
    void mantenerDejaLaResenaVisibleYRegistraLaDecision() {
        servicio.reportar(5, reporte(7, "Ofensiva"));

        MensajeResponseDTO respuesta = servicio.mantener(5, decision(4, "No incumple las normas"));

        assertEquals("Se revisaron los reportes y la reseña sigue visible", respuesta.getMensaje());
        assertTrue(resena.getVisible());
        EventoAuditoria decision = eventos.get(1);
        assertEquals(EventoAuditoria.MANTENER_RESENA, decision.getOperacion());
        assertSame(admin.getUsuario(), decision.getUsuario());
        assertEquals("Motivo: No incumple las normas", decision.getDetalle());
        verifyNoInteractions(accionModeracionRepository);
    }

    @Test
    void noSePuedeMantenerSinReportesPendientes() {
        assertFalla(HttpStatus.CONFLICT, () -> servicio.mantener(5, decision(4, "Nada que revisar")));
        assertTrue(eventos.isEmpty());
    }

    @Test
    void noSePuedeMantenerUnaResenaOculta() {
        resena.setVisible(false);

        assertFalla(HttpStatus.CONFLICT, () -> servicio.mantener(5, decision(4, "No incumple")));
    }

    @Test
    void soloUnAdministradorPuedeMantener() {
        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.mantener(5, decision(99, "No incumple")));
    }
}
