package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.LoginRequestDTO;
import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.RecuperarContrasenaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.RestablecerContrasenaRequestDTO;
import co.edu.unbosque.marketplace.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Lo común a cualquier usuario: recuperación de contraseña y reactivación de cuenta. Base: <b>/api/usuarios</b>
 *
 * <p>Los errores llegan con el formato por defecto de Spring; el texto para mostrar está en
 * <code>"message"</code>, ej. <code>{ "status": 400, "error": "Bad Request", "message": "El código es inválido o ya expiró", ... }</code>.</p>
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * <b>POST /api/usuarios/recuperar-contrasena</b> — Paso 1 de "¿Olvidaste tu contraseña?".
     *
     * <p>Genera un código de 6 dígitos, válido por 15 minutos, y lo envía al correo.
     * Pedir otro código invalida el anterior.</p>
     * <p>Pide (body JSON):</p>
     * <pre>{ "correo": "ana@correo.com" }</pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> siempre con el mismo mensaje, exista o no el correo (por seguridad no se revela
     *       qué correos están registrados):
     *     <pre>{ "mensaje": "Si el correo está registrado, recibirás un código para restablecer tu contraseña" }</pre>
     *   </li>
     *   <li><b>400</b> si el correo falta o no tiene formato válido.</li>
     * </ul>
     * <p>Uso en el front: pantalla donde el usuario escribe su correo; al recibir 200, mostrar el mensaje
     * y pasar a la pantalla del paso 2 (código + nueva contraseña) llevando el correo.</p>
     */
    @PostMapping("/recuperar-contrasena")
    public ResponseEntity<MensajeResponseDTO> solicitarRecuperacion(@Valid @RequestBody RecuperarContrasenaRequestDTO dto) {
        usuarioService.solicitarRecuperacion(dto);
        return ResponseEntity.ok(crearMensaje(
                "Si el correo está registrado, recibirás un código para restablecer tu contraseña"));
    }

    /**
     * <b>POST /api/usuarios/restablecer-contrasena</b> — Paso 2: cambiar la contraseña con el código.
     *
     * <p>Pide (body JSON):</p>
     * <pre>
     * {
     *   "correo": "ana@correo.com",
     *   "codigo": "483920",                // 6 dígitos, el que llegó al correo
     *   "nuevaPassword": "NuevaClave1"     // 8 a 64 caracteres, mínimo una letra y un número
     * }
     * </pre>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con <code>{ "mensaje": "Contraseña actualizada correctamente" }</code>.</li>
     *   <li><b>400</b> si algún campo es inválido, o si el código es incorrecto o expiró
     *       (<code>"El código es inválido o ya expiró"</code>). Tras 5 intentos fallidos el código se anula
     *       y hay que pedir uno nuevo.</li>
     * </ul>
     * <p>Uso en el front: formulario del paso 2; al recibir 200, mostrar el mensaje y redirigir al login.</p>
     */
    @PostMapping("/restablecer-contrasena")
    public ResponseEntity<MensajeResponseDTO> restablecerContrasena(@Valid @RequestBody RestablecerContrasenaRequestDTO dto) {
        usuarioService.restablecerContrasena(dto);
        return ResponseEntity.ok(crearMensaje("Contraseña actualizada correctamente"));
    }

    /**
     * <b>POST /api/usuarios/activar-cuenta</b> — Reactiva una cuenta desactivada (huésped o anfitrión).
     *
     * <p>La desactivación se hace con <code>DELETE /api/huespedes/{id}</code> o <code>DELETE /api/anfitriones/{id}</code>.
     * Como una cuenta desactivada no puede iniciar sesión, aquí se confirman las credenciales.</p>
     * <p>Pide (body JSON): <code>{ "correo": "ana@correo.com", "password": "Clave1234" }</code></p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con <code>{ "mensaje": "Cuenta activada correctamente" }</code>.</li>
     *   <li><b>400</b> si falta el correo o la contraseña, o el correo no tiene formato válido.</li>
     *   <li><b>401</b> si el correo no existe o la contraseña es incorrecta ("Correo o contraseña incorrectos").</li>
     *   <li><b>409</b> si la cuenta ya estaba activa.</li>
     * </ul>
     * <p>Uso en el front: opción "Activar mi cuenta" en el login cuando este responde 403 "La cuenta está
     * desactivada". Pedir confirmación, llamar este endpoint y, si responde 200, llevar al login.</p>
     * <p>Una cuenta restringida por un administrador sigue sin poder iniciar sesión aunque se active.</p>
     */
    @PostMapping("/activar-cuenta")
    public ResponseEntity<MensajeResponseDTO> activarCuenta(@Valid @RequestBody LoginRequestDTO dto) {
        usuarioService.activarCuenta(dto);
        return ResponseEntity.ok(crearMensaje("Cuenta activada correctamente"));
    }

    private static MensajeResponseDTO crearMensaje(String texto) {
        MensajeResponseDTO respuesta = new MensajeResponseDTO();
        respuesta.setMensaje(texto);
        return respuesta;
    }
}
