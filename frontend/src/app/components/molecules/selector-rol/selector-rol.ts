import { Component, input, model } from '@angular/core';
import { Rol } from '../../../models/usuario.model';

/** Elige si el usuario entra o se registra como anfitrión o como huésped. */
@Component({
  selector: 'app-selector-rol',
  templateUrl: './selector-rol.html',
  styleUrl: './selector-rol.css'
})
export class SelectorRol {
  readonly id = input.required<string>();
  readonly rol = model<Rol>('ANFITRION');

  protected readonly opciones: { valor: Rol; texto: string }[] = [
    { valor: 'ANFITRION', texto: 'Anfitrión' },
    { valor: 'HUESPED', texto: 'Huésped' }
  ];
}
