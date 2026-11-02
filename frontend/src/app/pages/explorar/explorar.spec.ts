import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Explorar } from './explorar';

describe('Explorar', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ imports: [Explorar], providers: [provideRouter([])] });
  });

  function buscar(fixture: ReturnType<typeof TestBed.createComponent<Explorar>>, texto: string): void {
    const input: HTMLInputElement = fixture.nativeElement.querySelector('input');
    input.value = texto;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  it('muestra todas las publicaciones al entrar', () => {
    const fixture = TestBed.createComponent(Explorar);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('app-tarjeta-publicacion').length).toBe(5);
  });

  it('filtra por ciudad sin importar tildes ni mayúsculas', () => {
    const fixture = TestBed.createComponent(Explorar);
    fixture.detectChanges();

    buscar(fixture, 'BOGOTA');

    const tarjetas = fixture.nativeElement.querySelectorAll('app-tarjeta-publicacion');
    expect(tarjetas.length).toBe(1);
    expect(tarjetas[0].textContent).toContain('Bogotá');
  });

  it('avisa cuando no hay resultados', () => {
    const fixture = TestBed.createComponent(Explorar);
    fixture.detectChanges();

    buscar(fixture, 'Marte');

    expect(fixture.nativeElement.querySelector('app-tarjeta-publicacion')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('No encontramos alojamientos');
  });
});