import { Component, input } from '@angular/core';

@Component({
  selector: 'app-etiqueta',
  templateUrl: './etiqueta.html',
  styleUrl: './etiqueta.css'
})
export class Etiqueta {
  readonly para = input.required<string>();
  readonly opcional = input(false);
}
