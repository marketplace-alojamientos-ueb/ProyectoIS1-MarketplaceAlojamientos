import { Component, input } from '@angular/core';

@Component({
  selector: 'app-boton',
  templateUrl: './boton.html',
  styleUrl: './boton.css',
  host: { '[class.completo]': 'completo()' }
})
export class Boton {
  readonly tipo = input<'button' | 'submit'>('button');
  readonly variante = input<'primario' | 'secundario'>('primario');
  readonly deshabilitado = input(false);
  /** Ocupa todo el ancho del contenedor. */
  readonly completo = input(false);
}
