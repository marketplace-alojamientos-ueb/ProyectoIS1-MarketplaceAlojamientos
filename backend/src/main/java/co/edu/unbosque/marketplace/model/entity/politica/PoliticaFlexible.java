package co.edu.unbosque.marketplace.model.entity.politica;

import co.edu.unbosque.marketplace.model.entity.Reserva;

/**
 * Política de cancelación flexible. Su código es FLEXIBLE.
 */
public class PoliticaFlexible implements PoliticaCancelacion {

    @Override
    public String codigo() {
        return "FLEXIBLE";
    }

    /**
     * Pendiente: la fórmula la define la historia de cancelación, cuando existan Reserva y Cotizacion.
     * Mientras tanto lanza una excepción, para que nadie obtenga una devolución de 0 sin darse cuenta.
     */
    @Override
    public Double calcularDevolucion(Reserva reserva) {
        throw new UnsupportedOperationException("La devolución de la política flexible aún no está implementada");
    }
}
