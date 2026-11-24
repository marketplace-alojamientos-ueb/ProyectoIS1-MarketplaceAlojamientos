package co.edu.unbosque.marketplace.controller;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaResponseDTO;
import co.edu.unbosque.marketplace.service.ResenaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Reseñas y calificaciones. Base: <b>/api/resenas</b>
 *
 * <p>El objeto <b>ResenaResponseDTO</b> es:</p>
 * <pre>
 * {
 *   "idResena": 5,
 *   "idReserva": 31,
 *   "idPublicacion": 15,
 *   "tituloPublicacion": "Cabaña con vista al lago",
 *   "idHuesped": 20,
 *   "nombreHuesped": "Luis Pérez Soto",
 *   "calificacion": 4,                    // de 1 a 5
 *   "comentario": "Muy buena estancia",   // puede venir null
 *   "fecha": "2026-11-23",
 *   "visible": true                       // false si un administrador la ocultó
 * }
 * </pre>
 * <p>Los errores llegan con el formato por defecto de Spring; el texto para mostrar está en
 * <code>"message"</code>.</p>
 */
@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    /**
     * <b>POST /api/resenas</b> — El huésped deja la reseña de una estancia completada.
     *
     * <p>Pide (JSON): <code>{ "idHuesped": 20, "idReserva": 31, "calificacion": 4, "comentario": "..." }</code>.
     * La calificación va de 1 a 5 y el comentario es opcional (máximo 1000 caracteres).</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>201</b> con la ResenaResponseDTO creada.</li>
     *   <li><b>400</b> si faltan datos o la calificación no está entre 1 y 5.</li>
     *   <li><b>403</b> si el huésped no tiene la cuenta activa o la reserva no es suya.</li>
     *   <li><b>404</b> si la reserva no existe.</li>
     *   <li><b>409</b> si la estancia todavía no se completó (la reserva debe estar COMPLETADA, o CONFIRMADA con la
     *       fecha de salida ya pasada) o si la reserva ya tiene reseña.</li>
     * </ul>
     * <p>Uso en el front: botón "Dejar reseña" en las reservas terminadas del huésped.</p>
     */
    @PostMapping
    public ResponseEntity<ResenaResponseDTO> crear(@Valid @RequestBody ResenaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaService.crear(dto));
    }

    /**
     * <b>GET /api/resenas/{id}</b> — Una reseña.
     *
     * <p>Retorna <b>200</b> con la ResenaResponseDTO, o <b>404</b> si no existe o fue ocultada.</p>
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResenaResponseDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(resenaService.obtener(id));
    }

    /**
     * <b>GET /api/resenas/publicacion/{idPublicacion}</b> — Reseñas visibles de un alojamiento.
     *
     * <p>Retorna <b>200</b> con un arreglo de ResenaResponseDTO, de la más reciente a la más antigua. Las ocultas por
     * moderación no aparecen. Si no hay, el arreglo viene vacío <code>[]</code>.</p>
     * <p>Uso en el front: sección de reseñas del detalle del alojamiento.</p>
     */
    @GetMapping("/publicacion/{idPublicacion}")
    public ResponseEntity<List<ResenaResponseDTO>> listarPorPublicacion(@PathVariable Integer idPublicacion) {
        return ResponseEntity.ok(resenaService.listarPorPublicacion(idPublicacion));
    }

    /**
     * <b>GET /api/resenas/huesped/{idHuesped}</b> — Reseñas que escribió un huésped ("Mis reseñas").
     *
     * <p>Retorna <b>200</b> con un arreglo de ResenaResponseDTO, incluidas las ocultas (con
     * <code>"visible": false</code>), o <b>403</b> si el id no es de un huésped con la cuenta activa.</p>
     */
    @GetMapping("/huesped/{idHuesped}")
    public ResponseEntity<List<ResenaResponseDTO>> listarPorHuesped(@PathVariable Integer idHuesped) {
        return ResponseEntity.ok(resenaService.listarPorHuesped(idHuesped));
    }

    /**
     * <b>PUT /api/resenas/{id}</b> — El autor edita su reseña.
     *
     * <p>Pide (JSON): <code>{ "idHuesped": 20, "calificacion": 5, "comentario": "..." }</code>.</p>
     * <p>Retorna:</p>
     * <ul>
     *   <li><b>200</b> con la ResenaResponseDTO actualizada.</li>
     *   <li><b>400</b> si faltan datos o la calificación no está entre 1 y 5.</li>
     *   <li><b>403</b> si el huésped no está activo o no es el autor.</li>
     *   <li><b>404</b> si la reseña no existe.</li>
     *   <li><b>409</b> si la reseña fue ocultada por moderación.</li>
     * </ul>
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResenaResponseDTO> actualizar(@PathVariable Integer id,
                                                        @Valid @RequestBody ResenaActualizarDTO dto) {
        return ResponseEntity.ok(resenaService.actualizar(id, dto));
    }

    /**
     * <b>DELETE /api/resenas/{id}?idHuesped=20</b> — El autor elimina su reseña.
     *
     * <p>Retorna <b>200</b> con <code>{ "mensaje": "La reseña fue eliminada" }</code>; <b>403</b> si el huésped no
     * está activo o no es el autor; <b>404</b> si no existe; <b>409</b> si fue ocultada por moderación.</p>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponseDTO> eliminar(@PathVariable Integer id, @RequestParam Integer idHuesped) {
        return ResponseEntity.ok(resenaService.eliminar(id, idHuesped));
    }
}
