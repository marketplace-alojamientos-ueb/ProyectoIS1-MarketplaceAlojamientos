import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginRequest } from '../models/login.model';
import { MensajeResponse } from '../models/mensaje.model';
import {
  RecuperarContrasenaRequest,
  RestablecerContrasenaRequest
} from '../models/recuperacion-contrasena.model';

/** Llamadas a UsuarioController (/api/usuarios): lo común a anfitriones y huéspedes. */
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/usuarios';

  solicitarRecuperacion(datos: RecuperarContrasenaRequest): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${this.url}/recuperar-contrasena`, datos);
  }

  restablecerContrasena(datos: RestablecerContrasenaRequest): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${this.url}/restablecer-contrasena`, datos);
  }

  /** Reactiva una cuenta desactivada confirmando el correo y la contraseña. */
  activarCuenta(datos: LoginRequest): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${this.url}/activar-cuenta`, datos);
  }
}
