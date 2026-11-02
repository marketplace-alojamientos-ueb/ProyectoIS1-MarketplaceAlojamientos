import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { DetallePublicacion } from './detalle-publicacion';

function crear(id: string) {
  TestBed.configureTestingModule({
    imports: [DetallePublicacion],
    providers: [
      provideRouter([]),
      { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id }) } } }
    ]
  });
  const fixture = TestBed.createComponent(DetallePublicacion);
  fixture.detectChanges();
  return fixture;
}

describe('DetallePublicacion', () => {
  beforeEach(() => sessionStorage.clear());

  it('muestra la publicación y el aviso al reservar', () => {
    const fixture = crear('1');
    const texto = () => fixture.nativeElement.textContent as string;

    expect(texto()).toContain('Apartamento frente al mar');
    expect(texto()).toContain('Laura Gómez');

    fixture.componentInstance['reservar']();
    fixture.detectChanges();
    expect(texto()).toContain('La reserva aún no está disponible');
  });

  it('avisa si la publicación no existe', () => {
    const fixture = crear('999');
    expect(fixture.nativeElement.textContent).toContain('No encontramos este alojamiento');
  });
});