import { Component, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  camposIguales,
  mensajeError,
  PATRON_CODIGO,
  validadoresPassword
} from '../../../utils/validaciones';
import { Boton } from '../../atoms/boton/boton';
import { CampoFormulario } from '../../molecules/campo-formulario/campo-formulario';

/** Paso 2 de la recuperación: código que llegó al correo + contraseña nueva. */
@Component({
  selector: 'app-formulario-restablecer',
  imports: [ReactiveFormsModule, CampoFormulario, Boton],
  templateUrl: './formulario-restablecer.html',
  styleUrl: './formulario-restablecer.css'
})
export class FormularioRestablecer {
  readonly correo = input.required<string>();
  readonly cargando = input(false);
  readonly enviar = output<{ codigo: string; nuevaPassword: string }>();
  readonly cambiarCorreo = output<void>();

  protected readonly form = new FormGroup(
    {
      codigo: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(PATRON_CODIGO)]
      }),
      nuevaPassword: new FormControl('', { nonNullable: true, validators: validadoresPassword }),
      confirmarPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] })
    },
    { validators: camposIguales('nuevaPassword', 'confirmarPassword') }
  );

  protected readonly error = mensajeError;

  protected errorConfirmacion(): string {
    const control = this.form.controls.confirmarPassword;
    const mensaje = mensajeError(control);
    if (mensaje) {
      return mensaje;
    }
    return control.touched && this.form.hasError('noCoinciden') ? 'Las contraseñas no coinciden' : '';
  }

  protected enviarFormulario(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { codigo, nuevaPassword } = this.form.getRawValue();
    this.enviar.emit({ codigo, nuevaPassword });
  }
}
