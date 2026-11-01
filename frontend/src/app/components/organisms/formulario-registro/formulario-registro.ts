import { Component, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { RegistroUsuarioRequest } from '../../../models/usuario.model';
import {
  camposIguales,
  mensajeConfirmacion,
  mensajeError,
  PATRON_CEDULA,
  validadoresCorreo,
  validadoresPassword
} from '../../../utils/validaciones';
import { Boton } from '../../atoms/boton/boton';
import { CampoFormulario } from '../../molecules/campo-formulario/campo-formulario';

@Component({
  selector: 'app-formulario-registro',
  imports: [ReactiveFormsModule, RouterLink, CampoFormulario, Boton],
  templateUrl: './formulario-registro.html',
  styleUrl: './formulario-registro.css'
})
export class FormularioRegistro {
  readonly cargando = input(false);
  readonly enviar = output<RegistroUsuarioRequest>();

  protected readonly form = new FormGroup(
    {
      cedula: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(PATRON_CEDULA)]
      }),
      primerNombre: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.maxLength(50)]
      }),
      segundoNombre: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(50)] }),
      primerApellido: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.maxLength(50)]
      }),
      segundoApellido: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.maxLength(50)]
      }),
      correo: new FormControl('', { nonNullable: true, validators: validadoresCorreo }),
      password: new FormControl('', { nonNullable: true, validators: validadoresPassword }),
      confirmarPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] })
    },
    { validators: camposIguales('password', 'confirmarPassword') }
  );

  protected readonly error = mensajeError;

  protected errorConfirmacion(): string {
    return mensajeConfirmacion(this.form, 'confirmarPassword');
  }

  protected enviarFormulario(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    // La confirmación solo se valida en el front, no se envía al back.
    const { confirmarPassword, ...datos } = this.form.getRawValue();
    this.enviar.emit({ ...datos, segundoNombre: datos.segundoNombre.trim() || null });
  }
}
