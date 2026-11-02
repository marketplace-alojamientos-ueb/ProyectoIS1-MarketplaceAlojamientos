import { Component, input } from '@angular/core';

@Component({
  selector: 'app-logo',
  templateUrl: './logo.html',
  styleUrl: './logo.css'
})
export class Logo {
  /** Versión blanca para fondos oscuros. */
  readonly claro = input(false);
}
