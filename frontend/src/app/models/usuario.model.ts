/** Igual a los DTO de anfitrión y huésped del back: los dos tipos de usuario usan los mismos campos. */

export type Rol = 'ANFITRION' | 'HUESPED';

export type EstadoCuenta = 'ACTIVA' | 'DESACTIVADA';

/** POST /api/anfitriones y POST /api/huespedes */
export interface RegistroUsuarioRequest {
  cedula: string;
  primerNombre: string;
  segundoNombre: string | null;
  primerApellido: string;
  segundoApellido: string;
  correo: string;
  password: string;
}

/** PUT /api/anfitriones/{idUsuario} y PUT /api/huespedes/{idUsuario} */
export interface DatosPersonales {
  primerNombre: string;
  segundoNombre: string | null;
  primerApellido: string;
  segundoApellido: string;
  correo: string;
}

/** Lo que devuelve el back de un anfitrión o de un huésped. */
export interface UsuarioResponse {
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
