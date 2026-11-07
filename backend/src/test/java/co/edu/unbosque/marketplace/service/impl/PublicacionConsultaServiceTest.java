package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.PublicacionResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import co.edu.unbosque.marketplace.repository.AdminRepository;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.PublicacionRepository;
import co.edu.unbosque.marketplace.repository.TipoPoliticaCancelacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas de "Mis publicaciones" (MA-107). El anfitrión 7 es quien consulta. */
class PublicacionConsultaServiceTest {

    private PublicacionRepository publicacionRepository;
    private PublicacionServiceImpl servicio;
    private Anfitrion anfitrion;

    @BeforeEach
    void setUp() {
        publicacionRepository = mock(PublicacionRepository.class);
        servicio = new PublicacionServiceImpl(publicacionRepository, mock(AnfitrionRepository.class),
                mock(TipoPoliticaCancelacionRepository.class), mock(AdminRepository.class));

        Usuario usuario = new Usuario("1012345678", "Ana", "María", "Gómez", "Rojas", "ana@correo.com", "hash");
        usuario.setIdUsuario(7);
        anfitrion = new Anfitrion(usuario);
        anfitrion.setIdUsuario(7);
    }

    private Publicacion publicacion(Integer id, EstadoPublicacion estado) {
        Publicacion p = new Publicacion();
        p.setIdPublicacion(id);
        p.setAnfitrion(anfitrion);
        p.setTitulo("Publicación " + id);
        p.setDescripcion("Descripción");
        p.setUbicacion("Ubicación");
        p.setCiudad("Bogotá");
        p.setTipoAlojamiento("Casa");
        p.setCapacidad(4);
        p.setPrecioPorNoche(200000.0);
        p.setModoAceptacion(ModoAceptacion.INMEDIATA);
        p.setPoliticaCancelacion(new PoliticaFlexible());
        p.setEstado(estado);
        return p;
    }

    @Test
    void devuelveSoloLasPublicacionesDelAnfitrionConSuEstadoActual() {
        when(publicacionRepository.findByAnfitrion_IdUsuario(7)).thenReturn(List.of(
                publicacion(1, EstadoPublicacion.BORRADOR),
                publicacion(2, EstadoPublicacion.ACTIVA),
                publicacion(3, EstadoPublicacion.PAUSADA)));

        List<PublicacionResponseDTO> lista = servicio.listar(7);

        assertEquals(3, lista.size());
        assertEquals(EstadoPublicacion.BORRADOR, lista.get(0).getEstado());
        assertEquals(EstadoPublicacion.ACTIVA, lista.get(1).getEstado());
        assertEquals(EstadoPublicacion.PAUSADA, lista.get(2).getEstado());
        assertEquals(7, lista.get(0).getAnfitrion().getIdUsuario());
        verify(publicacionRepository, never()).findAll();
    }

    @Test
    void sinPublicacionesDevuelveUnaListaVacia() {
        when(publicacionRepository.findByAnfitrion_IdUsuario(7)).thenReturn(List.of());

        assertTrue(servicio.listar(7).isEmpty());
    }

    @Test
    void alVolverAConsultarVeElEstadoActualizado() {
        Publicacion p = publicacion(2, EstadoPublicacion.ACTIVA);
        when(publicacionRepository.findByAnfitrion_IdUsuario(7)).thenReturn(List.of(p));
        assertEquals(EstadoPublicacion.ACTIVA, servicio.listar(7).get(0).getEstado());

        p.setEstado(EstadoPublicacion.PAUSADA);

        assertEquals(EstadoPublicacion.PAUSADA, servicio.listar(7).get(0).getEstado());
    }
}
