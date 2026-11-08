import { TonoInsignia } from '../components/atoms/insignia/insignia';
import { EstadoPublicacion } from '../models/publicacion-anfitrion.model';


export const ESTADOS_PUBLICACION: Record<EstadoPublicacion, { texto: string; tono: TonoInsignia }> = {
  ACTIVA: { texto: 'Activa', tono: 'exito' },
  PAUSADA: { texto: 'Pausada', tono: 'aviso' },
  PENDIENTE_REVISION: { texto: 'Pendiente de revisión', tono: 'info' },
  BORRADOR: { texto: 'Borrador', tono: 'neutro' },
  BLOQUEADA: { texto: 'Bloqueada', tono: 'error' }
};