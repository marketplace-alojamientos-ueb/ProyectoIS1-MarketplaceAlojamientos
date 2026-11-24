/** Igual a los DTO de reseñas del back (ResenaController: MA-81, MA-82). */

/** ResenaResponseDTO. */
export interface Resena {
  idResena: number;
  idReserva: number;
  idPublicacion: number;
  tituloPublicacion: string;
  idHuesped: number;
  nombreHuesped: string;
  /** De 1 a 5. */
  calificacion: number;
  comentario: string | null;
  /** AAAA-MM-DD. */
  fecha: string;
  /** false si un administrador la ocultó. */
  visible: boolean;
}

/** Lo que el huésped escribe: calificación de 1 a 5 y comentario opcional (máximo 1000). */
export interface DatosResena {
  calificacion: number;
  comentario: string | null;
}

/** POST /api/resenas (ResenaRequestDTO). */
export interface ResenaRequest extends DatosResena {
  idHuesped: number;
  idReserva: number;
}

/** PUT /api/resenas/{id} (ResenaActualizarDTO). */
export interface ResenaActualizar extends DatosResena {
  idHuesped: number;
}

/** POST /api/resenas/{id}/reportar (ResenaReportarDTO). Motivo obligatorio, máximo 500. */
export interface ResenaReportar {
  idUsuario: number;
  motivo: string;
}
