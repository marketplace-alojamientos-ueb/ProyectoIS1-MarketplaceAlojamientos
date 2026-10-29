/** POST /api/anfitriones/login */
export interface LoginRequest {
  correo: string;
  password: string;
}

export interface LoginResponse {
  idUsuario: number;
  nombreCompleto: string;
  correo: string;
  rol: string;
}
