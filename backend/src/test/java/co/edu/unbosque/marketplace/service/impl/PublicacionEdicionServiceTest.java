package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.PublicacionEditarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import co.edu.unbosque.marketplace.model.enums.TipoPeriodo;
import co.edu.unbosque.marketplace.repository.AdminRepository;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.PublicacionRepository;
import co.edu.unbosque.marketplace.repository.TipoPoliticaCancelacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PublicacionEdicionServiceTest {

    private PublicacionRepository publicacionRepository;
    private AdminRepository adminRepository;
    private TipoPoliticaCancelacionRepository politicaRepository;
    private PublicacionServiceImpl servicio;

    @BeforeEach
    void setUp() {
        publicacionRepository = mock(PublicacionRepository.class);
        adminRepository = mock(AdminRepository.class);
        politicaRepository = mock(TipoPoliticaCancelacionRepository.class);
        servicio = new PublicacionServiceImpl(publicacionRepository, mock(AnfitrionRepository.class),
                politicaRepository, adminRepository);

        when(politicaRepository.existsById("FLEXIBLE")).thenReturn(true);
        when(adminRepository.existsById(99)).thenReturn(true);
        when(publicacionRepository.findById(15)).thenReturn(Optional.of(publicacionExistente()));
        when(publicacionRepository.save(any(Publicacion.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Publicacion publicacionExistente() {
        Usuario usuarioPropietario = new Usuario("1012345678", "Ana", "María", "Gómez", "Rojas", "ana@correo.com", "hash");
        usuarioPropietario.setIdUsuario(7);
        Anfitrion anfitrion = new Anfitrion(usuarioPropietario);
        anfitrion.setIdUsuario(7);

        Publicacion p = new Publicacion();
        p.setIdPublicacion(15);
        p.setAnfitrion(anfitrion);
        p.setTitulo("Titulo viejo");
        p.setDescripcion("Descripcion vieja");
        p.setUbicacion("Ubicacion vieja");
        p.setCiudad("Bogota");
        p.setTipoAlojamiento("Casa");
        p.setCapacidad(2);
        p.setPrecioPorNoche(100000.0);
        p.setTarifaLimpieza(5000.0);
        p.setReglas("Reglas viejas");
        p.setModoAceptacion(ModoAceptacion.CON_APROBACION);
        p.setPoliticaCancelacion(new PoliticaFlexible());
        p.setEstado(EstadoPublicacion.ACTIVA);
        return p;
    }

    private PublicacionEditarRequestDTO cambios(Integer idUsuario) {
        PublicacionEditarRequestDTO dto = new PublicacionEditarRequestDTO();
        dto.setIdUsuario(idUsuario);
        dto.setTitulo("Titulo nuevo");
        dto.setDescripcion("Descripcion nueva");
        dto.setUbicacion("Ubicacion nueva");
        dto.setCiudad("Medellin");
        dto.setTipoAlojamiento("Apartamento");
        dto.setCapacidad(6);
        dto.setPrecioPorNoche(200000.0);
        dto.setTarifaLimpieza(30000.0);
        dto.setReglas("Reglas nuevas");
        dto.setModoAceptacion(ModoAceptacion.INMEDIATA);
        dto.setPoliticaCancelacion("FLEXIBLE");
        return dto;
    }

    @Test
    void elPropietarioEditaYSeActualizanLosDatos() {
        PublicacionResponseDTO respuesta = servicio.editar(15, cambios(7));

        ArgumentCaptor<Publicacion> guardada = ArgumentCaptor.forClass(Publicacion.class);
        verify(publicacionRepository).save(guardada.capture());
        Publicacion p = guardada.getValue();
        assertEquals("Titulo nuevo", p.getTitulo());
        assertEquals("Descripcion nueva", p.getDescripcion());
        assertEquals("Ubicacion nueva", p.getUbicacion());
        assertEquals("Medellin", p.getCiudad());
        assertEquals("Apartamento", p.getTipoAlojamiento());
        assertEquals(6, p.getCapacidad());
        assertEquals(200000.0, p.getPrecioPorNoche());
        assertEquals(30000.0, p.getTarifaLimpieza());
        assertEquals("Reglas nuevas", p.getReglas());
        assertEquals(ModoAceptacion.INMEDIATA, p.getModoAceptacion());

        assertEquals(15, respuesta.getIdPublicacion());
        assertEquals("Titulo nuevo", respuesta.getTitulo());
        assertEquals("FLEXIBLE", respuesta.getPoliticaCancelacion());
        assertEquals(7, respuesta.getAnfitrion().getIdUsuario());
    }

    @Test
    void editarNoCambiaLosPeriodosYLosDevuelve() {
        PeriodoDisponibilidad periodo = new PeriodoDisponibilidad();
        periodo.setFechaInicio(LocalDate.of(2030, 1, 1));
        periodo.setFechaFin(LocalDate.of(2030, 1, 10));
        periodo.setTipo(TipoPeriodo.DISPONIBLE);
        Publicacion existente = publicacionExistente();
        existente.getPeriodos().add(periodo);
        when(publicacionRepository.findById(15)).thenReturn(Optional.of(existente));

        PublicacionResponseDTO respuesta = servicio.editar(15, cambios(7));

        assertEquals(1, existente.getPeriodos().size());
        assertEquals(1, respuesta.getPeriodos().size());
        assertEquals(LocalDate.of(2030, 1, 1), respuesta.getPeriodos().get(0).getFechaInicio());
        assertEquals(TipoPeriodo.DISPONIBLE, respuesta.getPeriodos().get(0).getTipo());
    }

    @Test
    void unAdministradorPuedeEditarUnaPublicacionAjena() {
        PublicacionResponseDTO respuesta = servicio.editar(15, cambios(99));

        assertEquals("Titulo nuevo", respuesta.getTitulo());
        verify(publicacionRepository).save(any(Publicacion.class));
        assertEquals(7, respuesta.getAnfitrion().getIdUsuario());
    }

    @Test
    void otroUsuarioNoPuedeEditarYNoSeGuardaNada() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicio.editar(15, cambios(50)));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Edición no autorizada", ex.getReason());
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void obtenerParaEditarDevuelveLaPublicacionAlPropietario() {
        PublicacionResponseDTO respuesta = servicio.obtenerParaEditar(15, 7);

        assertEquals(15, respuesta.getIdPublicacion());
        assertEquals("Titulo viejo", respuesta.getTitulo());
        assertEquals("FLEXIBLE", respuesta.getPoliticaCancelacion());
        assertEquals(7, respuesta.getAnfitrion().getIdUsuario());
    }

    @Test
    void obtenerParaEditarNiegaElAccesoAOtroUsuario() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicio.obtenerParaEditar(15, 50));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Edición no autorizada", ex.getReason());
    }
}
