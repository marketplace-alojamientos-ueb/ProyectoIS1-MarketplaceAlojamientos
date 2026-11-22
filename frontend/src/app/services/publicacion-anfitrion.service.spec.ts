import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DatosPublicacion } from '../models/publicacion-anfitrion.model';
import { PublicacionAnfitrionService } from './publicacion-anfitrion.service';

const datos: DatosPublicacion = {
  titulo: 'Loft',
  descripcion: 'Loft iluminado',
  ubicacion: 'Calle 1',
  ciudad: 'Bogotá',
  tipoAlojamiento: 'APARTAMENTO',
  capacidad: 2,
  precioPorNoche: 100000,
  tarifaLimpieza: 0,
  reglas: null,
  modoAceptacion: 'INMEDIATA',
  politicaCancelacion: 'FLEXIBLE'
};

describe('PublicacionAnfitrionService', () => {
  let http: HttpTestingController;
  let servicio: PublicacionAnfitrionService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
    servicio = TestBed.inject(PublicacionAnfitrionService);
  });

  afterEach(() => http.verify());

  it('MA-107: lista las publicaciones del anfitrión', () => {
    servicio.listar(7).subscribe((lista) => expect(lista).toEqual([]));
    http.expectOne({ method: 'GET', url: '/api/publicaciones?idAnfitrion=7' }).flush([]);
  });

  it('MA-108: pausa una publicación indicando quién la pausa', () => {
    servicio.pausar(3, 7).subscribe();
    const req = http.expectOne({ method: 'POST', url: '/api/publicaciones/3/pausar?idUsuario=7' });
    expect(req.request.body).toBeNull();
    req.flush({});
  });

  it('MA-64 / MA-77: crea la publicación con sus periodos', () => {
    const crear = { ...datos, idAnfitrion: 7, periodos: [{ fechaInicio: '2026-12-01', fechaFin: '2026-12-15' }] };
    servicio.crear(crear).subscribe();
    expect(http.expectOne({ method: 'POST', url: '/api/publicaciones' }).request.body).toEqual(crear);
  });

  it('MA-64: trae las políticas de cancelación', () => {
    servicio.listarPoliticas().subscribe();
    http.expectOne({ method: 'GET', url: '/api/publicaciones/politicas-cancelacion' }).flush([]);
  });

  it('MA-61: trae la publicación para editarla y guarda los cambios', () => {
    const editar = { ...datos, idUsuario: 7 };
    servicio.obtenerParaEditar(3, 7).subscribe();
    servicio.editar(3, editar).subscribe();
    http.expectOne({ method: 'GET', url: '/api/publicaciones/3/edicion?idUsuario=7' }).flush({});
    expect(http.expectOne({ method: 'PUT', url: '/api/publicaciones/3' }).request.body).toEqual(editar);
  });

  it('MA-61: elimina la publicación indicando quién la elimina', () => {
    servicio.eliminar(3, 7).subscribe();
    http.expectOne({ method: 'DELETE', url: '/api/publicaciones/3?idUsuario=7' }).flush(null, { status: 204, statusText: 'No Content' });
  });
});