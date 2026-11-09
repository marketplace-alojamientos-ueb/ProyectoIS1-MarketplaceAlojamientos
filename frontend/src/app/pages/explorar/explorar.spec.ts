import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { PublicacionBusqueda } from '../../models/publicacion.model';
import { PublicacionService } from '../../services/publicacion.service';
import { Explorar } from './explorar';

const resultado = {
  idPublicacion: 1,
  titulo: 'Cabaña con chimenea',
  ciudad: 'Guatavita',
  tipoAlojamiento: 'CABANA',
  capacidad: 4,
  precioPorNoche: 180000,
  servicios: []
} as unknown as PublicacionBusqueda;

function crear() {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [Explorar],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
  });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(Explorar);
  fixture.detectChanges();
  return { fixture, http, pagina: fixture.componentInstance, texto: () => fixture.nativeElement.textContent as string };
}

const buscar = (http: HttpTestingController) => http.expectOne((r) => r.url === '/api/publicaciones/buscar');

describe('Explorar', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('MA-99: busca al entrar y muestra los resultados en tarjetas', () => {
    const { fixture, http } = crear();
    buscar(http).flush([resultado]);
    fixture.detectChanges();
    const tarjetas = fixture.nativeElement.querySelectorAll('app-tarjeta-publicacion');
    expect(tarjetas.length).toBe(1);
    expect(tarjetas[0].textContent).toContain('Cabaña con chimenea');
    expect(tarjetas[0].textContent).toContain('Cabaña');
    expect(tarjetas[0].querySelector('a').getAttribute('href')).toBe('/publicaciones/1');
  });

  it('MA-99: envía todos los filtros que acepta el back, los recuerda y avisa si no hay resultados', () => {
    const { fixture, http, pagina, texto } = crear();
    buscar(http).flush([]);
    pagina['filtros'].patchValue({
      ciudad: ' Guatavita ',
      llegada: '2026-12-20',
      salida: '2026-12-23',
      capacidad: '4',
      tipo: 'CABANA',
      precioMin: '100000',
      precioMax: '300000',
      servicios: ' WiFi, Piscina ,'
    });
    pagina['buscar']();
    const req = buscar(http);
    expect(req.request.params.get('ciudad')).toBe('Guatavita');
    expect(req.request.params.get('llegada')).toBe('2026-12-20');
    expect(req.request.params.get('salida')).toBe('2026-12-23');
    expect(req.request.params.get('capacidad')).toBe('4');
    expect(req.request.params.get('tipo')).toBe('CABANA');
    expect(req.request.params.get('precioMin')).toBe('100000');
    expect(req.request.params.get('precioMax')).toBe('300000');
    expect(req.request.params.getAll('servicios')).toEqual(['WiFi', 'Piscina']);
    req.flush([]);
    fixture.detectChanges();
    expect(texto()).toContain('No encontramos alojamientos');
    expect(TestBed.inject(PublicacionService).ultimosFiltros.ciudad).toBe('Guatavita');

    pagina['limpiar']();
    expect(buscar(http).request.params.keys()).toEqual([]);
  });

  it('vuelve con los últimos filtros al regresar del detalle', () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [Explorar],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    });
    TestBed.inject(PublicacionService).ultimosFiltros = { ciudad: 'Bogotá', capacidad: 2, servicios: ['WiFi'] };
    const fixture = TestBed.createComponent(Explorar);
    fixture.detectChanges();
    const req = buscar(TestBed.inject(HttpTestingController));
    expect(req.request.params.get('ciudad')).toBe('Bogotá');
    expect(req.request.params.get('capacidad')).toBe('2');
    expect(fixture.componentInstance['filtros'].controls.servicios.value).toBe('WiFi');
    req.flush([]);
  });

  it('muestra el mensaje del back si la búsqueda es inválida', () => {
    const { fixture, http, texto } = crear();
    buscar(http).flush(
      { message: 'La fecha de salida debe ser posterior a la fecha de llegada' },
      { status: 400, statusText: 'Bad Request' }
    );
    fixture.detectChanges();
    expect(texto()).toContain('La fecha de salida debe ser posterior');
  });
});