import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ResenaService } from './resena.service';

describe('ResenaService', () => {
  let http: HttpTestingController;
  let servicio: ResenaService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
    servicio = TestBed.inject(ResenaService);
  });

  afterEach(() => http.verify());

  it('consulta las reseñas de una publicación y las de un huésped', () => {
    servicio.porPublicacion(15).subscribe();
    servicio.porHuesped(20).subscribe();
    expect(http.expectOne('/api/resenas/publicacion/15').request.method).toBe('GET');
    expect(http.expectOne('/api/resenas/huesped/20').request.method).toBe('GET');
  });

  it('MA-81: crea, edita y elimina una reseña', () => {
    servicio.crear({ idHuesped: 20, idReserva: 31, calificacion: 4, comentario: 'Bien' }).subscribe();
    servicio.actualizar(5, { idHuesped: 20, calificacion: 5, comentario: null }).subscribe();
    servicio.eliminar(5, 20).subscribe();
    const crear = http.expectOne((r) => r.url === '/api/resenas' && r.method === 'POST');
    expect(crear.request.body).toEqual({ idHuesped: 20, idReserva: 31, calificacion: 4, comentario: 'Bien' });
    const editar = http.expectOne((r) => r.url === '/api/resenas/5' && r.method === 'PUT');
    expect(editar.request.body).toEqual({ idHuesped: 20, calificacion: 5, comentario: null });
    const eliminar = http.expectOne((r) => r.url === '/api/resenas/5' && r.method === 'DELETE');
    expect(eliminar.request.params.get('idHuesped')).toBe('20');
  });

  it('MA-82: reporta una reseña con el motivo', () => {
    servicio.reportar(5, { idUsuario: 7, motivo: 'Lenguaje ofensivo' }).subscribe();
    const req = http.expectOne('/api/resenas/5/reportar');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ idUsuario: 7, motivo: 'Lenguaje ofensivo' });
  });
});
