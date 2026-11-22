import { Component, input } from '@angular/core';
import { Rol } from '../../../models/usuario.model';
import { BarraNavegacion } from '../../organisms/barra-navegacion/barra-navegacion';


@Component({
  selector: 'app-plantilla-principal',
  imports: [BarraNavegacion],
  templateUrl: './plantilla-principal.html',
  styleUrl: './plantilla-principal.css'
})
export class PlantillaPrincipal {
  readonly titulo = input('');
  readonly subtitulo = input('');
  /** Define el menú de la barra. */
  readonly rol = input<Rol>('HUESPED');
}