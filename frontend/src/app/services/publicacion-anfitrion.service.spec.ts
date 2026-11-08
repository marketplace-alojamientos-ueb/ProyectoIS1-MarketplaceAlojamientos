import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PublicacionAnfitrionService } from './publicacion-anfitrion.service';

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
});