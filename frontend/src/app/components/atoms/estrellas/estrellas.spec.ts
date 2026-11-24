import { TestBed } from '@angular/core/testing';
import { Estrellas } from './estrellas';

function pintar(valor: number): HTMLElement {
  TestBed.configureTestingModule({ imports: [Estrellas] });
  const fixture = TestBed.createComponent(Estrellas);
  fixture.componentRef.setInput('valor', valor);
  fixture.detectChanges();
  return fixture.nativeElement.querySelector('.estrellas');
}

describe('Estrellas', () => {
  it('pinta las estrellas llenas y vacías con su texto accesible', () => {
    const el = pintar(4);
    expect(el.textContent).toBe('★★★★☆');
    expect(el.getAttribute('aria-label')).toBe('4 de 5 estrellas');
  });

  it('redondea promedios', () => {
    expect(pintar(3.6).textContent).toBe('★★★★☆');
  });
});
