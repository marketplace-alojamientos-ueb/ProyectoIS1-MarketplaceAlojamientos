import { CurrencyPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PublicacionBusqueda } from '../../../models/publicacion.model';
import { textoTipo } from '../../../utils/etiquetas';

/** Resumen de un resultado de búsqueda; lleva al detalle al hacer clic. */
@Component({
  selector: 'app-tarjeta-publicacion',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './tarjeta-publicacion.html',
  styleUrl: './tarjeta-publicacion.css'
})
export class TarjetaPublicacion {
  readonly publicacion = input.required<PublicacionBusqueda>();
  protected readonly textoTipo = textoTipo;
}