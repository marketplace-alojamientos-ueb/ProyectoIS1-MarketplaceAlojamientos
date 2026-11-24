package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Huesped;
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
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import co.edu.unbosque.marketplace.repository.ResenaRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResenaServiceImplTest {

    private ResenaRepository resenaRepository;
    private ReservaRepository reservaRepository;
    private HuespedRepository huespedRepository;
    private ResenaServiceImpl servicio;
    private Huesped huesped;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        resenaRepository = mock(ResenaRepository.class);
        reservaRepository = mock(ReservaRepository.class);
        huespedRepository = mock(HuespedRepository.class);
        servicio = new ResenaServiceImpl(resenaRepository, reservaRepository, huespedRepository,
                mock(UsuarioRepository.class), mock(AdminRepository.class), mock(AccionModeracionRepository.class),
                mock(EventoAuditoriaRepository.class), mock(NotificacionRepository.class));

        huesped = huesped(20);
        when(huespedRepository.findById(20)).thenReturn(Optional.of(huesped));

        Publicacion publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        publicacion.setTitulo("Cabaña con vista al lago");

        reserva = new Reserva();
        reserva.setIdReserva(31);
        reserva.setEstado(EstadoReserva.COMPLETADA);
        reserva.setFechaLlegada(LocalDate.now().minusDays(5));
        reserva.setFechaSalida(LocalDate.now().minusDays(2));
        reserva.setHuesped(huesped);
        reserva.setPublicacion(publicacion);
        when(reservaRepository.findById(31)).thenReturn(Optional.of(reserva));
        when(resenaRepository.save(any(Resena.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Huesped huesped(int id) {
        Usuario usuario = new Usuario("10987654" + id, "Luis", null, "Pérez", "Soto", "luis" + id + "@correo.com", "hash");
        usuario.setIdUsuario(id);
        Huesped nuevo = new Huesped(usuario);
        nuevo.setIdUsuario(id);
        return nuevo;
    }

    private ResenaRequestDTO solicitud(String comentario) {
        ResenaRequestDTO dto = new ResenaRequestDTO();
        dto.setIdHuesped(20);
        dto.setIdReserva(31);
        dto.setCalificacion(4);
        dto.setComentario(comentario);
        return dto;
    }

    private Resena resenaGuardada(boolean visible) {
        Resena resena = new Resena();
        resena.setIdResena(5);
        resena.setReserva(reserva);
        resena.setCalificacion(3);
        resena.setComentario("Bien");
        resena.setFecha(LocalDate.now().minusDays(1));
        resena.setVisible(visible);
        when(resenaRepository.findById(5)).thenReturn(Optional.of(resena));
        return resena;
    }

    private ResenaActualizarDTO cambio(int idHuesped) {
        ResenaActualizarDTO dto = new ResenaActualizarDTO();
        dto.setIdHuesped(idHuesped);
        dto.setCalificacion(5);
        dto.setComentario("  Excelente  ");
        return dto;
    }

    private void assertFalla(HttpStatus esperado, Executable accion) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, accion);
        assertEquals(esperado, error.getStatusCode());
    }

    // ---------- crear ----------

    @Test
    void creaLaResenaDeUnaEstanciaCompletada() {
        ResenaResponseDTO creada = servicio.crear(solicitud("  Muy buena estancia  "));

        ArgumentCaptor<Resena> guardada = ArgumentCaptor.forClass(Resena.class);
        verify(resenaRepository).save(guardada.capture());
        assertSame(reserva, guardada.getValue().getReserva());
        assertEquals(LocalDate.now(), guardada.getValue().getFecha());
        assertTrue(guardada.getValue().getVisible());
        assertEquals(31, creada.getIdReserva());
        assertEquals(15, creada.getIdPublicacion());
        assertEquals("Cabaña con vista al lago", creada.getTituloPublicacion());
        assertEquals(20, creada.getIdHuesped());
        assertEquals(4, creada.getCalificacion());
        assertEquals("Muy buena estancia", creada.getComentario());
        assertTrue(creada.getVisible());
    }

    @Test
    void unComentarioEnBlancoSeGuardaComoNull() {
        assertNull(servicio.crear(solicitud("   ")).getComentario());
        assertNull(servicio.crear(solicitud(null)).getComentario());
    }

    @Test
    void unaReservaConfirmadaConLaSalidaYaPasadaCuentaComoCompletada() {
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaSalida(LocalDate.now());

        assertNotNull(servicio.crear(solicitud("Bien")));
    }

    @Test
    void noSePuedeResenarUnaReservaConfirmadaQueNoHaTerminado() {
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaSalida(LocalDate.now().plusDays(1));

        assertFalla(HttpStatus.CONFLICT, () -> servicio.crear(solicitud("Bien")));
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void noSePuedeResenarUnaReservaCanceladaAunqueYaHayaPasado() {
        reserva.setEstado(EstadoReserva.CANCELADA);

        assertFalla(HttpStatus.CONFLICT, () -> servicio.crear(solicitud("Bien")));
    }

    @Test
    void noSePuedeResenarDosVecesLaMismaReserva() {
        when(resenaRepository.existsByReserva_IdReserva(31)).thenReturn(true);

        assertFalla(HttpStatus.CONFLICT, () -> servicio.crear(solicitud("Otra")));
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void otroHuespedNoPuedeResenarLaReserva() {
        when(huespedRepository.findById(21)).thenReturn(Optional.of(huesped(21)));
        ResenaRequestDTO dto = solicitud("Bien");
        dto.setIdHuesped(21);

        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.crear(dto));
    }

    @Test
    void unHuespedInactivoORestringidoNoPuedeResenar() {
        huesped.getUsuario().desactivarCuenta();
        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.crear(solicitud("Bien")));

        huesped.getUsuario().activarCuenta();
        huesped.getUsuario().setRestringido(true);
        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.crear(solicitud("Bien")));
    }

    @Test
    void unHuespedQueNoExisteNoPuedeResenar() {
        ResenaRequestDTO dto = solicitud("Bien");
        dto.setIdHuesped(99);

        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.crear(dto));
    }

    @Test
    void siLaReservaNoExisteResponde404() {
        ResenaRequestDTO dto = solicitud("Bien");
        dto.setIdReserva(99);

        assertFalla(HttpStatus.NOT_FOUND, () -> servicio.crear(dto));
    }

    // ---------- consultar ----------

    @Test
    void obtieneUnaResenaVisible() {
        resenaGuardada(true);

        ResenaResponseDTO resena = servicio.obtener(5);

        assertEquals(5, resena.getIdResena());
        assertEquals("Luis Pérez Soto", resena.getNombreHuesped());
    }

    @Test
    void unaResenaOcultaOQueNoExisteResponde404() {
        resenaGuardada(false);

        assertFalla(HttpStatus.NOT_FOUND, () -> servicio.obtener(5));
        assertFalla(HttpStatus.NOT_FOUND, () -> servicio.obtener(99));
    }

    @Test
    void listaLasResenasVisiblesDeUnaPublicacion() {
        Resena resena = resenaGuardada(true);
        when(resenaRepository.findByReserva_Publicacion_IdPublicacionAndVisibleTrueOrderByFechaDescIdResenaDesc(15))
                .thenReturn(List.of(resena));

        List<ResenaResponseDTO> resenas = servicio.listarPorPublicacion(15);

        assertEquals(1, resenas.size());
        assertEquals(5, resenas.get(0).getIdResena());
    }

    @Test
    void listaLasResenasDelHuespedIncluidasLasOcultas() {
        Resena resena = resenaGuardada(false);
        when(resenaRepository.findByReserva_Huesped_IdUsuarioOrderByFechaDescIdResenaDesc(20)).thenReturn(List.of(resena));

        List<ResenaResponseDTO> resenas = servicio.listarPorHuesped(20);

        assertEquals(1, resenas.size());
        assertFalse(resenas.get(0).getVisible());
    }

    @Test
    void unHuespedInactivoNoPuedeListarSusResenas() {
        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.listarPorHuesped(99));
    }

    // ---------- actualizar ----------

    @Test
    void elAutorEditaLaCalificacionYElComentario() {
        resenaGuardada(true);

        ResenaResponseDTO editada = servicio.actualizar(5, cambio(20));

        assertEquals(5, editada.getCalificacion());
        assertEquals("Excelente", editada.getComentario());
        verify(resenaRepository).save(any(Resena.class));
    }

    @Test
    void otroHuespedNoPuedeEditarLaResena() {
        resenaGuardada(true);
        when(huespedRepository.findById(21)).thenReturn(Optional.of(huesped(21)));

        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.actualizar(5, cambio(21)));
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void unaResenaOcultaNoSePuedeEditar() {
        resenaGuardada(false);

        assertFalla(HttpStatus.CONFLICT, () -> servicio.actualizar(5, cambio(20)));
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void editarUnaResenaQueNoExisteResponde404() {
        assertFalla(HttpStatus.NOT_FOUND, () -> servicio.actualizar(99, cambio(20)));
    }

    // ---------- eliminar ----------

    @Test
    void elAutorEliminaSuResena() {
        Resena resena = resenaGuardada(true);

        MensajeResponseDTO respuesta = servicio.eliminar(5, 20);

        verify(resenaRepository).delete(resena);
        assertEquals("La reseña fue eliminada", respuesta.getMensaje());
    }

    @Test
    void otroHuespedNoPuedeEliminarLaResena() {
        resenaGuardada(true);
        when(huespedRepository.findById(21)).thenReturn(Optional.of(huesped(21)));

        assertFalla(HttpStatus.FORBIDDEN, () -> servicio.eliminar(5, 21));
        verify(resenaRepository, never()).delete(any());
    }

    @Test
    void unaResenaOcultaNoSePuedeEliminar() {
        resenaGuardada(false);

        assertFalla(HttpStatus.CONFLICT, () -> servicio.eliminar(5, 20));
        verify(resenaRepository, never()).delete(any());
    }
}
