/** Publicaciones del anfitrión. Igual a los DTO de PublicacionController del back (MA-64, MA-61, MA-76, MA-77, MA-107). */

import { UsuarioResponse } from './usuario.model';

export type EstadoPublicacion = 'BORRADOR' | 'PENDIENTE_REVISION' | 'ACTIVA' | 'PAUSADA' | 'BLOQUEADA';

export type ModoAceptacion = 'INMEDIATA' | 'CON_APROBACION';

export type TipoPeriodo = 'DISPONIBLE' | 'BLOQUEADO' | 'RESERVADO';

/** Fechas en formato AAAA-MM-DD, como las envía y recibe el back. */
export interface Periodo {
  fechaInicio: string;
  fechaFin: string;
}

export interface PeriodoDisponibilidad extends Periodo {
  idPeriodo: number;
  tipo: TipoPeriodo;
}

/** Campos del formulario. Al crear pueden ir incompletos: la publicación queda en BORRADOR (MA-76). */
export interface DatosPublicacion {
  titulo: string | null;
  descripcion: string | null;
  ubicacion: string | null;
  ciudad: string | null;
  tipoAlojamiento: string | null;
  capacidad: number | null;
  precioPorNoche: number | null;
  tarifaLimpieza: number;
  reglas: string | null;
  modoAceptacion: ModoAceptacion | null;
  politicaCancelacion: string | null;
}

/** POST /api/publicaciones */
export interface CrearPublicacionRequest extends DatosPublicacion {
  idAnfitrion: number;
  periodos: Periodo[];
}

/** PUT /api/publicaciones/{id}: todos los campos, sin periodos. */
export interface EditarPublicacionRequest extends DatosPublicacion {
  idUsuario: number;
}

/** PublicacionResponseDTO */
export interface PublicacionAnfitrion extends DatosPublicacion {
  idPublicacion: number;
  estado: EstadoPublicacion;
  anfitrion: UsuarioResponse;
  periodos: PeriodoDisponibilidad[];
  camposFaltantes: string[];
}

/** GET /api/publicaciones/politicas-cancelacion */
export interface PoliticaCancelacion {
  codigo: string;
  nombre: string;
  descripcion: string;
}