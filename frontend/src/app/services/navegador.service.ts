import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/**
 * Salir de la app a una URL externa (la página de pago de Stripe). Stripe exige una redirección completa, no
 * router.navigate ni un iframe. Va en un servicio para poder simularlo en las pruebas.
 */
@Injectable({ providedIn: 'root' })
export class NavegadorService {
  private readonly documento = inject(DOCUMENT);

  irA(url: string): void {
    this.documento.location.href = url;
  }
}
