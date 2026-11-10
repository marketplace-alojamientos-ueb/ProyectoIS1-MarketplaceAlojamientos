package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.service.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reservas. Base: <b>/api/reservas</b>
 *
 * <p>El objeto <b>ReservaPendienteDTO</b> es:</p>
 * <pre>
 * {
 *   "idReserva": 31,
 *   "estado": "PENDIENTE",             // PENDIENTE | CONFIRMADA | RECHAZADA | CANCELADA | COMPLETADA | EN_REVISION_MANUAL
 *   "fechaLlegada": "2026-12-20",
 *   "fechaSalida": "2026-12-23",
 *   "numHuespedes": 3,
 *   "idPublicacion": 15,
 *   "tituloPublicacion": "Cabaña con vista al lago",
 *   "idHuesped": 20,
 *   "nombreHuesped": "Luis Pérez Soto"
 * }
 * </pre>
 * <p>Los errores llegan con el formato por defecto de Spring; el texto para mostrar está en
 * <code>"message"</code>.</p>
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /**
     * <b>GET /api/reservas/pendientes?idAnfitrion=7</b> — Reservas en espera de aprobación de un anfitrión.
     *
     * <p>Pide: como parámetro <code>idAnfitrion</code>, el id del anfitrión que consulta. Es obligatorio, sin él
     * responde 400.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con un arreglo de ReservaPendienteDTO, solo las reservas PENDIENTES de los alojamientos de ese
     *       anfitrión, ordenadas por fecha de llegada. Si no tiene ninguna, el arreglo viene vacío
     *       <code>[]</code>.</li>
     *   <li><b>403</b> si el id no es de un anfitrión con la cuenta activa y sin restricciones.</li>
     * </ul>
     * <p>Uso en el front: lista de "Reservas pendientes". Si el arreglo viene vacío, mostrar el mensaje "No tienes
     * reservas pendientes".</p>
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<ReservaPendienteDTO>> listarPendientes(@RequestParam Integer idAnfitrion) {
        return ResponseEntity.ok(reservaService.listarPendientes(idAnfitrion));
    }
}
