import { TonoInsignia } from '../components/atoms/insignia/insignia';
import { EstadoPublicacion, TipoPeriodo } from '../models/publicacion-anfitrion.model';
import { EstadoReserva } from '../models/reserva.model';


export const ESTADOS_PUBLICACION: Record<EstadoPublicacion, { texto: string; tono: TonoInsignia }> = {
  ACTIVA: { texto: 'Activa', tono: 'exito' },
  PAUSADA: { texto: 'Pausada', tono: 'aviso' },
  PENDIENTE_REVISION: { texto: 'Pendiente de revisión', tono: 'info' },
  BORRADOR: { texto: 'Borrador', tono: 'neutro' },
  BLOQUEADA: { texto: 'Bloqueada', tono: 'error' }
};


export const TIPOS_PERIODO: Record<TipoPeriodo, { texto: string; tono: TonoInsignia }> = {
  DISPONIBLE: { texto: 'Disponible', tono: 'exito' },
  RESERVADO: { texto: 'Reservado', tono: 'info' },
  BLOQUEADO: { texto: 'Bloqueado', tono: 'error' }
};


export const TIPOS_ALOJAMIENTO = [
  { valor: 'APARTAMENTO', texto: 'Apartamento' },
  { valor: 'CASA', texto: 'Casa' },
  { valor: 'CABANA', texto: 'Cabaña' },
  { valor: 'FINCA', texto: 'Finca' },
  { valor: 'HABITACION', texto: 'Habitación' }
];

/** Texto del tipo de alojamiento ("CABANA" → "Cabaña"); si el back trae otro valor, lo muestra tal cual. */
export function textoTipo(valor: string | null): string {
  return TIPOS_ALOJAMIENTO.find((t) => t.valor === valor)?.texto ?? valor ?? '';
}

/** Cómo acepta reservas el anfitrión, como lo guarda el back. */
export const MODOS_ACEPTACION: Record<string, string> = {
  INMEDIATA: 'Reserva inmediata',
  CON_APROBACION: 'Con aprobación del anfitrión'
};

/** Estado de una reserva con su color. */
export const ESTADOS_RESERVA: Record<EstadoReserva, { texto: string; tono: TonoInsignia }> = {
  PENDIENTE: { texto: 'Pendiente de aprobación', tono: 'aviso' },
  CONFIRMADA: { texto: 'Confirmada', tono: 'exito' },
  RECHAZADA: { texto: 'Rechazada', tono: 'error' },
  CANCELADA: { texto: 'Cancelada', tono: 'neutro' },
  COMPLETADA: { texto: 'Completada', tono: 'info' },
  EN_REVISION_MANUAL: { texto: 'En revisión', tono: 'aviso' }
};

/** Solo se pueden cancelar las reservas pendientes o confirmadas (regla del back, MA-95 / MA-96). */
export function sePuedeCancelar(estado: EstadoReserva): boolean {
  return estado === 'PENDIENTE' || estado === 'CONFIRMADA';
}
