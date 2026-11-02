import { Injectable, signal } from '@angular/core';
import { LoginResponse } from '../models/login.model';

const CLAVE = 'sesion';

/**
 * Guarda en sessionStorage quién inició sesión (la respuesta del login).
 * Provisional: el back aún no entrega token (JWT); cuando lo haga se guardará aquí también.
 */
@Injectable({ providedIn: 'root' })
export class SesionService {
  readonly usuario = signal<LoginResponse | null>(leer());

  iniciar(usuario: LoginResponse): void {
    try {
      sessionStorage.setItem(CLAVE, JSON.stringify(usuario));
    } catch {
      // Sin sessionStorage la sesión solo dura hasta recargar la página.
    }
    this.usuario.set(usuario);
  }

  cerrar(): void {
    try {
      sessionStorage.removeItem(CLAVE);
    } catch {
      // Nada que borrar.
    }
    this.usuario.set(null);
  }
}

function leer(): LoginResponse | null {
  try {
    const guardado = sessionStorage.getItem(CLAVE);
    return guardado ? (JSON.parse(guardado) as LoginResponse) : null;
  } catch {
    return null;
  }
}
