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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas de pausar una publicación (MA-108). La publicación 15 es del anfitrión 7 y empieza ACTIVA. */
class PublicacionPausaServiceTest {

    private PublicacionRepository publicacionRepository;
    private PublicacionServiceImpl servicio;
    private Publicacion publicacion;

    @BeforeEach
    void setUp() {
        publicacionRepository = mock(PublicacionRepository.class);
        servicio = new PublicacionServiceImpl(publicacionRepository, mock(AnfitrionRepository.class),
                mock(TipoPoliticaCancelacionRepository.class), mock(AdminRepository.class));

        Usuario usuario = new Usuario("1012345678", "Ana", "María", "Gómez", "Rojas", "ana@correo.com", "hash");
        usuario.setIdUsuario(7);
        Anfitrion anfitrion = new Anfitrion(usuario);
        anfitrion.setIdUsuario(7);

        publicacion = new Publicacion();
        publicacion.setIdPublicacion(15);
        publicacion.setAnfitrion(anfitrion);
        publicacion.setTitulo("Cabaña");
        publicacion.setDescripcion("Cabaña con vista");
        publicacion.setUbicacion("Vereda El Lago");
        publicacion.setCiudad("Guatavita");
        publicacion.setTipoAlojamiento("Cabana");
        publicacion.setCapacidad(4);
        publicacion.setPrecioPorNoche(250000.0);
        publicacion.setModoAceptacion(ModoAceptacion.INMEDIATA);
        publicacion.setPoliticaCancelacion(new PoliticaFlexible());
        publicacion.setEstado(EstadoPublicacion.ACTIVA);

        when(publicacionRepository.findById(15)).thenReturn(Optional.of(publicacion));
        when(publicacionRepository.save(any(Publicacion.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void elAnfitrionPausaUnaPublicacionActivaYPasaAPausada() {
        PublicacionResponseDTO respuesta = servicio.pausar(15, 7);

        assertEquals(EstadoPublicacion.PAUSADA, respuesta.getEstado());
        assertEquals(EstadoPublicacion.PAUSADA, publicacion.getEstado());
        verify(publicacionRepository).save(publicacion);
    }

    @Test
    void siLaPublicacionNoEstaActivaResponde409YNoLaCambia() {
        publicacion.setEstado(EstadoPublicacion.BORRADOR);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.pausar(15, 7));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals(EstadoPublicacion.BORRADOR, publicacion.getEstado());
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void otroUsuarioNoPuedePausarLaPublicacion() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.pausar(15, 50));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals(EstadoPublicacion.ACTIVA, publicacion.getEstado());
        verify(publicacionRepository, never()).save(any());
    }
}
