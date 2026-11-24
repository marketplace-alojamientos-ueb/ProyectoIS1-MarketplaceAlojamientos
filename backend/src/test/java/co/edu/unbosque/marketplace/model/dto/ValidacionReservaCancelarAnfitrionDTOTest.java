package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionReservaCancelarAnfitrionDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(ReservaCancelarAnfitrionRequestDTO dto) {
        Set<ConstraintViolation<ReservaCancelarAnfitrionRequestDTO>> errores = validator.validate(dto);
        return errores.stream().map(e -> e.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private ReservaCancelarAnfitrionRequestDTO solicitud(Integer idAnfitrion, String motivo) {
        ReservaCancelarAnfitrionRequestDTO dto = new ReservaCancelarAnfitrionRequestDTO();
        dto.setIdAnfitrion(idAnfitrion);
        dto.setMotivo(motivo);
        return dto;
    }

    @Test
    void solicitudValidaNoTieneErrores() {
        assertTrue(camposInvalidos(solicitud(7, "Imprevisto en la propiedad")).isEmpty());
    }

    @Test
    void sinDatosExigeElAnfitrionYElMotivo() {
        assertEquals(Set.of("idAnfitrion", "motivo"), camposInvalidos(new ReservaCancelarAnfitrionRequestDTO()));
    }

    @Test
    void unMotivoEnBlancoNoEsValido() {
        assertEquals(Set.of("motivo"), camposInvalidos(solicitud(7, "   ")));
    }

    @Test
    void unMotivoDeMasDe500CaracteresNoEsValido() {
        assertEquals(Set.of("motivo"), camposInvalidos(solicitud(7, "a".repeat(501))));
        assertTrue(camposInvalidos(solicitud(7, "a".repeat(500))).isEmpty());
    }
}
