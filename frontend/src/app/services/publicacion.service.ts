import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Publicacion } from '../models/publicacion.model';
/**
 * Publicaciones que ve el huésped.
 * Provisional: devuelve datos de ejemplo; cuando exista el back se cambia por llamadas HTTP
 * con Observable
 */
@Injectable({ providedIn: 'root' })
export class PublicacionService {
  listar(): Observable<Publicacion[]> {
    return of(PUBLICACIONES_EJEMPLO);
  }

  obtener(idPublicacion: number): Observable<Publicacion | undefined> {
    return of(PUBLICACIONES_EJEMPLO.find((p) => p.idPublicacion === idPublicacion));
  }
}

const PUBLICACIONES_EJEMPLO: Publicacion[] = [
  {
    idPublicacion: 1,
    titulo: 'Apartamento frente al mar',
    descripcion: 'Apartamento con balcón y vista al mar, a cinco minutos de la ciudad amurallada.',
    ciudad: 'Cartagena',
    tipo: 'APARTAMENTO',
    precioNoche: 250000,
    capacidad: 4,
    habitaciones: 2,
    banos: 2,
    calificacion: 4.8,
    servicios: ['Wifi', 'Aire acondicionado', 'Piscina', 'Cocina'],
    nombreAnfitrion: 'Laura Gómez',
    imagen:'/alojamientos/cartagena.jpg'
  },
  {
    idPublicacion: 2,
    titulo: 'Cabaña en la montaña',
    descripcion: 'Cabaña rodeada de naturaleza, con chimenea y zona de fogata.',
    ciudad: 'Guatavita',
    tipo: 'CABANA',
    precioNoche: 180000,
    capacidad: 6,
    habitaciones: 3,
    banos: 1,
    calificacion: 4.6,
    servicios: ['Chimenea', 'Parqueadero', 'Cocina'],
    nombreAnfitrion: 'Carlos Ruiz',
    imagen: '/alojamientos/guatavita.jpg'
  },
  {
    idPublicacion: 3,
    titulo: 'Habitación cerca a Chapinero',
    descripcion: 'Habitación privada con escritorio, cerca de restaurantes y transporte público.',
    ciudad: 'Bogotá',
    tipo: 'HABITACION',
    precioNoche: 90000,
    capacidad: 1,
    habitaciones: 1,
    banos: 1,
    calificacion: 4.4,
    servicios: ['Wifi', 'Escritorio', 'Lavadora'],
    nombreAnfitrion: 'Andrea Torres',
    imagen: '/alojamientos/bogota.avif'
  },
  {
    idPublicacion: 4,
    titulo: 'Finca llanera con piscina',
    descripcion: 'Finca ideal para grupos, con piscina, kiosco y zona BBQ.',
    ciudad: 'Villavicencio',
    tipo: 'FINCA',
    precioNoche: 400000,
    capacidad: 10,
    habitaciones: 5,
    banos: 3,
    calificacion: 4.9,
    servicios: ['Piscina', 'BBQ', 'Parqueadero', 'Wifi'],
    nombreAnfitrion: 'Jorge Rey',
    imagen: '/alojamientos/villavicencio.avif'
  },
  {
    idPublicacion: 5,
    titulo: 'Casa colonial en el centro',
    descripcion: 'Casa tradicional a dos cuadras de la plaza principal.',
    ciudad: 'Villa de Leyva',
    tipo: 'CASA',
    precioNoche: 220000,
    capacidad: 5,
    habitaciones: 3,
    banos: 2,
    calificacion: 4.7,
    servicios: ['Wifi', 'Patio', 'Cocina'],
    nombreAnfitrion: 'María Pardo',
    imagen: '/alojamientos/villaDeLeyva.jpg'
  }
];