package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
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

    /**
     * <b>POST /api/reservas/{id}/aceptar?idAnfitrion=7</b> — Acepta una reserva en espera (botón "Aceptar").
     *
     * <p>Pide: el id de la reserva en la ruta y, como parámetro <code>idAnfitrion</code>, el id del anfitrión dueño
     * del alojamiento que decide.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con <code>{ "mensaje": "..." }</code>. La reserva pasa a <code>CONFIRMADA</code>, las fechas
     *       del alojamiento quedan reservadas y se guarda una notificación para el huésped.</li>
     *   <li><b>403</b> si el id no es de un anfitrión activo y sin restricciones, o si la reserva no es de un
     *       alojamiento suyo.</li>
     *   <li><b>404</b> si la reserva no existe.</li>
     *   <li><b>409</b> si la reserva ya no está PENDIENTE, o si las fechas ya no están disponibles en el
     *       alojamiento. En ambos casos no cambia nada.</li>
     * </ul>
     * <p>Uso en el front: botón "Aceptar" de cada reserva de la lista de pendientes. Al responder 200, quitarla de la
     * lista y mostrar el mensaje.</p>
     */
    @PostMapping("/{id}/aceptar")
    public ResponseEntity<MensajeResponseDTO> aceptar(@PathVariable Integer id, @RequestParam Integer idAnfitrion) {
        return ResponseEntity.ok(reservaService.aceptar(id, idAnfitrion));
    }

    /**
     * <b>POST /api/reservas/{id}/rechazar?idAnfitrion=7</b> — Rechaza una reserva en espera (botón "Rechazar").
     *
     * <p>Pide: el id de la reserva en la ruta y, como parámetro <code>idAnfitrion</code>, el id del anfitrión dueño
     * del alojamiento que decide.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con <code>{ "mensaje": "..." }</code>. La reserva pasa a <code>RECHAZADA</code>, las fechas
     *       del alojamiento no se tocan y se guarda una notificación para el huésped.</li>
     *   <li><b>403</b> si el id no es de un anfitrión activo y sin restricciones, o si la reserva no es de un
     *       alojamiento suyo.</li>
     *   <li><b>404</b> si la reserva no existe.</li>
     *   <li><b>409</b> si la reserva ya no está PENDIENTE. No cambia nada.</li>
     * </ul>
     * <p>Uso en el front: botón "Rechazar" de cada reserva de la lista de pendientes. Al responder 200, quitarla de
     * la lista y mostrar el mensaje.</p>
     */
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<MensajeResponseDTO> rechazar(@PathVariable Integer id, @RequestParam Integer idAnfitrion) {
        return ResponseEntity.ok(reservaService.rechazar(id, idAnfitrion));
    }
}
