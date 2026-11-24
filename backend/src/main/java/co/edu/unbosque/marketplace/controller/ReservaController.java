package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCanceladaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarAnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCancelarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.SimulacionCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaCheckoutRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmadaDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaConfirmarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaListadoDTO;
import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
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
     * <b>GET /api/reservas/huesped?idHuesped=20&amp;estado=CONFIRMADA</b> — Reservas de un huésped, de la más
     * reciente a la más antigua.
     *
     * <p>Pide: como parámetro <code>idHuesped</code>, el id del huésped que consulta (obligatorio, sin él responde
     * 400). Opcional <code>estado</code> para filtrar: PENDIENTE, CONFIRMADA, RECHAZADA, CANCELADA, COMPLETADA o
     * EN_REVISION_MANUAL. Sin <code>estado</code> trae todas. Un estado que no existe responde 400.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con un arreglo de ReservaListadoDTO, vacío <code>[]</code> si no hay ninguna:
     *   <pre>
     * {
     *   "idReserva": 31, "estado": "CONFIRMADA", "fechaLlegada": "2026-12-20", "fechaSalida": "2026-12-23",
     *   "numHuespedes": 3, "total": 500000.0,
     *   "idPublicacion": 15, "tituloPublicacion": "Cabaña con vista al lago",
     *   "idHuesped": 20, "nombreHuesped": "Luis Pérez Soto",
     *   "idAnfitrion": 7, "nombreAnfitrion": "Ana Gómez Rojas"
     * }
     *   </pre></li>
     *   <li><b>403</b> si el id no es de un huésped con la cuenta activa y sin restricciones.</li>
     * </ul>
     * <p>Uso en el front: pantalla "Mis reservas" del huésped. Mostrar el botón "Cancelar reserva" solo en las que
     * estén en PENDIENTE o CONFIRMADA. Si el arreglo viene vacío, mostrar "Aún no tienes reservas".</p>
     */
    @GetMapping("/huesped")
    public ResponseEntity<List<ReservaListadoDTO>> listarDelHuesped(@RequestParam Integer idHuesped,
                                                                    @RequestParam(required = false) EstadoReserva estado) {
        return ResponseEntity.ok(reservaService.listarDelHuesped(idHuesped, estado));
    }

    /**
     * <b>GET /api/reservas/anfitrion?idAnfitrion=7&amp;estado=CONFIRMADA</b> — Reservas de los alojamientos de un
     * anfitrión, de la más reciente a la más antigua.
     *
     * <p>Pide: como parámetro <code>idAnfitrion</code>, el id del anfitrión que consulta (obligatorio, sin él
     * responde 400). Opcional <code>estado</code> para filtrar, con los mismos valores de la lista del huésped. Sin
     * <code>estado</code> trae todas. Un estado que no existe responde 400.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con un arreglo de ReservaListadoDTO, el mismo objeto de la lista del huésped, vacío
     *       <code>[]</code> si no hay ninguna.</li>
     *   <li><b>403</b> si el id no es de un anfitrión con la cuenta activa y sin restricciones.</li>
     * </ul>
     * <p>Uso en el front: pantalla "Reservas de mis alojamientos" del anfitrión. Mostrar el botón "Cancelar reserva"
     * solo en las que estén en PENDIENTE o CONFIRMADA. Si el arreglo viene vacío, mostrar "Aún no tienes
     * reservas".</p>
     */
    @GetMapping("/anfitrion")
    public ResponseEntity<List<ReservaListadoDTO>> listarDelAnfitrion(@RequestParam Integer idAnfitrion,
                                                                      @RequestParam(required = false) EstadoReserva estado) {
        return ResponseEntity.ok(reservaService.listarDelAnfitrion(idAnfitrion, estado));
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
     * <b>POST /api/reservas/confirmar</b> — El huésped confirma la reserva y paga (pago simulado, MA-102). Para pagar
     * con Stripe usar <b>/api/reservas/checkout</b> (más abajo).
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

    // =====================================================================================================
    //  PAGO CON STRIPE (modo de pruebas). Reemplaza al pago simulado de /confirmar.
    //
    //  Flujo completo para el front:
    //   1. Botón "Reservar y pagar"  -> POST /api/reservas/checkout                      -> recibe "urlPago".
    //   2. window.location.href = urlPago   (el huésped sale a la página de Stripe y paga allá).
    //   3. Stripe lo devuelve a  http://localhost:4200/reservas/pago-exitoso?session_id=cs_test_...
    //      (o, si se arrepiente, a http://localhost:4200/publicaciones/{idPublicacion}?pago=cancelado).
    //   4. La página /reservas/pago-exitoso lee "session_id" de la URL y llama
    //      POST /api/reservas/checkout/{session_id}/confirmar?idHuesped=20    -> recibe la reserva creada.
    //
    //  Tarjetas de prueba de Stripe (cualquier fecha futura, cualquier CVC, cualquier nombre):
    //   4242 4242 4242 4242 -> pago aprobado
    //   4000 0000 0000 0002 -> pago rechazado (Stripe muestra el error en su página y el huésped no sale de ahí)
    //   4000 0025 0000 3155 -> pide autenticación 3D Secure (se aprueba en la ventana que abre Stripe)
    // =====================================================================================================

    /**
     * <b>POST /api/reservas/checkout</b> — Paso 1 del pago con Stripe: crea la sesión de pago y devuelve la URL a la
     * que hay que mandar al huésped.
     *
     * <p>Pide en el cuerpo (igual que /confirmar, pero SIN tarjeta y SIN clave de idempotencia):</p>
     * <pre>
     * {
     *   "idHuesped": 20,
     *   "idPublicacion": 15,
     *   "fechaLlegada": "2026-12-20",
     *   "fechaSalida": "2026-12-23",
     *   "numHuespedes": 3
     * }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>201</b> con el ReservaCheckoutDTO:
     *   <pre>
     * {
     *   "idSesion": "cs_test_a1B2c3...",
     *   "urlPago": "https://checkout.stripe.com/c/pay/cs_test_a1B2c3...",
     *   "idPublicacion": 15,
     *   "tituloPublicacion": "Cabaña con vista al lago",
     *   "noches": 3,
     *   "precioBase": 450000.0,
     *   "tarifaLimpieza": 50000.0,
     *   "total": 500000.0
     * }
     *   </pre>
     *   Todavía NO hay reserva ni se bloquearon las fechas: eso pasa en el paso 2, cuando el huésped ya pagó. La
     *   sesión de Stripe vence a los 60 minutos.</li>
     *   <li><b>400</b> si falta un dato, la salida no es posterior a la llegada, la llegada ya pasó o hay más
     *       huéspedes que la capacidad.</li>
     *   <li><b>403</b> si el id no es de un huésped con la cuenta activa y sin restricciones.</li>
     *   <li><b>404</b> si la publicación no existe.</li>
     *   <li><b>409</b> si las fechas no están disponibles, la publicación no está activa o no tiene precio.</li>
     *   <li><b>502</b> si Stripe respondió con un error.</li>
     *   <li><b>503</b> si el backend no tiene configurada la llave de Stripe (variable STRIPE_SECRET_KEY).</li>
     * </ul>
     * <p>Uso en el front: botón "Reservar y pagar". Con el 201 hacer <code>window.location.href = urlPago</code>
     * (redirección completa, NO router.navigate ni un iframe). Deshabilitar el botón mientras responde para no crear
     * dos sesiones. En los errores mostrar el <code>"message"</code>.</p>
     */
    @PostMapping("/checkout")
    public ResponseEntity<ReservaCheckoutDTO> iniciarPagoStripe(@Valid @RequestBody ReservaCheckoutRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.iniciarPagoStripe(dto));
    }

    /**
     * <b>POST /api/reservas/checkout/{idSesion}/confirmar?idHuesped=20</b> — Paso 2 del pago con Stripe: cuando
     * Stripe devuelve al huésped, el backend revisa con Stripe que el pago se hizo y crea la reserva.
     *
     * <p>Pide: en la ruta el <code>idSesion</code>, que es el <code>session_id</code> que Stripe pone en la URL de
     * regreso (empieza por <code>cs_test_</code>); y como parámetro <code>idHuesped</code>, el huésped en sesión.
     * No lleva cuerpo.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>201</b> con el mismo ReservaConfirmadaDTO de /confirmar. <code>"idTransaccion"</code> trae el id del
     *       cobro en Stripe (empieza por <code>pi_</code>) y <code>"estado"</code> es <code>CONFIRMADA</code>, o
     *       <code>PENDIENTE</code> si el alojamiento necesita aprobación del anfitrión. Las fechas quedan reservadas,
     *       se guarda el pago y se notifica al huésped y al anfitrión.
     *       <br>Se puede llamar varias veces con el mismo <code>idSesion</code> (por ejemplo, si el huésped recarga la
     *       página): siempre devuelve la misma reserva, sin crear otra ni cobrar de nuevo.</li>
     *   <li><b>402</b> si Stripe dice que esa sesión todavía no está pagada. No se crea la reserva.</li>
     *   <li><b>403</b> si el huésped no está activo o la sesión de pago es de otro huésped.</li>
     *   <li><b>404</b> si la sesión no existe en Stripe.</li>
     *   <li><b>409</b> si mientras el huésped pagaba otra persona reservó esas fechas, la publicación dejó de estar
     *       activa o cambió el precio. En ese caso el backend YA le devolvió el pago completo en Stripe y no se creó
     *       la reserva; el <code>"message"</code> lo explica.</li>
     *   <li><b>502</b> / <b>503</b> igual que en el paso 1.</li>
     * </ul>
     * <p>Uso en el front: página <code>/reservas/pago-exitoso</code>. Al cargar, leer <code>session_id</code> de la
     * URL y llamar este endpoint una sola vez. Mientras responde mostrar "Confirmando tu pago...". Con el 201 mostrar
     * el resumen de la reserva y el <code>"mensaje"</code>; en los errores mostrar el <code>"message"</code> y un
     * botón para volver al alojamiento.</p>
     */
    @PostMapping("/checkout/{idSesion}/confirmar")
    public ResponseEntity<ReservaConfirmadaDTO> confirmarPagoStripe(@PathVariable String idSesion,
                                                                    @RequestParam Integer idHuesped) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.confirmarPagoStripe(idSesion, idHuesped));
    }

    /**
     * <b>POST /api/reservas/{id}/cancelar</b> — El huésped cancela su reserva (botón "Cancelar reserva").
     *
     * <p>Pide: el id de la reserva en la ruta y en el cuerpo:</p>
     * <pre>
     * {
     *   "idHuesped": 20,
     *   "motivo": "Cambio de planes"   // obligatorio, máximo 500 caracteres
     * }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con el ReservaCanceladaDTO:
     *   <pre>
     * {
     *   "idReserva": 31, "estado": "CANCELADA", "valorDevolucion": 500000.0,
     *   "estadoDevolucion": "DEVUELTA",       // DEVUELTA | SIN_DEVOLUCION
     *   "mensaje": "La reserva fue cancelada y se devolvió 500000.0"
     * }
     *   </pre>
     *   Se aplica la política de cancelación del alojamiento: con la política flexible se devuelve el total si faltan
     *   24 horas o más para la llegada y nada si falta menos. Las fechas vuelven a quedar disponibles, se guarda la
     *   cancelación y se notifica al huésped y al anfitrión.</li>
     *   <li><b>400</b> si falta el huésped o el motivo, o el motivo pasa de 500 caracteres.</li>
     *   <li><b>403</b> si el id no es de un huésped con la cuenta activa y sin restricciones, o si la reserva no es
     *       suya.</li>
     *   <li><b>404</b> si la reserva no existe.</li>
     *   <li><b>409</b> si la reserva no se puede cancelar, por ejemplo porque ya está CANCELADA. El mensaje trae su
     *       estado actual y no cambia nada.</li>
     * </ul>
     * <p>Uso en el front: botón "Cancelar reserva" en la lista de reservas del huésped, con un campo para el motivo.
     * Al responder 200 mostrar el <code>"mensaje"</code> y actualizar el estado de la reserva. En 403 y 409 mostrar el
     * <code>"message"</code> del error.</p>
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ReservaCanceladaDTO> cancelar(@PathVariable Integer id,
                                                        @Valid @RequestBody ReservaCancelarRequestDTO dto) {
        return ResponseEntity.ok(reservaService.cancelar(id, dto));
    }

    /**
     * <b>POST /api/reservas/{id}/cancelar-anfitrion</b> — El anfitrión cancela una reserva de su alojamiento (botón
     * "Cancelar reserva" en la gestión de reservas del anfitrión).
     *
     * <p>Pide: el id de la reserva en la ruta y en el cuerpo:</p>
     * <pre>
     * {
     *   "idAnfitrion": 7,
     *   "motivo": "Imprevisto en la propiedad"   // obligatorio, máximo 500 caracteres
     * }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con el ReservaCanceladaDTO, el mismo de la cancelación del huésped:
     *   <pre>
     * {
     *   "idReserva": 31, "estado": "CANCELADA", "valorDevolucion": 500000.0,
     *   "estadoDevolucion": "DEVUELTA",
     *   "mensaje": "La reserva fue cancelada y se devolvió 500000.0"
     * }
     *   </pre>
     *   La reserva queda CANCELADA, las fechas vuelven a quedar disponibles, se guarda la cancelación y se notifica
     *   al huésped. Como la cancelación es del anfitrión, al huésped siempre se le devuelve el total de la reserva,
     *   sin aplicar la política de cancelación.</li>
     *   <li><b>400</b> si falta el anfitrión o el motivo, o el motivo pasa de 500 caracteres.</li>
     *   <li><b>403</b> si el id no es de un anfitrión con la cuenta activa y sin restricciones, o si la reserva no es
     *       de uno de sus alojamientos.</li>
     *   <li><b>404</b> si la reserva no existe.</li>
     *   <li><b>409</b> si la reserva no se puede cancelar, por ejemplo porque ya está CANCELADA. El mensaje trae su
     *       estado actual y no cambia nada.</li>
     * </ul>
     * <p>Uso en el front: botón "Cancelar reserva" para el anfitrión, con un campo para el motivo. Al responder 200
     * mostrar el <code>"mensaje"</code> y actualizar el estado de la reserva. En 403 y 409 mostrar el
     * <code>"message"</code> del error.</p>
     */
    @PostMapping("/{id}/cancelar-anfitrion")
    public ResponseEntity<ReservaCanceladaDTO> cancelarComoAnfitrion(@PathVariable Integer id,
                                                                     @Valid @RequestBody ReservaCancelarAnfitrionRequestDTO dto) {
        return ResponseEntity.ok(reservaService.cancelarComoAnfitrion(id, dto));
    }

    /**
     * <b>GET /api/reservas/{id}/cancelacion/simulacion?idHuesped=20</b> — Cuánto se le devolvería al huésped si cancela
     * ahora su reserva. Solo consulta: no cancela nada ni guarda nada.
     *
     * <p>Pide: el id de la reserva en la ruta y el parámetro <code>idHuesped</code>, el id del huésped que hizo la
     * reserva. Es obligatorio, sin él responde 400.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con el SimulacionCancelacionDTO:
     *   <pre>
     * {
     *   "idReserva": 31, "estado": "CONFIRMADA", "politica": "FLEXIBLE",
     *   "valorTotal": 500000.0, "valorDevolucion": 500000.0, "aplicaDevolucion": true,
     *   "mensaje": "Si cancelas ahora se te devolverían 500000.0"
     * }
     *   </pre>
     *   El valor se calcula con la política de cancelación del alojamiento y es el mismo que se devuelve al cancelar
     *   en ese momento. Con la política flexible se devuelve el total si faltan 24 horas o más para la llegada y nada
     *   si falta menos. Si no hay devolución, <code>"valorDevolucion"</code> es 0, <code>"aplicaDevolucion"</code> es
     *   false y el mensaje lo dice.</li>
     *   <li><b>403</b> si el id no es de un huésped con la cuenta activa y sin restricciones, o si la reserva no es
     *       suya.</li>
     *   <li><b>404</b> si la reserva no existe.</li>
     *   <li><b>409</b> si la reserva ya no se puede cancelar, por ejemplo porque ya está CANCELADA. El mensaje trae su
     *       estado actual.</li>
     * </ul>
     * <p>Uso en el front: al abrir el diálogo de "Cancelar reserva", llamar a este endpoint y mostrar el
     * <code>"mensaje"</code> para que el huésped sepa cuánto recibiría antes de confirmar. En 403 y 409 mostrar el
     * <code>"message"</code> del error.</p>
     */
    @GetMapping("/{id}/cancelacion/simulacion")
    public ResponseEntity<SimulacionCancelacionDTO> simularCancelacion(@PathVariable Integer id,
                                                                       @RequestParam Integer idHuesped) {
        return ResponseEntity.ok(reservaService.simularCancelacion(id, idHuesped));
    }
}
