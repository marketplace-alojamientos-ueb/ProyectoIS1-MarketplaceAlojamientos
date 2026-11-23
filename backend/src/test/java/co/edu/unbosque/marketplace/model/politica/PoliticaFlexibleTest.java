package co.edu.unbosque.marketplace.model.politica;

import co.edu.unbosque.marketplace.model.entity.Cotizacion;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaCancelacion;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaFlexible;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaFlexibleTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private Clock relojEn(String instante) {
        return Clock.fixed(Instant.parse(instante), ZONA);
    }

    private Reserva reservaQueLlegaElDia20ConTotal(double total) {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setTotal(total);
        Reserva reserva = new Reserva();
        reserva.setFechaLlegada(LocalDate.of(2030, 1, 20));
        reserva.setCotizacion(cotizacion);
        return reserva;
    }

    @Test
    void suCodigoEsFlexible() {
        assertEquals("FLEXIBLE", new PoliticaFlexible().codigo());
    }

    @Test
    void susParametrosSon24HorasCienPorCientoAntesYCeroDespues() {
        PoliticaFlexible politica = new PoliticaFlexible();

        assertEquals("Flexible", politica.getNombre());
        assertEquals(24, politica.getHorasLimite());
        assertEquals(100.0, politica.getPorcentajeAntes());
        assertEquals(0.0, politica.getPorcentajeDespues());
    }

    @Test
    void conMuchaAnticipacionDevuelveElTotal() {
        PoliticaCancelacion politica = new PoliticaFlexible(relojEn("2030-01-10T15:00:00Z"));

        assertEquals(500000.0, politica.calcularDevolucion(reservaQueLlegaElDia20ConTotal(500000.0)));
    }

    @Test
    void justoConLasHorasLimiteDevuelveElTotal() {
        PoliticaCancelacion politica = new PoliticaFlexible(
                Clock.fixed(LocalDate.of(2030, 1, 19).atStartOfDay(ZONA).toInstant(), ZONA));

        assertEquals(500000.0, politica.calcularDevolucion(reservaQueLlegaElDia20ConTotal(500000.0)));
    }

    @Test
    void conMenosDeLasHorasLimiteNoDevuelveNada() {
        PoliticaCancelacion politica = new PoliticaFlexible(
                Clock.fixed(LocalDate.of(2030, 1, 19).atStartOfDay(ZONA).plusMinutes(1).toInstant(), ZONA));

        assertEquals(0.0, politica.calcularDevolucion(reservaQueLlegaElDia20ConTotal(500000.0)));
    }

    @Test
    void siYaPasoLaLlegadaNoDevuelveNada() {
        PoliticaCancelacion politica = new PoliticaFlexible(relojEn("2030-01-25T15:00:00Z"));

        assertEquals(0.0, politica.calcularDevolucion(reservaQueLlegaElDia20ConTotal(500000.0)));
    }
}
