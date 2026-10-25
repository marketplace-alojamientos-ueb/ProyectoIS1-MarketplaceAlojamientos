package co.edu.unbosque.marketplace.controlador;

import co.edu.unbosque.marketplace.modelo.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.modelo.dto.RecuperarContrasenaRequestDTO;
import co.edu.unbosque.marketplace.modelo.dto.RestablecerContrasenaRequestDTO;
import co.edu.unbosque.marketplace.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Lo común a cualquier usuario, por ahora la recuperación de contraseña. Base: <b>/api/usuarios</b>
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
     * <pre>{ "email": "ana@correo.com" }</pre>
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
        return ResponseEntity.ok(new MensajeResponseDTO(
                "Si el correo está registrado, recibirás un código para restablecer tu contraseña"));
    }

    /**
     * <b>POST /api/usuarios/restablecer-contrasena</b> — Paso 2: cambiar la contraseña con el código.
     *
     * <p>Pide (body JSON):</p>
     * <pre>
     * {
     *   "email": "ana@correo.com",
     *   "codigo": "483920",                // 6 dígitos, el que llegó al correo
     *   "nuevaContrasena": "NuevaClave1"   // 8 a 64 caracteres, mínimo una letra y un número
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
        return ResponseEntity.ok(new MensajeResponseDTO("Contraseña actualizada correctamente"));
    }
}
