import { Component, input } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Entrada } from '../../atoms/entrada/entrada';
import { Etiqueta } from '../../atoms/etiqueta/etiqueta';

/** Etiqueta + entrada + texto de error o de ayuda. */
@Component({
  selector: 'app-campo-formulario',
  imports: [Etiqueta, Entrada],
  templateUrl: './campo-formulario.html',
  styleUrl: './campo-formulario.css'
})
export class CampoFormulario {
  readonly id = input.required<string>();
  readonly etiqueta = input.required<string>();
  readonly control = input.required<FormControl<string>>();
  readonly tipo = input<'text' | 'email' | 'password'>('text');
  readonly placeholder = input('');
  readonly autocomplete = input('off');
  readonly modoTeclado = input<'text' | 'numeric' | 'email'>('text');
  readonly opcional = input(false);
  /** Mensaje de error ya calculado; vacío si el campo está bien. */
  readonly error = input('');
  readonly ayuda = input('');
}
