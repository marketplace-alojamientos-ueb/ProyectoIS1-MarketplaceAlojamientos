package co.edu.unbosque.marketplace.model.entity.politica;

import co.edu.unbosque.marketplace.model.entity.Reserva;

/**
 * Contrato de una política de cancelación (patrón estrategia). Cada política decide cuánto se devuelve al
 * cancelar una reserva. Una publicación guarda solo el código de su política (ver PoliticasCancelacion). El nombre y
 * la descripción que ve el anfitrión al escogerla vienen de la tabla politica_cancelacion (TipoPoliticaCancelacion).
 */
public interface PoliticaCancelacion {

    /**
     * Código que se guarda en la publicación (ej. 'FLEXIBLE'). Debe ser único entre las políticas.
     */
    String codigo();

    /**
     * Valor de la devolución simulada que corresponde al cancelar la reserva.
     */
    Double calcularDevolucion(Reserva reserva);
}
