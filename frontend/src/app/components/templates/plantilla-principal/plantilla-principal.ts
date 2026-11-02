import { Component, input } from '@angular/core';
import { BarraNavegacion } from '../../organisms/barra-navegacion/barra-navegacion';

/** Estructura común de las pantallas después de iniciar sesión: barra arriba y contenido centrado. */
@Component({
  selector: 'app-plantilla-principal',
  imports: [BarraNavegacion],
  templateUrl: './plantilla-principal.html',
  styleUrl: './plantilla-principal.css'
})
export class PlantillaPrincipal {
  readonly titulo = input('');
  readonly subtitulo = input('');
}