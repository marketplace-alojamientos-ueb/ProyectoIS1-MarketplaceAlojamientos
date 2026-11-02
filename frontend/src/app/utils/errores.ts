import { HttpErrorResponse } from '@angular/common/http';

/**
 * Texto para mostrar de un error del back. Spring lo manda en "message";
 * en los 400 de validación el texto útil viene en "errors[0]" (defaultMessage o message).
 */
export function mensajeDelBack(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return 'No se pudo conectar con el servidor. Intenta de nuevo en unos minutos.';
  }
  const cuerpo = error.error;
  const primerError = cuerpo?.errors?.[0];
  const validacion = primerError?.defaultMessage ?? primerError?.message;
  if (validacion) {
    return validacion;
  }
  if (error.status >= 500 || !cuerpo?.message) {
    return 'Ocurrió un error inesperado. Intenta de nuevo.';
  }
  return cuerpo.message;
}
