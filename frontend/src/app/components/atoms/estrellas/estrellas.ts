import { Component, computed, input } from '@angular/core';

/** Calificación de 1 a 5 en estrellas (solo lectura). Los lectores de pantalla leen "4 de 5 estrellas". */
@Component({
  selector: 'app-estrellas',
  template: `<span class="estrellas" role="img" [attr.aria-label]="valor() + ' de 5 estrellas'">{{ texto() }}</span>`,
  styles: `.estrellas { color: #d97706; letter-spacing: 0.1em; white-space: nowrap; }`
})
export class Estrellas {
  readonly valor = input.required<number>();

  protected readonly texto = computed(() => {
    const llenas = Math.max(0, Math.min(5, Math.round(this.valor())));
    return '★'.repeat(llenas) + '☆'.repeat(5 - llenas);
  });
}
