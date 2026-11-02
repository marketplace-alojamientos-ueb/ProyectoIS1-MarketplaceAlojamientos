/** POST /api/usuarios/recuperar-contrasena (paso 1) */
export interface RecuperarContrasenaRequest {
  correo: string;
}

/** POST /api/usuarios/restablecer-contrasena (paso 2) */
export interface RestablecerContrasenaRequest {
  correo: string;
  codigo: string;
  nuevaPassword: string;
}
