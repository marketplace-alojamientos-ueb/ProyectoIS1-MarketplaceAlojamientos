import { Component, input } from '@angular/core';

export type TonoInsignia = 'exito' | 'aviso' | 'info' | 'error' | 'neutro';


@Component({
  selector: 'app-insignia',
  templateUrl: './insignia.html',
  styleUrl: './insignia.css'
})
export class Insignia {
  readonly tono = input<TonoInsignia>('neutro');
}