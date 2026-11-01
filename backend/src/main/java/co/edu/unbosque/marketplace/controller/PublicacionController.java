package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionResponseDTO;
import co.edu.unbosque.marketplace.service.PublicacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Publicaciones de alojamientos. Base: <b>/api/publicaciones</b>
 *
 * <p>El objeto <b>PublicacionResponseDTO</b> que devuelve la creación es:</p>
 * <pre>
 * {
 *   "idPublicacion": 15,
 *   "estado": "PENDIENTE_REVISION",    // BORRADOR | PENDIENTE_REVISION | ACTIVA | PAUSADA | BLOQUEADA
 *   "titulo": "Cabaña con vista al lago",
 *   "descripcion": "Cabaña para 4 personas...",
 *   "ubicacion": "Vereda El Lago, km 3",
 *   "ciudad": "Guatavita",
 *   "tipoAlojamiento": "CABANA",
 *   "capacidad": 4,
 *   "precioPorNoche": 250000.0,
 *   "tarifaLimpieza": 30000.0,
 *   "reglas": "No se permiten fiestas",  // puede ser null
 *   "modoAceptacion": "INMEDIATA",       // INMEDIATA | CON_APROBACION
 *   "politicaCancelacion": "FLEXIBLE",   // código de la política
 *   "anfitrion": {                       // datos del anfitrión, sin contraseña
 *     "idUsuario": 7, "cedula": "1012345678", "primerNombre": "Ana", "segundoNombre": "María",
 *     "primerApellido": "Gómez", "segundoApellido": "Rojas", "nombreCompleto": "Ana María Gómez Rojas",
 *     "correo": "ana@correo.com", "estadoCuenta": "ACTIVA", "restringido": false
 *   }
 * }
 * </pre>
 * <p>Los errores llegan con el formato por defecto de Spring; el texto para mostrar está en
 * <code>"message"</code>. Los de validación (400) traen además <code>"errors"</code>.</p>
 */
@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionService publicacionService;

    public PublicacionController(PublicacionService publicacionService) {
        this.publicacionService = publicacionService;
    }

    /**
     * <b>POST /api/publicaciones</b> — Crea una publicación (botón "Enviar publicación").
     *
     * <p>Pide (body JSON):</p>
     * <pre>
     * {
     *   "idAnfitrion": 7,                      // obligatorio, id del anfitrión que publica
     *   "titulo": "Cabaña con vista al lago",  // obligatorio, máx. 52
     *   "descripcion": "Cabaña para 4...",     // obligatorio, máx. 500
     *   "ubicacion": "Vereda El Lago, km 3",   // obligatorio, máx. 100
     *   "ciudad": "Guatavita",                 // obligatorio, máx. 50
     *   "tipoAlojamiento": "CABANA",           // obligatorio, máx. 20
     *   "capacidad": 4,                        // obligatorio, mayor que 0
     *   "precioPorNoche": 250000,              // obligatorio, mayor que 0
     *   "tarifaLimpieza": 30000,               // opcional, 0 o más (si no se envía queda en 0)
     *   "reglas": "No se permiten fiestas",    // opcional, máx. 1000
     *   "modoAceptacion": "INMEDIATA",         // obligatorio: INMEDIATA o CON_APROBACION
     *   "politicaCancelacion": "FLEXIBLE"      // obligatorio, un código de GET /api/publicaciones/politicas-cancelacion
     * }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>201</b> con el PublicacionResponseDTO creado, con estado <code>PENDIENTE_REVISION</code>.</li>
     *   <li><b>400</b> si falta algún campo o no cumple las validaciones (en <code>"errors"</code> viene cada campo
     *       con su mensaje), o si la política de cancelación no existe.</li>
     *   <li><b>403</b> si el id no es de un anfitrión, o su cuenta está desactivada o restringida
     *       ("Solo un anfitrión puede crear publicaciones").</li>
     * </ul>
     * <p>Uso en el front: formulario "Crear publicación". Si responde 400, marcar los campos de
     * <code>"errors"</code> para que el anfitrión los complete.</p>
     */
    @PostMapping
    public ResponseEntity<PublicacionResponseDTO> crear(@Valid @RequestBody PublicacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacionService.crear(dto));
    }

    /**
     * <b>GET /api/publicaciones/politicas-cancelacion</b> — Opciones de política de cancelación.
     *
     * <p>Pide: nada.</p>
     * <p>Retorna: <b>200</b> con un arreglo de opciones (vacío <code>[]</code> si no hay ninguna):</p>
     * <pre>[ { "codigo": "FLEXIBLE", "nombre": "Flexible", "descripcion": "..." } ]</pre>
     * <p>Uso en el front: llenar la lista desplegable del formulario de crear publicación. Al enviar la
     * publicación, mandar el <code>codigo</code> en el campo <code>politicaCancelacion</code>.</p>
     */
    @GetMapping("/politicas-cancelacion")
    public ResponseEntity<List<PoliticaCancelacionDTO>> listarPoliticas() {
        return ResponseEntity.ok(publicacionService.listarPoliticas());
    }
}
