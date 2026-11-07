package co.edu.unbosque.marketplace.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static co.edu.unbosque.marketplace.DatosDePrueba.publicacion;
import static org.junit.jupiter.api.Assertions.*;

/** Comprueba las validaciones de la solicitud para crear una publicación. */
class ValidacionPublicacionDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /** Devuelve los nombres de los campos que no pasaron la validación. */
    private Set<String> camposInvalidos(Object dto) {
        Set<ConstraintViolation<Object>> errores = validator.validate(dto);
        return errores.stream().map(e -> e.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void publicacionValidaNoTieneErrores() {
        assertTrue(camposInvalidos(publicacion(7)).isEmpty());
    }

    @Test
    void sinDatosSoloExigeElAnfitrion() {
        // Los campos mínimos que falten no son error de validación: la publicación queda en BORRADOR
        assertEquals(Set.of("idAnfitrion"), camposInvalidos(new PublicacionRequestDTO()));
    }

    @Test
    void sinPeriodosNoEsErrorDeValidacion() {
        PublicacionRequestDTO dto = publicacion(7);
        dto.setPeriodos(List.of());

        assertTrue(camposInvalidos(dto).isEmpty());
    }

    @Test
    void unPeriodoSinFechasEsInvalido() {
        PublicacionRequestDTO dto = publicacion(7);
        dto.setPeriodos(List.of(new PeriodoDisponibilidadRequestDTO()));

        assertEquals(Set.of("periodos[0].fechaInicio", "periodos[0].fechaFin"), camposInvalidos(dto));
    }

    @Test
    void unPeriodoNuloEsInvalido() {
        PublicacionRequestDTO dto = publicacion(7);
        dto.setPeriodos(Collections.singletonList(null));

        assertFalse(camposInvalidos(dto).isEmpty());
    }

    @Test
    void textosEnBlancoNoSonErrorDeValidacion() {
        PublicacionRequestDTO dto = publicacion(7);
        dto.setTitulo("   ");
        dto.setPoliticaCancelacion(" ");

        assertTrue(camposInvalidos(dto).isEmpty());
    }

    @Test
    void textosMasLargosQueLaColumnaSonInvalidos() {
        PublicacionRequestDTO dto = publicacion(7);
        dto.setTitulo("a".repeat(53));
        dto.setDescripcion("a".repeat(501));
        dto.setUbicacion("a".repeat(101));
        dto.setCiudad("a".repeat(51));
        dto.setTipoAlojamiento("a".repeat(21));
        dto.setReglas("a".repeat(1001));

        assertEquals(Set.of("titulo", "descripcion", "ubicacion", "ciudad", "tipoAlojamiento", "reglas"),
                camposInvalidos(dto));
    }

    @Test
    void textosEnElLimiteSonValidos() {
        PublicacionRequestDTO dto = publicacion(7);
        dto.setTitulo("a".repeat(52));
        dto.setReglas("a".repeat(1000));

        assertTrue(camposInvalidos(dto).isEmpty());
    }

    @Test
    void capacidadYPrecioDebenSerMayoresQueCero() {
        PublicacionRequestDTO dto = publicacion(7);
        dto.setCapacidad(0);
        dto.setPrecioPorNoche(-5.0);

        assertEquals(Set.of("capacidad", "precioPorNoche"), camposInvalidos(dto));
    }

    @Test
    void laTarifaDeLimpiezaEsOpcionalPeroNoPuedeSerNegativa() {
        PublicacionRequestDTO dto = publicacion(7);
        dto.setTarifaLimpieza(0.0);
        assertTrue(camposInvalidos(dto).isEmpty());

        dto.setTarifaLimpieza(-1.0);
        assertEquals(Set.of("tarifaLimpieza"), camposInvalidos(dto));
    }
}
