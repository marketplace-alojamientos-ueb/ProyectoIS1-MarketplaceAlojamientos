/** Lo que ve el huésped. Igual a PublicacionBusquedaDTO y PublicacionDetalleDTO del back (MA-99 / MA-100). */

/** GET /api/publicaciones/buscar: cada resultado. Sin datos personales del anfitrión. */
export interface PublicacionBusqueda {
  idPublicacion: number;
  titulo: string;
  descripcion: string;
  ubicacion: string;
  ciudad: string;
  tipoAlojamiento: string;
  capacidad: number;
  precioPorNoche: number;
  tarifaLimpieza: number;
  reglas: string | null;
  modoAceptacion: string;
  politicaCancelacion: string;
  servicios: string[];
}

/** GET /api/publicaciones/{id}/detalle: lo mismo de la búsqueda más las rutas de las imágenes. */
export interface PublicacionDetalle extends PublicacionBusqueda {
  imagenes: string[];
}

/** Query params de GET /api/publicaciones/buscar (PublicacionBusquedaRequestDTO). Todos opcionales. */
export interface FiltrosBusqueda {
  /** Texto exacto, sin importar mayúsculas. */
  ciudad?: string;
  /** Fechas AAAA-MM-DD; se usan juntas. */
  llegada?: string;
  salida?: string;
  /** Personas: trae los que alojan esa cantidad o más. */
  capacidad?: number;
  tipo?: string;
  precioMin?: number;
  precioMax?: number;
  /** Por nombre; trae los que tienen todos los indicados. */
  servicios?: string[];
}