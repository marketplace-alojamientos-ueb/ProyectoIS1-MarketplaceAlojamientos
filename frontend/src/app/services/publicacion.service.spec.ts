import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PublicacionService } from './publicacion.service';

describe('PublicacionService', () => {
  let http: HttpTestingController;
  let servicio: PublicacionService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
    servicio = TestBed.inject(PublicacionService);
  });

  afterEach(() => http.verify());

  it('MA-99: busca enviando solo los filtros con valor', () => {
    servicio.buscar({ ciudad: 'Bogotá', capacidad: 2, tipo: '', llegada: undefined }).subscribe();
    const req = http.expectOne((r) => r.url === '/api/publicaciones/buscar');
    expect(req.request.params.keys()).toEqual(['ciudad', 'capacidad']);
    expect(req.request.params.get('capacidad')).toBe('2');
    req.flush([]);
  });

  it('MA-99: envía el rango de precio y cada servicio como el back lo espera', () => {
    servicio.buscar({ precioMin: 100000, precioMax: 300000, servicios: ['WiFi', 'Piscina'] }).subscribe();
    const req = http.expectOne((r) => r.url === '/api/publicaciones/buscar');
    expect(req.request.params.get('precioMin')).toBe('100000');
    expect(req.request.params.get('precioMax')).toBe('300000');
    expect(req.request.params.getAll('servicios')).toEqual(['WiFi', 'Piscina']);
    req.flush([]);
  });

  it('MA-100: consulta el detalle', () => {
    servicio.detalle(5).subscribe();
    http.expectOne({ method: 'GET', url: '/api/publicaciones/5/detalle' }).flush({});
  });
});