import { Rol } from './usuario.model';

/** POST /api/anfitriones/login, POST /api/huespedes/login y POST /api/usuarios/activar-cuenta */
export interface LoginRequest {
  correo: string;
  password: string;
}

export interface LoginResponse {
  idUsuario: number;
  nombreCompleto: string;
  correo: string;
  rol: Rol;
}
