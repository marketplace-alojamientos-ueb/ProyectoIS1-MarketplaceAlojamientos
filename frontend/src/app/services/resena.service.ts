import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MensajeResponse } from '../models/mensaje.model';
import { Resena, ResenaActualizar, ResenaReportar, ResenaRequest } from '../models/resena.model';

const URL = '/api/resenas';

/** Reseñas (ResenaController): el huésped califica estancias completadas (MA-81) y cualquiera reporta una reseña (MA-82). */
@Injectable({ providedIn: 'root' })
export class ResenaService {
  private readonly http = inject(HttpClient);

  /** Reseñas visibles de un alojamiento, de la más reciente a la más antigua; [] si no tiene. */
  porPublicacion(idPublicacion: number): Observable<Resena[]> {
    return this.http.get<Resena[]>(`${URL}/publicacion/${idPublicacion}`);
  }

  /** "Mis reseñas": las que escribió el huésped, incluidas las ocultas (visible: false). 403 si no es huésped activo. */
  porHuesped(idHuesped: number): Observable<Resena[]> {
    return this.http.get<Resena[]>(`${URL}/huesped/${idHuesped}`);
  }

  /** MA-81: 201 con la reseña. 409 si la estancia no se completó o la reserva ya tiene reseña. */
  crear(datos: ResenaRequest): Observable<Resena> {
    return this.http.post<Resena>(URL, datos);
  }

  /** El autor edita su reseña. 403 si no es el autor, 409 si fue ocultada por moderación. */
  actualizar(idResena: number, datos: ResenaActualizar): Observable<Resena> {
    return this.http.put<Resena>(`${URL}/${idResena}`, datos);
  }

  /** El autor elimina su reseña. 403 si no es el autor, 409 si fue ocultada por moderación. */
  eliminar(idResena: number, idHuesped: number): Observable<MensajeResponse> {
    const params = new HttpParams().set('idHuesped', idHuesped);
    return this.http.delete<MensajeResponse>(`${URL}/${idResena}`, { params });
  }

  /** MA-82: reportar para que un administrador la revise. 409 si es el autor, ya la reportó o está oculta. */
  reportar(idResena: number, datos: ResenaReportar): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${URL}/${idResena}/reportar`, datos);
  }
}
