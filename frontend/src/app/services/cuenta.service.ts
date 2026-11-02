import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/login.model';
import { DatosPersonales, RegistroUsuarioRequest, UsuarioResponse } from '../models/usuario.model';

/**
 * Endpoints que anfitriones y huéspedes tienen iguales en el back; solo cambia la URL base.
 * AnfitrionService y HuespedService heredan de aquí.
 */
export abstract class CuentaService {
  private readonly http = inject(HttpClient);

  protected constructor(private readonly url: string) {}

  listarTodos(): Observable<UsuarioResponse[]> {
    return this.http.get<UsuarioResponse[]>(this.url);
  }

  buscarPorId(idUsuario: number): Observable<UsuarioResponse> {
    return this.http.get<UsuarioResponse>(`${this.url}/${idUsuario}`);
  }

  registrar(datos: RegistroUsuarioRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(this.url, datos);
  }

  actualizar(idUsuario: number, datos: DatosPersonales): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(`${this.url}/${idUsuario}`, datos);
  }

  desactivar(idUsuario: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${idUsuario}`);
  }

  iniciarSesion(datos: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.url}/login`, datos);
  }
}
