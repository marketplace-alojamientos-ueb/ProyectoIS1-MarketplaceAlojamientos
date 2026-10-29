/** Igual a los DTO de anfitrión del back (rama MA-111). */

export type EstadoCuenta = 'ACTIVA' | 'DESACTIVADA';

/** POST /api/anfitriones */
export interface AnfitrionRequest {
  cedula: string;
  primerNombre: string;
  segundoNombre: string | null;
  primerApellido: string;
  segundoApellido: string;
  correo: string;
  password: string;
}

/** PUT /api/anfitriones/{idUsuario} */
export interface AnfitrionActualizar {
  primerNombre: string;
  segundoNombre: string | null;
  primerApellido: string;
  segundoApellido: string;
  correo: string;
}

/** Lo que devuelve el back de un anfitrión. */
export interface AnfitrionResponse {
  idUsuario: number;
  cedula: string;
  primerNombre: string;
  segundoNombre: string | null;
  primerApellido: string;
  segundoApellido: string;
  nombreCompleto: string;
  correo: string;
  estadoCuenta: EstadoCuenta;
  restringido: boolean;
}
