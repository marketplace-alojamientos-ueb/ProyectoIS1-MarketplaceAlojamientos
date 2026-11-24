import { HttpErrorResponse } from '@angular/common/http';
import { mensajeDelBack } from './errores';

function errorHttp(status: number, error: unknown): HttpErrorResponse {
  return new HttpErrorResponse({ status, error });
}

describe('mensajeDelBack', () => {
  it('avisa cuando no hay conexión con el servidor', () => {
    expect(mensajeDelBack(errorHttp(0, null))).toContain('No se pudo conectar');
  });

  it('usa el defaultMessage del primer error de validación', () => {
    const error = errorHttp(400, { message: 'Validation failed', errors: [{ defaultMessage: 'El correo es obligatorio' }] });
    expect(mensajeDelBack(error)).toBe('El correo es obligatorio');
  });

  it('usa el message del primer error de validación si no hay defaultMessage', () => {
    const error = errorHttp(400, { errors: [{ message: 'La cédula es obligatoria' }] });
    expect(mensajeDelBack(error)).toBe('La cédula es obligatoria');
  });

  it('usa el message del back', () => {
    expect(mensajeDelBack(errorHttp(401, { message: 'Correo o contraseña incorrectos' }))).toBe(
      'Correo o contraseña incorrectos'
    );
  });

  it('da un mensaje genérico en errores 500 o sin cuerpo', () => {
    expect(mensajeDelBack(errorHttp(500, { message: 'NullPointerException' }))).toContain('error inesperado');
    expect(mensajeDelBack(errorHttp(404, null))).toContain('error inesperado');
  });

  it('en 502 y 503 muestra el mensaje del back (errores de Stripe o falta la llave)', () => {
    expect(mensajeDelBack(errorHttp(503, { message: 'El pago con Stripe no está configurado' }))).toBe(
      'El pago con Stripe no está configurado'
    );
    expect(mensajeDelBack(errorHttp(502, { message: 'Stripe rechazó la solicitud' }))).toBe('Stripe rechazó la solicitud');
    expect(mensajeDelBack(errorHttp(502, null))).toContain('error inesperado');
  });
});
