package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionReservaCancelarDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(ReservaCancelarRequestDTO dto) {
        Set<ConstraintViolation<ReservaCancelarRequestDTO>> errores = validator.validate(dto);
        return errores.stream().map(e -> e.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private ReservaCancelarRequestDTO solicitud(Integer idHuesped, String motivo) {
        ReservaCancelarRequestDTO dto = new ReservaCancelarRequestDTO();
        dto.setIdHuesped(idHuesped);
        dto.setMotivo(motivo);
        return dto;
    }

    @Test
    void solicitudValidaNoTieneErrores() {
        assertTrue(camposInvalidos(solicitud(20, "Cambio de planes")).isEmpty());
    }

    @Test
    void sinDatosExigeElHuespedYElMotivo() {
        assertEquals(Set.of("idHuesped", "motivo"), camposInvalidos(new ReservaCancelarRequestDTO()));
    }

    @Test
    void unMotivoEnBlancoNoEsValido() {
        assertEquals(Set.of("motivo"), camposInvalidos(solicitud(20, "   ")));
    }

    @Test
    void unMotivoDeMasDe500CaracteresNoEsValido() {
        assertEquals(Set.of("motivo"), camposInvalidos(solicitud(20, "a".repeat(501))));
        assertTrue(camposInvalidos(solicitud(20, "a".repeat(500))).isEmpty());
    }
}
