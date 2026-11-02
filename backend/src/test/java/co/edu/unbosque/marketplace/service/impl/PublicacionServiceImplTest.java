package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.TipoPoliticaCancelacion;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoCuenta;
import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.PublicacionRepository;
import co.edu.unbosque.marketplace.repository.TipoPoliticaCancelacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

import static co.edu.unbosque.marketplace.DatosDePrueba.publicacion;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas del servicio de publicaciones. Los repositorios son simulados (mock), no se usa la BD; el mapper es el real.
 */
class PublicacionServiceImplTest {

    private PublicacionRepository publicacionRepository;
    private AnfitrionRepository anfitrionRepository;
    private TipoPoliticaCancelacionRepository politicaRepository;
    private PublicacionServiceImpl servicio;

    @BeforeEach
    void setUp() {
        publicacionRepository = mock(PublicacionRepository.class);
        anfitrionRepository = mock(AnfitrionRepository.class);
        politicaRepository = mock(TipoPoliticaCancelacionRepository.class);
        servicio = new PublicacionServiceImpl(publicacionRepository, anfitrionRepository, politicaRepository);

        // La política FLEXIBLE existe en la tabla
        when(politicaRepository.existsById("FLEXIBLE")).thenReturn(true);

        // save() devuelve el mismo objeto que recibe, con el id que le pondría la BD
        when(publicacionRepository.save(any(Publicacion.class))).thenAnswer(inv -> {
            Publicacion p = inv.getArgument(0);
            p.setIdPublicacion(15);
            return p;
        });
    }

    /** Crea un anfitrión de ejemplo con id 7, cuenta activa y sin restricciones. */
    private Anfitrion crearAnfitrion() {
        Usuario usuario = new Usuario("1012345678", "Ana", "María", "Gómez", "Rojas", "ana@correo.com", "hash");
        usuario.setIdUsuario(7);
        Anfitrion anfitrion = new Anfitrion(usuario);
        anfitrion.setIdUsuario(7);
        return anfitrion;
    }

    private void anfitrionExiste(Anfitrion anfitrion) {
        when(anfitrionRepository.findById(7)).thenReturn(Optional.of(anfitrion));
    }

    private void assertEstado(HttpStatus esperado, PublicacionRequestDTO dto) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.crear(dto));
        assertEquals(esperado, ex.getStatusCode());
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void crearGuardaLaPublicacionPendienteDeRevision() {
        anfitrionExiste(crearAnfitrion());

        PublicacionResponseDTO respuesta = servicio.crear(publicacion(7));

        ArgumentCaptor<Publicacion> guardada = ArgumentCaptor.forClass(Publicacion.class);
        verify(publicacionRepository).save(guardada.capture());
        assertEquals(EstadoPublicacion.PENDIENTE_REVISION, guardada.getValue().getEstado());
        assertEquals(7, guardada.getValue().getAnfitrion().getIdUsuario());
        assertInstanceOf(PoliticaFlexible.class, guardada.getValue().getPoliticaCancelacion());

        assertEquals(15, respuesta.getIdPublicacion());
        assertEquals(7, respuesta.getAnfitrion().getIdUsuario());
        assertEquals("Ana María Gómez Rojas", respuesta.getAnfitrion().getNombreCompleto());
        assertEquals("Apartamento con vista al mar", respuesta.getTitulo());
        assertEquals("FLEXIBLE", respuesta.getPoliticaCancelacion());
        assertEquals(EstadoPublicacion.PENDIENTE_REVISION, respuesta.getEstado());
    }

    @Test
    void copiaLosCamposQueSeLlamanIgualAlaPublicacionGuardada() {
        anfitrionExiste(crearAnfitrion());

        servicio.crear(publicacion(7));

        ArgumentCaptor<Publicacion> guardada = ArgumentCaptor.forClass(Publicacion.class);
        verify(publicacionRepository).save(guardada.capture());
        Publicacion p = guardada.getValue();
        assertEquals("Apartamento con vista al mar", p.getTitulo());
        assertEquals("Apartamento cómodo cerca de la playa", p.getDescripcion());
        assertEquals("Calle 10 # 5-20, El Rodadero", p.getUbicacion());
        assertEquals("Santa Marta", p.getCiudad());
        assertEquals("Apartamento", p.getTipoAlojamiento());
        assertEquals(4, p.getCapacidad());
        assertEquals(180000.0, p.getPrecioPorNoche());
        assertEquals(ModoAceptacion.INMEDIATA, p.getModoAceptacion());
    }

    @Test
    void sinTarifaDeLimpiezaNiReglasQuedaCeroYNulo() {
        anfitrionExiste(crearAnfitrion());

        PublicacionResponseDTO respuesta = servicio.crear(publicacion(7));

        assertEquals(0.0, respuesta.getTarifaLimpieza());
        assertNull(respuesta.getReglas());
    }

    @Test
    void conTarifaDeLimpiezaYReglasLasGuarda() {
        anfitrionExiste(crearAnfitrion());
        PublicacionRequestDTO dto = publicacion(7);
        dto.setTarifaLimpieza(25000.0);
        dto.setReglas("No fumar");

        PublicacionResponseDTO respuesta = servicio.crear(dto);

        assertEquals(25000.0, respuesta.getTarifaLimpieza());
        assertEquals("No fumar", respuesta.getReglas());
    }

    @Test
    void quienNoEsAnfitrionRecibe403() {
        when(anfitrionRepository.findById(99)).thenReturn(Optional.empty());

        assertEstado(HttpStatus.FORBIDDEN, publicacion(99));
    }

    @Test
    void anfitrionConCuentaDesactivadaRecibe403() {
        Anfitrion anfitrion = crearAnfitrion();
        anfitrion.getUsuario().setEstadoCuenta(EstadoCuenta.DESACTIVADA);
        anfitrionExiste(anfitrion);

        assertEstado(HttpStatus.FORBIDDEN, publicacion(7));
    }

    @Test
    void anfitrionRestringidoRecibe403() {
        Anfitrion anfitrion = crearAnfitrion();
        anfitrion.getUsuario().setRestringido(true);
        anfitrionExiste(anfitrion);

        assertEstado(HttpStatus.FORBIDDEN, publicacion(7));
    }

    @Test
    void unaPoliticaConClasePeroSinFilaEnLaTablaRecibe400() {
        anfitrionExiste(crearAnfitrion());
        when(politicaRepository.existsById("FLEXIBLE")).thenReturn(false);

        assertEstado(HttpStatus.BAD_REQUEST, publicacion(7));
    }

    @Test
    void unaPoliticaInexistenteRecibe400() {
        anfitrionExiste(crearAnfitrion());
        PublicacionRequestDTO dto = publicacion(7);
        dto.setPoliticaCancelacion("INEXISTENTE");

        assertEstado(HttpStatus.BAD_REQUEST, dto);
    }

    private TipoPoliticaCancelacion crearTipoPolitica(String codigo, String nombre, String descripcion) {
        TipoPoliticaCancelacion tipo = new TipoPoliticaCancelacion();
        tipo.setCodigo(codigo);
        tipo.setNombre(nombre);
        tipo.setDescripcion(descripcion);
        return tipo;
    }

    @Test
    void listarPoliticasDevuelveCodigoNombreYDescripcion() {
        when(politicaRepository.findAll()).thenReturn(List.of(crearTipoPolitica("FLEXIBLE", "Flexible", "Devolución flexible")));

        List<PoliticaCancelacionDTO> politicas = servicio.listarPoliticas();

        assertEquals(1, politicas.size());
        assertEquals("FLEXIBLE", politicas.get(0).getCodigo());
        assertEquals("Flexible", politicas.get(0).getNombre());
        assertEquals("Devolución flexible", politicas.get(0).getDescripcion());
    }

    @Test
    void listarPoliticasOmiteLasQueNoTienenClaseImplementada() {
        when(politicaRepository.findAll()).thenReturn(List.of(
                crearTipoPolitica("FLEXIBLE", "Flexible", "Devolución flexible"),
                crearTipoPolitica("ESTRICTA", "Estricta", "Sin clase todavía")));

        List<PoliticaCancelacionDTO> politicas = servicio.listarPoliticas();

        assertEquals(1, politicas.size());
        assertEquals("FLEXIBLE", politicas.get(0).getCodigo());
    }
}
