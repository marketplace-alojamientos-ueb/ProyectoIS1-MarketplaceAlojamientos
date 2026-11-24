package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionResenaModeracionDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private <T> Set<String> camposInvalidos(T dto) {
        Set<ConstraintViolation<T>> errores = validator.validate(dto);
        return errores.stream().map(e -> e.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private ResenaReportarDTO reporte(Integer idUsuario, String motivo) {
        ResenaReportarDTO dto = new ResenaReportarDTO();
        dto.setIdUsuario(idUsuario);
        dto.setMotivo(motivo);
        return dto;
    }

    private ResenaModerarDTO decision(Integer idAdmin, String motivo) {
        ResenaModerarDTO dto = new ResenaModerarDTO();
        dto.setIdAdmin(idAdmin);
        dto.setMotivo(motivo);
        return dto;
    }

    @Test
    void datosValidosNoTienenErrores() {
        assertTrue(camposInvalidos(reporte(7, "Ofensiva")).isEmpty());
        assertTrue(camposInvalidos(decision(4, "Contenido ofensivo")).isEmpty());
    }

    @Test
    void sinDatosExigeQuienYElMotivo() {
        assertEquals(Set.of("idUsuario", "motivo"), camposInvalidos(new ResenaReportarDTO()));
        assertEquals(Set.of("idAdmin", "motivo"), camposInvalidos(new ResenaModerarDTO()));
    }

    @Test
    void unMotivoEnBlancoNoEsValido() {
        assertEquals(Set.of("motivo"), camposInvalidos(reporte(7, "   ")));
        assertEquals(Set.of("motivo"), camposInvalidos(decision(4, "   ")));
    }

    @Test
    void elMotivoNoPuedePasarDe500Caracteres() {
        assertTrue(camposInvalidos(reporte(7, "a".repeat(500))).isEmpty());
        assertEquals(Set.of("motivo"), camposInvalidos(reporte(7, "a".repeat(501))));
        assertEquals(Set.of("motivo"), camposInvalidos(decision(4, "a".repeat(501))));
    }
}
