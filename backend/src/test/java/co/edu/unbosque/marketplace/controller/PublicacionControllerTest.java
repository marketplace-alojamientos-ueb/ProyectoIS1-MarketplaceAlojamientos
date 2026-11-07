package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionEditarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionResponseDTO;
import co.edu.unbosque.marketplace.service.PublicacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas del controller: revisa que llame al servicio y responda con el código HTTP correcto. */
class PublicacionControllerTest {

    private PublicacionService publicacionService;
    private PublicacionController controller;

    @BeforeEach
    void setUp() {
        publicacionService = mock(PublicacionService.class);
        controller = new PublicacionController(publicacionService);
    }

    @Test
    void crearResponde201() {
        PublicacionRequestDTO dto = new PublicacionRequestDTO();
        PublicacionResponseDTO publicacion = new PublicacionResponseDTO();
        publicacion.setIdPublicacion(15);
        when(publicacionService.crear(dto)).thenReturn(publicacion);

        ResponseEntity<PublicacionResponseDTO> respuesta = controller.crear(dto);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(15, respuesta.getBody().getIdPublicacion());
        verify(publicacionService).crear(dto);
    }

    @Test
    void listarPoliticasResponde200() {
        PoliticaCancelacionDTO politica = new PoliticaCancelacionDTO();
        politica.setCodigo("FLEXIBLE");
        when(publicacionService.listarPoliticas()).thenReturn(List.of(politica));

        ResponseEntity<List<PoliticaCancelacionDTO>> respuesta = controller.listarPoliticas();

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("FLEXIBLE", respuesta.getBody().get(0).getCodigo());
    }

    @Test
    void listarResponde200ConLasPublicacionesDelAnfitrion() {
        PublicacionResponseDTO publicacion = new PublicacionResponseDTO();
        publicacion.setIdPublicacion(15);
        when(publicacionService.listar(7)).thenReturn(List.of(publicacion));

        ResponseEntity<List<PublicacionResponseDTO>> respuesta = controller.listar(7);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, respuesta.getBody().size());
        assertEquals(15, respuesta.getBody().get(0).getIdPublicacion());
    }

    @Test
    void obtenerParaEditarResponde200() {
        PublicacionResponseDTO publicacion = new PublicacionResponseDTO();
        publicacion.setIdPublicacion(15);
        when(publicacionService.obtenerParaEditar(15, 7)).thenReturn(publicacion);

        ResponseEntity<PublicacionResponseDTO> respuesta = controller.obtenerParaEditar(15, 7);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(15, respuesta.getBody().getIdPublicacion());
    }

    @Test
    void pausarResponde200() {
        PublicacionResponseDTO publicacion = new PublicacionResponseDTO();
        publicacion.setIdPublicacion(15);
        when(publicacionService.pausar(15, 7)).thenReturn(publicacion);

        ResponseEntity<PublicacionResponseDTO> respuesta = controller.pausar(15, 7);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(15, respuesta.getBody().getIdPublicacion());
    }

    @Test
    void editarResponde200() {
        PublicacionEditarRequestDTO dto = new PublicacionEditarRequestDTO();
        PublicacionResponseDTO publicacion = new PublicacionResponseDTO();
        publicacion.setIdPublicacion(15);
        when(publicacionService.editar(15, dto)).thenReturn(publicacion);

        ResponseEntity<PublicacionResponseDTO> respuesta = controller.editar(15, dto);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(15, respuesta.getBody().getIdPublicacion());
        verify(publicacionService).editar(15, dto);
    }

    @Test
    void buscarResponde200ConLosAlojamientosEncontrados() {
        PublicacionBusquedaRequestDTO filtros = new PublicacionBusquedaRequestDTO();
        PublicacionBusquedaDTO alojamiento = new PublicacionBusquedaDTO();
        alojamiento.setIdPublicacion(15);
        when(publicacionService.buscar(filtros)).thenReturn(List.of(alojamiento));

        ResponseEntity<List<PublicacionBusquedaDTO>> respuesta = controller.buscar(filtros);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(15, respuesta.getBody().get(0).getIdPublicacion());
    }
}
