import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { FiltrosBusqueda, PublicacionBusqueda, PublicacionDetalle } from '../models/publicacion.model';

const URL = '/api/publicaciones';

/** Búsqueda y detalle de alojamientos para el huésped (PublicacionController, MA-99 / MA-100). */
@Injectable({ providedIn: 'root' })
export class PublicacionService {
  private readonly http = inject(HttpClient);

  /** Últimos filtros usados: el back pide guardarlos para volver del detalle a los mismos resultados. */
  ultimosFiltros: FiltrosBusqueda = {};

  /** MA-99: solo envía los filtros con valor; cada servicio va como un parámetro "servicios" aparte. */
  buscar(filtros: FiltrosBusqueda): Observable<PublicacionBusqueda[]> {
    let params = new HttpParams();
    for (const [clave, valor] of Object.entries(filtros)) {
      const valores: unknown[] = Array.isArray(valor) ? valor : [valor];
      for (const v of valores) {
        if (v !== undefined && v !== null && v !== '') {
          params = params.append(clave, String(v));
        }
      }
    }
    return this.http.get<PublicacionBusqueda[]>(`${URL}/buscar`, { params });
  }

  /** MA-100: responde 404 si el alojamiento no existe o ya no está ACTIVO. */
  detalle(idPublicacion: number): Observable<PublicacionDetalle> {
    return this.http.get<PublicacionDetalle>(`${URL}/${idPublicacion}/detalle`);
  }
}