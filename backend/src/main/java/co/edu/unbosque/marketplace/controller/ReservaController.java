package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmadaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    /**
     * <b>POST /api/reservas/confirmar</b> — El huésped confirma la reserva y paga (pago simulado, MA-102).
     *
     * <p>Pide en el cuerpo:</p>
     * <pre>
     * {
     *   "idHuesped": 20,
     *   "idPublicacion": 15,
     *   "fechaLlegada": "2026-12-20",
     *   "fechaSalida": "2026-12-23",
     *   "numHuespedes": 3,
     *   "claveIdempotencia": "9b1f0c1e-...",   // un UUID que el front genera una vez por intento (crypto.randomUUID())
     *   "numeroTarjeta": "4242424242424242"    // 16 dígitos; las que terminan en 0002 se rechazan
     * }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>201</b> con el ReservaConfirmadaDTO:
     *   <pre>
     * {
     *   "idReserva": 31, "estado": "CONFIRMADA", "fechaLlegada": "2026-12-20", "fechaSalida": "2026-12-23",
     *   "numHuespedes": 3, "idPublicacion": 15, "tituloPublicacion": "Cabaña con vista al lago",
     *   "noches": 3, "precioBase": 450000.0, "tarifaLimpieza": 50000.0, "total": 500000.0,
     *   "idTransaccion": "SIM-...", "mensaje": "El pago fue aprobado y la reserva quedó confirmada"
     * }
     *   </pre>
     *   Las fechas quedan RESERVADAS en la publicación, se guarda el pago y se notifica al huésped y al anfitrión. Si
     *   la publicación es CON_APROBACION, el estado es <code>PENDIENTE</code> y las fechas se reservan cuando el
     *   anfitrión acepta. Si se repite la misma <code>claveIdempotencia</code> devuelve la misma reserva sin cobrar
     *   otra vez.</li>
     *   <li><b>400</b> si falta un dato, la salida no es posterior a la llegada, la llegada ya pasó o hay más
     *       huéspedes que la capacidad.</li>
     *   <li><b>402</b> si el pago fue rechazado. No se crea la reserva ni se bloquean las fechas.</li>
     *   <li><b>403</b> si el id no es de un huésped con la cuenta activa y sin restricciones.</li>
     *   <li><b>404</b> si la publicación no existe.</li>
     *   <li><b>409</b> si se perdió la disponibilidad de las fechas o la publicación no está activa. No se procesa el
     *       pago.</li>
     * </ul>
     * <p>Uso en el front: botón "Confirmar y pagar". En 402 y 409 mostrar el <code>"message"</code> del error.</p>
     */
    @PostMapping("/confirmar")
    public ResponseEntity<ReservaConfirmadaDTO> confirmar(@Valid @RequestBody ReservaConfirmarRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.confirmar(dto));
    }
}
