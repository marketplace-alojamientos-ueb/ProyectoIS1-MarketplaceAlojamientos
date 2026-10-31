package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba las validaciones del registro de huésped (campos propios y heredados). */
class ValidacionHuespedDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /** Devuelve los nombres de los campos que no pasaron la validación. */
    private Set<String> camposInvalidos(Object dto) {
        Set<ConstraintViolation<Object>> errores = validator.validate(dto);
        return errores.stream().map(e -> e.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private HuespedRequestDTO crearRegistro(String cedula, String primerNombre, String correo, String password) {
        HuespedRequestDTO dto = new HuespedRequestDTO();
        dto.setCedula(cedula);
        dto.setPrimerNombre(primerNombre);
        dto.setPrimerApellido("Gómez");
        dto.setSegundoApellido("Pérez");
        dto.setCorreo(correo);
        dto.setPassword(password);
        return dto;
    }

    @Test
    void registroValidoNoTieneErrores() {
        assertTrue(camposInvalidos(crearRegistro("1012345678", "Laura", "laura@correo.com", "Clave1234")).isEmpty());
    }

    @Test
    void registroValidaLosCamposPropiosYLosHeredados() {
        HuespedRequestDTO dto = crearRegistro("12", "", "correo-malo", "corta");

        assertEquals(Set.of("cedula", "primerNombre", "correo", "password"), camposInvalidos(dto));
    }

    @Test
    void registroSinDatosMarcaLosCamposObligatorios() {
        assertEquals(Set.of("cedula", "primerNombre", "primerApellido", "segundoApellido", "correo", "password"),
                camposInvalidos(new HuespedRequestDTO()));
    }

    @Test
    void passwordSinNumeroEsInvalida() {
        assertEquals(Set.of("password"),
                camposInvalidos(crearRegistro("1012345678", "Laura", "laura@correo.com", "SoloLetras")));
    }
}
