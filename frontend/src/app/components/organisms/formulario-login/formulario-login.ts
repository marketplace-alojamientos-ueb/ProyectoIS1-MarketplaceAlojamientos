import { Component, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LoginRequest } from '../../../models/login.model';
import { mensajeError, validadoresCorreo } from '../../../utils/validaciones';
import { Boton } from '../../atoms/boton/boton';
import { CampoFormulario } from '../../molecules/campo-formulario/campo-formulario';

@Component({
  selector: 'app-formulario-login',
  imports: [ReactiveFormsModule, RouterLink, CampoFormulario, Boton],
  templateUrl: './formulario-login.html',
  styleUrl: './formulario-login.css'
})
export class FormularioLogin {
  readonly cargando = input(false);
  readonly enviar = output<LoginRequest>();

  protected readonly form = new FormGroup({
    correo: new FormControl('', { nonNullable: true, validators: validadoresCorreo }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(64)]
    })
  });

  protected readonly error = mensajeError;

  protected enviarFormulario(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviar.emit(this.form.getRawValue());
  }
}
