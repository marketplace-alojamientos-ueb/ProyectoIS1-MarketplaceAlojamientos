package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.HuespedRequestDTO;
import co.edu.unbosque.marketplace.model.dto.HuespedResponseDTO;
import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.LoginResponseDTO;
import co.edu.unbosque.marketplace.service.HuespedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Registro, consulta e inicio de sesión del huésped. Base: <b>/api/huespedes</b>
 *
 * <p>El objeto <b>HuespedResponseDTO</b> que devuelven es:</p>
 * <pre>
 * {
 *   "idUsuario": 12,
 *   "cedula": "1012345678",
 *   "primerNombre": "Laura",
 *   "segundoNombre": "Sofía",          // puede ser null
 *   "primerApellido": "Gómez",
 *   "segundoApellido": "Pérez",
 *   "nombreCompleto": "Laura Sofía Gómez Pérez",
 *   "correo": "laura@correo.com",
 *   "estadoCuenta": "ACTIVA",          // ACTIVA | DESACTIVADA
 *   "restringido": false
 * }
 * </pre>
 * <p>Los errores llegan con el formato por defecto de Spring; el texto para mostrar está en
 * <code>"message"</code>. Los de validación (400) traen además <code>"errors"</code>.</p>
 * <p>La recuperación de contraseña es la misma de todos los usuarios: <b>/api/usuarios</b>.</p>
 */
@RestController
@RequestMapping("/api/huespedes")
public class HuespedController {

    private final HuespedService huespedService;

    public HuespedController(HuespedService huespedService) {
        this.huespedService = huespedService;
    }

    /**
     * <b>POST /api/huespedes</b> — Registro de un huésped nuevo.
     *
     * <p>Pide (body JSON):</p>
     * <pre>
     * {
     *   "cedula": "1012345678",            // obligatorio, texto de 6 a 10 dígitos
     *   "primerNombre": "Laura",           // obligatorio, máx. 50
     *   "segundoNombre": "Sofía",          // opcional, máx. 50
     *   "primerApellido": "Gómez",         // obligatorio, máx. 50
     *   "segundoApellido": "Pérez",        // obligatorio, máx. 50
     *   "correo": "laura@correo.com",      // obligatorio, formato de correo, máx. 200
     *   "password": "Clave1234"            // obligatorio, 8 a 64 caracteres, mínimo una letra y un número
     * }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>201</b> con el HuespedResponseDTO creado (estadoCuenta = ACTIVA).</li>
     *   <li><b>400</b> si algún campo no cumple las validaciones.</li>
     *   <li><b>409</b> si ya existe un usuario con esa cédula o ese correo.</li>
     * </ul>
     * <p>Uso en el front: formulario "Regístrate como huésped". Al recibir 201, redirigir al login
     * (el registro no inicia sesión automáticamente).</p>
     */
    @PostMapping
    public ResponseEntity<HuespedResponseDTO> registrar(@Valid @RequestBody HuespedRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(huespedService.registrar(dto));
    }

    /**
     * <b>GET /api/huespedes/{idUsuario}</b> — Consulta un huésped por su id.
     *
     * <p>Retorna: <b>200</b> con el HuespedResponseDTO, <b>404</b> si no existe, <b>400</b> si el id no es un número.</p>
     */
    @GetMapping("/{idUsuario}")
    public ResponseEntity<HuespedResponseDTO> buscarPorId(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(huespedService.buscarPorId(idUsuario));
    }

    /**
     * <b>POST /api/huespedes/login</b> — Inicio de sesión del huésped.
     *
     * <p>Pide (body JSON): <code>{ "correo": "laura@correo.com", "password": "Clave1234" }</code></p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con <code>{ "idUsuario": 12, "nombreCompleto": "...", "correo": "...", "rol": "HUESPED" }</code>.</li>
     *   <li><b>400</b> si falta el correo o la contraseña, o el correo no tiene formato válido.</li>
     *   <li><b>401</b> si el correo no es de un huésped o la contraseña es incorrecta
     *       ("Correo o contraseña incorrectos").</li>
     *   <li><b>403</b> si la cuenta está desactivada o restringida por un administrador.</li>
     * </ul>
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(huespedService.iniciarSesion(dto));
    }
}
