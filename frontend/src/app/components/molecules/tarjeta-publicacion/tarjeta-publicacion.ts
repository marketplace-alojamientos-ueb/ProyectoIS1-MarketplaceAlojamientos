import { CurrencyPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Publicacion } from '../../../models/publicacion.model';


@Component({
  selector: 'app-tarjeta-publicacion',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './tarjeta-publicacion.html',
  styleUrl: './tarjeta-publicacion.css'
})
export class TarjetaPublicacion {
  readonly publicacion = input.required<Publicacion>();
}