import { Component, input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-entrada',
  imports: [ReactiveFormsModule],
  templateUrl: './entrada.html',
  styleUrl: './entrada.css'
})
export class Entrada {
  readonly control = input.required<FormControl<string>>();
  readonly id = input.required<string>();
  readonly tipo = input<'text' | 'email' | 'password'>('text');
  readonly placeholder = input('');
  readonly autocomplete = input('off');
  readonly modoTeclado = input<'text' | 'numeric' | 'email'>('text');
  readonly invalida = input(false);
  /** Texto de la etiqueta; se repite como aria-label para que el input tenga nombre accesible por sí mismo (Sonar). */
  readonly etiqueta = input('');
}
