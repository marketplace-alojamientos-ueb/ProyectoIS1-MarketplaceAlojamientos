import { TonoInsignia } from '../components/atoms/insignia/insignia';
import { EstadoPublicacion, TipoPeriodo } from '../models/publicacion-anfitrion.model';


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