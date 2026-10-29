import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AnfitrionActualizar, AnfitrionRequest, AnfitrionResponse } from '../models/anfitrion.model';
import { LoginRequest, LoginResponse } from '../models/login.model';

/** Llamadas a AnfitrionController (/api/anfitriones). Aún no se usan: las páginas son prototipos. */
@Injectable({ providedIn: 'root' })
export class AnfitrionService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/anfitriones';

  listarTodos(): Observable<AnfitrionResponse[]> {
    return this.http.get<AnfitrionResponse[]>(this.url);
  }

  buscarPorId(idUsuario: number): Observable<AnfitrionResponse> {
    return this.http.get<AnfitrionResponse>(`${this.url}/${idUsuario}`);
  }

  registrar(datos: AnfitrionRequest): Observable<AnfitrionResponse> {
    return this.http.post<AnfitrionResponse>(this.url, datos);
  }

  actualizar(idUsuario: number, datos: AnfitrionActualizar): Observable<AnfitrionResponse> {
    return this.http.put<AnfitrionResponse>(`${this.url}/${idUsuario}`, datos);
  }

  desactivar(idUsuario: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${idUsuario}`);
  }

  iniciarSesion(datos: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.url}/login`, datos);
  }
}
