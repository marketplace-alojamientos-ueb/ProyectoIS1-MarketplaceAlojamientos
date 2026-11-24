package co.edu.unbosque.marketplace.model.entity.politica;

import co.edu.unbosque.marketplace.model.entity.Reserva;
import lombok.Getter;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

@Getter
public class PoliticaFlexible implements PoliticaCancelacion {

    private final String nombre = "Flexible";
    private final int horasLimite = 24;
    private final double porcentajeAntes = 100.0;
    private final double porcentajeDespues = 0.0;
    private final Clock reloj;

    public PoliticaFlexible() {
        this(Clock.systemDefaultZone());
    }

    public PoliticaFlexible(Clock reloj) {
        this.reloj = reloj;
    }

    @Override
    public String codigo() {
        return "FLEXIBLE";
    }

    @Override
    public Double calcularDevolucion(Reserva reserva) {
        // ZonedDateTime (no LocalDateTime) para que la resta mida horas reales en la zona del reloj (pedido por Sonar)
        long horasHastaLaLlegada = Duration.between(ZonedDateTime.now(reloj),
                reserva.getFechaLlegada().atStartOfDay(reloj.getZone())).toHours();
        double porcentaje = horasHastaLaLlegada >= horasLimite ? porcentajeAntes : porcentajeDespues;
        return Math.round(reserva.getCotizacion().getTotal() * porcentaje) / 100.0;
    }
}
