import { AbstractControl, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

/** Las mismas reglas que validan los DTO del back. */
export const PATRON_CEDULA = /^\d{6,10}$/;
export const PATRON_PASSWORD = /^(?=.*[A-Za-z])(?=.*\d).+$/;
export const PATRON_CODIGO = /^\d{6}$/;

export const validadoresCorreo = [Validators.required, Validators.email, Validators.maxLength(200)];
export const validadoresPassword = [
  Validators.required,
  Validators.minLength(8),
  Validators.maxLength(64),
  Validators.pattern(PATRON_PASSWORD)
];

/** Valida en el grupo que dos campos tengan el mismo valor (ej. contraseña y confirmación). */
export function camposIguales(campo: string, confirmacion: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null =>
    grupo.get(campo)?.value === grupo.get(confirmacion)?.value ? null : { noCoinciden: true };
}

/** Texto del primer error de un campo, solo si el usuario ya lo tocó. */
export function mensajeError(control: AbstractControl, mensajePatron = 'El formato no es válido'): string {
  if (!control.touched || !control.errors) {
    return '';
  }
  const errores = control.errors;
  if (errores['required']) return 'Este campo es obligatorio';
  if (errores['email']) return 'El correo no tiene un formato válido';
  if (errores['minlength']) return `Debe tener mínimo ${errores['minlength'].requiredLength} caracteres`;
  if (errores['maxlength']) return `Admite máximo ${errores['maxlength'].requiredLength} caracteres`;
  if (errores['pattern']) return mensajePatron;
  return '';
}
