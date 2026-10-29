package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.stream.Collectors;

import static co.edu.unbosque.marketplace.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

/** Comprueba que los DTOs validan también los campos heredados de DatosPersonalesDTO. */
class ValidacionDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /** Devuelve los nombres de los campos que no pasaron la validación. */
    private Set<String> camposInvalidos(Object dto) {
        Set<ConstraintViolation<Object>> errores = validator.validate(dto);
        return errores.stream().map(e -> e.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void registroValidoNoTieneErrores() {
        AnfitrionRequestDTO dto = registro("1012345678", "Ana", null, "Gómez", "Rojas",
                "ana@correo.com", "Clave1234");

        assertTrue(camposInvalidos(dto).isEmpty());
    }

    @Test
    void registroValidaLosCamposPropiosYLosHeredados() {
        AnfitrionRequestDTO dto = registro("12", "", null, "Gómez", "Rojas",
                "correo-malo", "corta");

        assertEquals(Set.of("cedula", "primerNombre", "correo", "password"), camposInvalidos(dto));
    }

    @Test
    void actualizarValidaLosCamposHeredados() {
        AnfitrionActualizarDTO dto = actualizacion("Ana", null, "", "Rojas", "correo-malo");

        assertEquals(Set.of("primerApellido", "correo"), camposInvalidos(dto));
    }
}
