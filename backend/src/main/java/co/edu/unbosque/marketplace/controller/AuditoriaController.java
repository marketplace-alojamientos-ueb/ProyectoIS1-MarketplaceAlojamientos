package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.EventoAuditoriaDTO;
import co.edu.unbosque.marketplace.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Auditoría. Base: <b>/api/auditoria</b>
 *
 * <p>El objeto <b>EventoAuditoriaDTO</b> es:</p>
 * <pre>
 * {
 *   "idEvento": 12,
 *   "idUsuario": 20,                      // null si el evento no tiene usuario
 *   "operacion": "CANCELAR_RESERVA",
 *   "entidadAfectada": "RESERVA",
 *   "idEntidad": 31,
 *   "fecha": "2026-11-23T12:30:00",
 *   "resultado": "CANCELADA",
 *   "detalle": "Motivo: Cambio de planes. Devolución: 500000.0 (DEVUELTA)"
 * }
 * </pre>
 * <p>Los errores llegan con el formato por defecto de Spring; el texto para mostrar está en
 * <code>"message"</code>.</p>
 */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /**
     * <b>GET /api/auditoria/eventos?idAdmin=4&amp;operacion=CANCELAR_RESERVA</b> — Eventos de auditoría de la
     * plataforma.
     *
     * <p>Pide: como parámetro <code>idAdmin</code>, el id del administrador que consulta. Es obligatorio, sin él
     * responde 400. El parámetro <code>operacion</code> es opcional: si se envía, solo trae los eventos de esa
     * operación, por ejemplo <code>CANCELAR_RESERVA</code>.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con un arreglo de EventoAuditoriaDTO, del más reciente al más antiguo. Si no hay eventos, el
     *       arreglo viene vacío <code>[]</code>.</li>
     *   <li><b>403</b> si el id no es de un administrador con la cuenta activa y sin restricciones.</li>
     * </ul>
     * <p>Uso en el front: pantalla de auditoría del administrador. Si el arreglo viene vacío, mostrar el mensaje "No
     * hay eventos registrados". El filtro por operación se puede usar con una lista desplegable.</p>
     */
    @GetMapping("/eventos")
    public ResponseEntity<List<EventoAuditoriaDTO>> listarEventos(@RequestParam Integer idAdmin,
                                                                  @RequestParam(required = false) String operacion) {
        return ResponseEntity.ok(auditoriaService.listarEventos(idAdmin, operacion));
    }
}
