package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionEditarRequestDTO;
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
 *   "politicaCancelacion": "FLEXIBLE",   // código de la política (null en un borrador sin política)
 *   "camposFaltantes": [],               // en BORRADOR: ej. ["Título", "Precio por noche"]
 *   "anfitrion": {                       // datos del anfitrión, sin contraseña
 *     "idUsuario": 7, "cedula": "1012345678", "primerNombre": "Ana", "segundoNombre": "María",
 *     "primerApellido": "Gómez", "segundoApellido": "Rojas", "nombreCompleto": "Ana María Gómez Rojas",
 *     "correo": "ana@correo.com", "estadoCuenta": "ACTIVA", "restringido": false
 *   },
 *   "periodos": [                        // fechas en formato AAAA-MM-DD
 *     { "idPeriodo": 3, "fechaInicio": "2026-12-01", "fechaFin": "2026-12-15", "tipo": "DISPONIBLE" }
 *   ]                                    // tipo: DISPONIBLE | BLOQUEADO | RESERVADO
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
     *   "titulo": "Cabaña con vista al lago",  // mínimo, máx. 52
     *   "descripcion": "Cabaña para 4...",     // mínimo, máx. 500
     *   "ubicacion": "Vereda El Lago, km 3",   // mínimo, máx. 100
     *   "ciudad": "Guatavita",                 // mínimo, máx. 50
     *   "tipoAlojamiento": "CABANA",           // mínimo, máx. 20
     *   "capacidad": 4,                        // mínimo, mayor que 0
     *   "precioPorNoche": 250000,              // mínimo, mayor que 0
     *   "tarifaLimpieza": 30000,               // opcional, 0 o más (si no se envía queda en 0)
     *   "reglas": "No se permiten fiestas",    // opcional, máx. 1000
     *   "modoAceptacion": "INMEDIATA",         // mínimo: INMEDIATA o CON_APROBACION
     *   "politicaCancelacion": "FLEXIBLE",     // mínimo, un código de GET /api/publicaciones/politicas-cancelacion
     *   "periodos": [                          // mínimo, al menos uno; fechas en formato AAAA-MM-DD
     *     { "fechaInicio": "2026-12-01", "fechaFin": "2026-12-15" }
     *   ]
     * }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>201</b> con el PublicacionResponseDTO creado. Si vienen todos los campos mínimos queda en
     *       <code>PENDIENTE_REVISION</code> y <code>camposFaltantes</code> va vacío; si falta alguno queda en
     *       <code>BORRADOR</code> y <code>camposFaltantes</code> trae sus nombres.</li>
     *   <li><b>400</b> si falta el idAnfitrion, si un campo enviado no cumple las validaciones (en
     *       <code>"errors"</code> viene cada campo con su mensaje), si la política de cancelación no existe, o si un
     *       período enviado es inválido: la fecha de fin no es posterior a la de inicio, la de inicio está en el pasado
     *       o se cruza con otro período.</li>
     *   <li><b>403</b> si el id no es de un anfitrión, o su cuenta está desactivada o restringida
     *       ("Solo un anfitrión puede crear publicaciones").</li>
     * </ul>
     * <p>Uso en el front: formulario "Crear publicación". Si la respuesta trae <code>estado = BORRADOR</code>,
     * mostrar la lista <code>camposFaltantes</code> para que el anfitrión los complete. Si responde 400, marcar los
     * campos de <code>"errors"</code>.</p>
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

    /**
     * <b>GET /api/publicaciones/{id}/edicion?idUsuario=7</b> — Datos de una publicación para llenar el formulario de edición.
     *
     * <p>Pide: el id de la publicación en la ruta y, como parámetro <code>idUsuario</code>, el id de quien va a
     * editar (el anfitrión propietario o un administrador).</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con el PublicacionResponseDTO de la publicación.</li>
     *   <li><b>403</b> "Edición no autorizada" si el usuario no es el propietario (con la cuenta activa y sin
     *       restricciones) ni un administrador.</li>
     *   <li><b>404</b> si la publicación no existe.</li>
     * </ul>
     * <p>Uso en el front: al escoger una publicación en "Mis publicaciones" y pulsar editar, para llenar el formulario.</p>
     */
    @GetMapping("/{id}/edicion")
    public ResponseEntity<PublicacionResponseDTO> obtenerParaEditar(@PathVariable Integer id,
                                                                    @RequestParam Integer idUsuario) {
        return ResponseEntity.ok(publicacionService.obtenerParaEditar(id, idUsuario));
    }

    /**
     * <b>GET /api/publicaciones?idAnfitrion=7</b> — "Mis publicaciones": las publicaciones de un anfitrión, cada una con
     * su estado actual.
     *
     * <p>Pide: el parámetro <code>idAnfitrion</code>, el id del anfitrión que consulta. Solo devuelve las de ese
     * anfitrión, en cualquier estado: BORRADOR, PENDIENTE_REVISION, ACTIVA, PAUSADA o BLOQUEADA. Es obligatorio, sin él
     * responde 400.</p>
     * <p>Retorna: <b>200</b> con un arreglo de PublicacionResponseDTO, con el campo <code>estado</code> de cada una.
     * Si el anfitrión no tiene publicaciones, el arreglo viene vacío <code>[]</code>.</p>
     * <p>Uso en el front: lista de "Mis publicaciones". Si el arreglo viene vacío, mostrar el mensaje de que todavía
     * no tiene publicaciones. Cada consulta lee el estado actual, así que al volver a consultar se ve actualizado.</p>
     */
    @GetMapping
    public ResponseEntity<List<PublicacionResponseDTO>> listar(@RequestParam Integer idAnfitrion) {
        return ResponseEntity.ok(publicacionService.listar(idAnfitrion));
    }

    /**
     * <b>GET /api/publicaciones/{id}</b> — Una publicación por su id, sin importar su estado.
     *
     * <p>Pide: el id en la ruta.</p>
     * <p>Retorna: <b>200</b> con el PublicacionResponseDTO, o <b>404</b> si no existe.</p>
     */
    @GetMapping("/{id}")
    public ResponseEntity<PublicacionResponseDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(publicacionService.obtener(id));
    }

    /**
     * <b>PUT /api/publicaciones/{id}</b> — Guarda los cambios de una publicación (botón "Guardar cambios").
     *
     * <p>Pide (body JSON): los mismos campos y validaciones que al crear, con <code>idUsuario</code> en lugar de
     * <code>idAnfitrion</code> y sin <code>periodos</code>, que no se modifican. Reemplaza todos los datos del
     * formulario, por eso hay que enviarlos todos:</p>
     * <pre>
     * {
     *   "idUsuario": 7,                        // obligatorio, quien edita: el propietario o un administrador
     *   "titulo": "Cabaña con vista al lago",  // los demás campos, igual que en POST /api/publicaciones
     *   "descripcion": "...", "ubicacion": "...", "ciudad": "...", "tipoAlojamiento": "...",
     *   "capacidad": 4, "precioPorNoche": 250000, "tarifaLimpieza": 30000, "reglas": "...",
     *   "modoAceptacion": "INMEDIATA", "politicaCancelacion": "FLEXIBLE"
     * }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con el PublicacionResponseDTO actualizado. El estado de la publicación no cambia.</li>
     *   <li><b>400</b> si falta algún campo (en <code>"message"</code> vienen sus nombres), si no cumple las validaciones
     *       (en <code>"errors"</code>), o si la política de cancelación no existe.</li>
     *   <li><b>403</b> "Edición no autorizada" si el usuario no es el propietario (con la cuenta activa y sin
     *       restricciones) ni un administrador.</li>
     *   <li><b>404</b> si la publicación no existe.</li>
     * </ul>
     * <p>Uso en el front: al recibir 200, mostrar el mensaje de confirmación de que la publicación se actualizó.</p>
     */
    @PutMapping("/{id}")
    public ResponseEntity<PublicacionResponseDTO> editar(@PathVariable Integer id,
                                                         @Valid @RequestBody PublicacionEditarRequestDTO dto) {
        return ResponseEntity.ok(publicacionService.editar(id, dto));
    }

    /**
     * <b>DELETE /api/publicaciones/{id}?idUsuario=7</b> — Elimina una publicación con sus imágenes, períodos y servicios.
     *
     * <p>Pide: el id en la ruta y, como parámetro <code>idUsuario</code>, el id de quien elimina (el anfitrión
     * propietario o un administrador).</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>204</b> sin cuerpo si se eliminó.</li>
     *   <li><b>403</b> "Eliminación no autorizada" si el usuario no es el propietario (con la cuenta activa y sin
     *       restricciones) ni un administrador.</li>
     *   <li><b>404</b> si la publicación no existe.</li>
     *   <li><b>409</b> si no se puede eliminar porque tiene datos asociados, como reservas.</li>
     * </ul>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, @RequestParam Integer idUsuario) {
        publicacionService.eliminar(id, idUsuario);
        return ResponseEntity.noContent().build();
    }

    /**
     * <b>POST /api/publicaciones/{id}/pausar?idUsuario=7</b> — Pausa una publicación ACTIVA para dejar de recibir
     * nuevas reservas de forma temporal.
     *
     * <p>Pide: el id de la publicación en la ruta y, como parámetro <code>idUsuario</code>, el id de quien pausa
     * (el anfitrión propietario o un administrador). Sin body.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con el PublicacionResponseDTO, ya con estado <code>PAUSADA</code>.</li>
     *   <li><b>403</b> si el usuario no es el anfitrión propietario (con la cuenta activa y sin restricciones) ni un
     *       administrador.</li>
     *   <li><b>404</b> si la publicación no existe.</li>
     *   <li><b>409</b> si la publicación está en BORRADOR, PENDIENTE_REVISION, PAUSADA o BLOQUEADA. El mensaje en
     *       <code>"message"</code> informa que no se puede pausar y la publicación no cambia de estado.</li>
     * </ul>
     * <p>Uso en el front: pedir la confirmación al anfitrión y llamar a este endpoint solo si confirma. Si cancela, no
     * se llama y la publicación sigue ACTIVA. Las búsquedas y las reservas deben ignorar las publicaciones que no estén
     * ACTIVAS.</p>
     */
    @PostMapping("/{id}/pausar")
    public ResponseEntity<PublicacionResponseDTO> pausar(@PathVariable Integer id, @RequestParam Integer idUsuario) {
        return ResponseEntity.ok(publicacionService.pausar(id, idUsuario));
    }
}
