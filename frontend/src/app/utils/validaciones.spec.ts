import { FormControl, FormGroup, Validators } from '@angular/forms';
import { camposIguales, mensajeConfirmacion, mensajeError, validadoresPassword } from './validaciones';

function tocado(valor: string, validadores = validadoresPassword): FormControl<string> {
  const control = new FormControl(valor, { nonNullable: true, validators: validadores });
  control.markAsTouched();
  return control;
}

describe('mensajeError', () => {
  it('no muestra nada si el campo no se ha tocado', () => {
    expect(mensajeError(new FormControl('', { validators: Validators.required }))).toBe('');
  });

  it('describe cada tipo de error', () => {
    expect(mensajeError(tocado(''))).toBe('Este campo es obligatorio');
    expect(mensajeError(tocado('abc', [Validators.email]))).toBe('El correo no tiene un formato válido');
    expect(mensajeError(tocado('a1'))).toBe('Debe tener mínimo 8 caracteres');
    expect(mensajeError(tocado('a1'.repeat(40)))).toBe('Admite máximo 64 caracteres');
    expect(mensajeError(tocado('solo-letras'), 'Falta un número')).toBe('Falta un número');
    expect(mensajeError(tocado('Clave1234'))).toBe('');
  });
});

describe('mensajeConfirmacion', () => {
  function grupo(password: string, confirmar: string): FormGroup {
    const form = new FormGroup(
      {
        password: new FormControl(password),
        confirmar: new FormControl(confirmar, Validators.required)
      },
      { validators: camposIguales('password', 'confirmar') }
    );
    form.controls.confirmar.markAsTouched();
    return form;
  }

  it('muestra el error propio del campo', () => {
    expect(mensajeConfirmacion(grupo('Clave1234', ''), 'confirmar')).toBe('Este campo es obligatorio');
  });

  it('avisa si las contraseñas no coinciden', () => {
    expect(mensajeConfirmacion(grupo('Clave1234', 'Otra1234'), 'confirmar')).toBe('Las contraseñas no coinciden');
  });

  it('no muestra nada si coinciden', () => {
    expect(mensajeConfirmacion(grupo('Clave1234', 'Clave1234'), 'confirmar')).toBe('');
  });
});
