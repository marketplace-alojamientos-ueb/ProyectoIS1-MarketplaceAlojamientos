/** Publicaciones del anfitrión. Igual a PublicacionResponseDTO del back (MA-107, MA-108). */

import { UsuarioResponse } from './usuario.model';

export type EstadoPublicacion = 'BORRADOR' | 'PENDIENTE_REVISION' | 'ACTIVA' | 'PAUSADA' | 'BLOQUEADA';

export type ModoAceptacion = 'INMEDIATA' | 'CON_APROBACION';

export type TipoPeriodo = 'DISPONIBLE' | 'BLOQUEADO' | 'RESERVADO';


export interface PeriodoDisponibilidad {
  idPeriodo: number;
  fechaInicio: string;
  fechaFin: string;
  tipo: TipoPeriodo;
}

export interface PublicacionAnfitrion {
  idPublicacion: number;
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
  estado: EstadoPublicacion;
  anfitrion: UsuarioResponse;
  periodos: PeriodoDisponibilidad[];
  camposFaltantes: string[];
}