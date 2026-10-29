import { Component, input, OnInit, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { RecuperarContrasenaRequest } from '../../../models/recuperacion-contrasena.model';
import { mensajeError, validadoresCorreo } from '../../../utils/validaciones';
import { Boton } from '../../atoms/boton/boton';
import { CampoFormulario } from '../../molecules/campo-formulario/campo-formulario';

/** Paso 1 de la recuperación: el usuario escribe su correo para recibir el código. */
@Component({
  selector: 'app-formulario-solicitar-codigo',
  imports: [ReactiveFormsModule, RouterLink, CampoFormulario, Boton],
  templateUrl: './formulario-solicitar-codigo.html',
  styleUrl: './formulario-solicitar-codigo.css'
})
export class FormularioSolicitarCodigo implements OnInit {
  readonly cargando = input(false);
  /** Correo con el que se vuelve a llenar el campo al regresar del paso 2. */
  readonly correoInicial = input('');
  readonly enviar = output<RecuperarContrasenaRequest>();

  protected readonly form = new FormGroup({
    correo: new FormControl('', { nonNullable: true, validators: validadoresCorreo })
  });

  protected readonly error = mensajeError;

  ngOnInit(): void {
    this.form.controls.correo.setValue(this.correoInicial());
  }

  protected enviarFormulario(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviar.emit(this.form.getRawValue());
  }
}
