import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { DetallePublicacion } from './detalle-publicacion';

const DETALLE = {
  idPublicacion: 1,
  titulo: 'Apartamento frente al mar',
  descripcion: 'Vista al mar',
  ciudad: 'Cartagena',
  ubicacion: 'Bocagrande',
  tipoAlojamiento: 'APARTAMENTO',
  capacidad: 4,
  precioPorNoche: 250000,
  tarifaLimpieza: 30000,
  reglas: 'No fiestas',
  modoAceptacion: 'INMEDIATA',
  politicaCancelacion: 'FLEXIBLE',
  servicios: ['WiFi'],
  imagenes: ['https://imagenes.test/1.jpg']
};

function crear() {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [DetallePublicacion],
    providers: [
      provideRouter([]),
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } } }
    ]
  });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(DetallePublicacion);
  fixture.detectChanges();
  http
    .expectOne('/api/publicaciones/politicas-cancelacion')
    .flush([{ codigo: 'FLEXIBLE', nombre: 'Flexible', descripcion: 'Devolución total hasta 24 horas antes' }]);
  const req = http.expectOne('/api/publicaciones/1/detalle');
  return { fixture, req, texto: () => fixture.nativeElement.textContent as string };
}

describe('DetallePublicacion', () => {
  it('MA-100: muestra el alojamiento con sus imágenes, servicios, tarifas y política', () => {
    const { fixture, req, texto } = crear();
    req.flush(DETALLE);
    fixture.detectChanges();
    expect(texto()).toContain('Apartamento frente al mar');
    expect(texto()).toContain('Apartamento en Cartagena');
    expect(texto()).toContain('Reserva inmediata');
    expect(texto()).toContain('WiFi');
    expect(texto()).toContain('Flexible');
    expect(texto()).toContain('Devolución total hasta 24 horas antes');
    expect(fixture.nativeElement.querySelector('img').getAttribute('src')).toBe('https://imagenes.test/1.jpg');
  });

  it('sin imágenes muestra un ícono y no ofrece reservar todavía', () => {
    const { fixture, req, texto } = crear();
    req.flush({ ...DETALLE, imagenes: [], servicios: [], reglas: null, politicaCancelacion: 'OTRA', tipoAlojamiento: 'GLAMPING' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.sin-foto')).not.toBeNull();
    expect(texto()).toContain('OTRA');
    expect(texto()).toContain('GLAMPING en Cartagena');
    expect(texto()).toContain('Las reservas aún no están habilitadas');
  });

  it('avisa si el alojamiento no existe o no está activo', () => {
    const { fixture, req, texto } = crear();
    req.flush({ message: 'El alojamiento no está disponible' }, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();
    expect(texto()).toContain('El alojamiento no está disponible');
  });
});