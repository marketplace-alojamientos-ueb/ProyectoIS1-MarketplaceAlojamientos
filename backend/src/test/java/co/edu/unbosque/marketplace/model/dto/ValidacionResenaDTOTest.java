package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionResenaDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private <T> Set<String> camposInvalidos(T dto) {
        Set<ConstraintViolation<T>> errores = validator.validate(dto);
        return errores.stream().map(e -> e.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private ResenaRequestDTO solicitud(Integer calificacion, String comentario) {
        ResenaRequestDTO dto = new ResenaRequestDTO();
        dto.setIdHuesped(20);
        dto.setIdReserva(31);
        dto.setCalificacion(calificacion);
        dto.setComentario(comentario);
        return dto;
    }

    private ResenaActualizarDTO cambio(Integer calificacion, String comentario) {
        ResenaActualizarDTO dto = new ResenaActualizarDTO();
        dto.setIdHuesped(20);
        dto.setCalificacion(calificacion);
        dto.setComentario(comentario);
        return dto;
    }

    @Test
    void unaSolicitudValidaNoTieneErrores() {
        assertTrue(camposInvalidos(solicitud(4, "Muy buena")).isEmpty());
        assertTrue(camposInvalidos(solicitud(1, null)).isEmpty());
        assertTrue(camposInvalidos(cambio(5, "Excelente")).isEmpty());
    }

    @Test
    void sinDatosExigeHuespedReservaYCalificacion() {
        assertEquals(Set.of("idHuesped", "idReserva", "calificacion"), camposInvalidos(new ResenaRequestDTO()));
        assertEquals(Set.of("idHuesped", "calificacion"), camposInvalidos(new ResenaActualizarDTO()));
    }

    @Test
    void laCalificacionDebeEstarEntre1Y5() {
        assertEquals(Set.of("calificacion"), camposInvalidos(solicitud(0, null)));
        assertEquals(Set.of("calificacion"), camposInvalidos(solicitud(6, null)));
        assertEquals(Set.of("calificacion"), camposInvalidos(cambio(0, null)));
        assertEquals(Set.of("calificacion"), camposInvalidos(cambio(6, null)));
    }

    @Test
    void elComentarioNoPuedePasarDe1000Caracteres() {
        assertTrue(camposInvalidos(solicitud(4, "a".repeat(1000))).isEmpty());
        assertEquals(Set.of("comentario"), camposInvalidos(solicitud(4, "a".repeat(1001))));
        assertEquals(Set.of("comentario"), camposInvalidos(cambio(4, "a".repeat(1001))));
    }
}
