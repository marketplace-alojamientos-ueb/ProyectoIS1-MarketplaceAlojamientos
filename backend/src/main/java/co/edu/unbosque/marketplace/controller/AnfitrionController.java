package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.AnfitrionActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.AnfitrionResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.service.AnfitrionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * CRUD del anfitrión. Base: <b>/api/anfitriones</b>
 *
 * <p>Todas las respuestas son JSON. El objeto <b>AnfitrionResponseDTO</b> que devuelven es:</p>
 * <pre>
 * {
 *   "idUsuario": 7,
 *   "cedula": "1012345678",
 *   "primerNombre": "Ana",
 *   "segundoNombre": "María",          // puede ser null
 *   "primerApellido": "Gómez",
 *   "segundoApellido": "Rojas",
 *   "nombreCompleto": "Ana María Gómez Rojas",
 *   "correo": "ana@correo.com",
 *   "estadoCuenta": "ACTIVA",          // ACTIVA | DESACTIVADA
 *   "restringido": false
 * }
 * </pre>
 * <p>Los errores llegan con el formato por defecto de Spring; el texto para mostrar está en
 * <code>"message"</code>, ej. <code>{ "status": 404, "error": "Not Found", "message": "Anfitrión no encontrado con id: 1", ... }</code>.
 * Los de validación (400) traen además <code>"errors"</code> con el campo y el mensaje de cada input inválido.</p>
 */
@RestController
@RequestMapping("/api/anfitriones")
public class AnfitrionController {

    private final AnfitrionService anfitrionService;

    public AnfitrionController(AnfitrionService anfitrionService) {
        this.anfitrionService = anfitrionService;
    }

    /**
     * <b>GET /api/anfitriones</b> — Lista todos los anfitriones (activos y desactivados).
     *
     * <p>Pide: nada.</p>
     * <p>Retorna: <b>200</b> con un arreglo de AnfitrionResponseDTO (vacío <code>[]</code> si no hay ninguno).</p>
     * <p>Uso en el front: tablas de administración o pruebas; no es para la vista del anfitrión.</p>
     */
    @GetMapping
    public ResponseEntity<List<AnfitrionResponseDTO>> listarTodos() {
        return ResponseEntity.ok(anfitrionService.listarTodos());
    }

    /**
     * <b>GET /api/anfitriones/{idUsuario}</b> — Consulta un anfitrión por su id.
     *
     * <p>Pide: el idUsuario en la URL, ej. <code>/api/anfitriones/7</code>.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con el AnfitrionResponseDTO.</li>
     *   <li><b>404</b> si no existe un anfitrión con ese id.</li>
     *   <li><b>400</b> si el id no es un número.</li>
     * </ul>
     * <p>Uso en el front: cargar la pantalla "Mi perfil" con el idUsuario guardado al iniciar sesión.</p>
     */
    @GetMapping("/{idUsuario}")
    public ResponseEntity<AnfitrionResponseDTO> buscarPorId(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(anfitrionService.buscarPorId(idUsuario));
    }

    /**
     * <b>POST /api/anfitriones</b> — Registro de un anfitrión nuevo.
     *
     * <p>Pide (body JSON):</p>
     * <pre>
     * {
     *   "cedula": "1012345678",            // obligatorio, texto de 6 a 10 dígitos
     *   "primerNombre": "Ana",             // obligatorio, máx. 50
     *   "segundoNombre": "María",          // opcional, máx. 50
     *   "primerApellido": "Gómez",         // obligatorio, máx. 50
     *   "segundoApellido": "Rojas",        // obligatorio, máx. 50
     *   "correo": "ana@correo.com",         // obligatorio, formato de correo, máx. 200
     *   "password": "Clave1234"            // obligatorio, 8 a 64 caracteres, mínimo una letra y un número
     * }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>201</b> con el AnfitrionResponseDTO creado (estadoCuenta = ACTIVA).</li>
     *   <li><b>400</b> si algún campo no cumple las validaciones (ver <code>"campos"</code>).</li>
     *   <li><b>409</b> si ya existe un usuario con esa cédula o ese correo.</li>
     * </ul>
     * <p>Uso en el front: formulario "Regístrate como anfitrión". Al recibir 201, redirigir al login
     * (el registro no inicia sesión automáticamente).</p>
     */
    @PostMapping
    public ResponseEntity<AnfitrionResponseDTO> registrar(@Valid @RequestBody AnfitrionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anfitrionService.registrar(dto));
    }

    /**
     * <b>PUT /api/anfitriones/{idUsuario}</b> — Actualiza los datos personales del anfitrión.
     *
     * <p>Pide: el idUsuario en la URL y en el body todos los campos editables (se reemplazan todos):</p>
     * <pre>
     * {
     *   "primerNombre": "Ana",
     *   "segundoNombre": null,
     *   "primerApellido": "Gómez",
     *   "segundoApellido": "Rojas",
     *   "correo": "nuevo@correo.com"
     * }
     * </pre>
     * <p>No cambia la cédula ni la contraseña.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con el AnfitrionResponseDTO actualizado.</li>
     *   <li><b>400</b> si algún campo no cumple las validaciones.</li>
     *   <li><b>404</b> si no existe el anfitrión.</li>
     *   <li><b>409</b> si el correo nuevo ya lo usa otro usuario.</li>
     * </ul>
     * <p>Uso en el front: botón "Guardar" de la pantalla "Editar perfil".</p>
     */
    @PutMapping("/{idUsuario}")
    public ResponseEntity<AnfitrionResponseDTO> actualizar(@PathVariable Integer idUsuario,
                                                           @Valid @RequestBody AnfitrionActualizarDTO dto) {
        return ResponseEntity.ok(anfitrionService.actualizar(idUsuario, dto));
    }

    /**
     * <b>DELETE /api/anfitriones/{idUsuario}</b> — Desactiva la cuenta del anfitrión (borrado lógico).
     *
     * <p>No borra el registro de la base de datos: deja <code>estadoCuenta = DESACTIVADA</code>, así se
     * conservan sus alojamientos y reservas, y ya no podrá iniciar sesión.</p>
     * <p>Pide: el idUsuario en la URL.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>204</b> sin contenido si se desactivó.</li>
     *   <li><b>404</b> si no existe el anfitrión.</li>
     * </ul>
     * <p>Uso en el front: opción "Eliminar mi cuenta" (pedir confirmación antes) y luego cerrar sesión.</p>
     */
    @DeleteMapping("/{idUsuario}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer idUsuario) {
        anfitrionService.desactivar(idUsuario);
        return ResponseEntity.noContent().build();
    }

    /**
     * <b>POST /api/anfitriones/login</b> — Inicio de sesión del anfitrión.
     *
     * <p>Pide (body JSON):</p>
     * <pre>
     * { "correo": "ana@correo.com", "password": "Clave1234" }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con:
     *     <pre>{ "idUsuario": 7, "nombreCompleto": "Ana María Gómez Rojas", "correo": "ana@correo.com", "rol": "ANFITRION" }</pre>
     *   </li>
     *   <li><b>400</b> si falta el correo o la contraseña, o el correo no tiene formato válido.</li>
     *   <li><b>401</b> si el correo no es de un anfitrión o la contraseña es incorrecta
     *       (mismo mensaje en ambos casos: "Correo o contraseña incorrectos").</li>
     *   <li><b>403</b> si la cuenta está desactivada o restringida por un administrador.</li>
     * </ul>
     * <p>Uso en el front: formulario de login del anfitrión. Guardar la respuesta (ej. en sessionStorage)
     * para saber quién está conectado y usar el <code>idUsuario</code> en los demás endpoints.
     * Provisional: todavía no se entrega token (JWT); cuando se agregue vendrá en esta misma respuesta.</p>
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(anfitrionService.iniciarSesion(dto));
    }
}
