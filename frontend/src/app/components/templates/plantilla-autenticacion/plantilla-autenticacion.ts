import { Component, input } from '@angular/core';
import { Logo } from '../../atoms/logo/logo';

/** Estructura común de las pantallas de login, registro y recuperación. */
@Component({
  selector: 'app-plantilla-autenticacion',
  imports: [Logo],
  templateUrl: './plantilla-autenticacion.html',
  styleUrl: './plantilla-autenticacion.css'
})
export class PlantillaAutenticacion {
  readonly titulo = input.required<string>();
  readonly subtitulo = input('');
  /** Tarjeta más ancha para formularios largos (registro). */
  readonly amplia = input(false);
}
