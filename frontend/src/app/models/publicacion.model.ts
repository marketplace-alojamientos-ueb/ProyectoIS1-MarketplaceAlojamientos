export type TipoAlojamiento = 'APARTAMENTO' | 'CASA' | 'CABANA' | 'FINCA' | 'HABITACION';

export interface Publicacion {
  idPublicacion: number;
  titulo: string;
  descripcion: string;
  ciudad: string;
  tipo: TipoAlojamiento;
  precioNoche: number;
  capacidad: number;
  habitaciones: number;
  banos: number;
  calificacion: number;
  servicios: string[];
  nombreAnfitrion: string;
  imagen: string;
}