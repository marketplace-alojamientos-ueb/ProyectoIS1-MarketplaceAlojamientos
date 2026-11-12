import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { PublicacionAnfitrion } from '../../models/publicacion-anfitrion.model';
import { SesionService } from '../../services/sesion.service';
import { MisPublicaciones } from './mis-publicaciones';

const publicacion = (idPublicacion: number, estado: string, titulo: string, ciudad: string) =>
  ({
    idPublicacion,
    estado,
    titulo,
    ciudad,
    capacidad: 4,
    precioPorNoche: 180000,
    camposFaltantes: estado === 'BORRADOR' ? ['Precio por noche'] : [],
    periodos: []
  }) as unknown as PublicacionAnfitrion;

const LISTA = [
  publicacion(1, 'ACTIVA', 'Apartamento con vista', 'Bogotá'),
  publicacion(2, 'PAUSADA', 'Cabaña', 'Guatavita'),
  publicacion(3, 'PENDIENTE_REVISION', 'Frente al mar', 'Cartagena'),
  publicacion(4, 'BORRADOR', '', ''),
  publicacion(5, 'BLOQUEADA', 'Habitación', 'Medellín')
];

function crear(aviso: string | null = null) {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [MisPublicaciones],
    providers: [
      provideRouter([]),
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: convertToParamMap(aviso ? { aviso } : {}) } } }
    ]
  });
  TestBed.inject(SesionService).iniciar({ idUsuario: 7, nombreCompleto: 'Ana', correo: 'a@a.co', rol: 'ANFITRION' });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(MisPublicaciones);
  fixture.detectChanges();
  return { fixture, http, pagina: fixture.componentInstance, texto: () => fixture.nativeElement.textContent as string };
}

const botones = (fixture: ComponentFixture<MisPublicaciones>, texto: string): HTMLButtonElement[] =>
  Array.from<HTMLButtonElement>(fixture.nativeElement.querySelectorAll('tbody button')).filter(
    (b) => b.textContent?.trim() === texto
  );
const enlaces = (fixture: ComponentFixture<MisPublicaciones>): string[] =>
  Array.from<HTMLAnchorElement>(fixture.nativeElement.querySelectorAll('a')).map(
    (a) => `${a.textContent?.trim()} ${a.getAttribute('href')}`
  );

const filas = (fixture: ComponentFixture<MisPublicaciones>) => fixture.nativeElement.querySelectorAll('tbody tr').length;
const listar = (http: HttpTestingController) => http.expectOne('/api/publicaciones?idAnfitrion=7');

describe('MisPublicaciones', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('MA-107: lista las publicaciones del anfitrión con su estado', () => {
    const { fixture, http, texto } = crear();
    listar(http).flush(LISTA);
    fixture.detectChanges();
    expect(filas(fixture)).toBe(5);
    expect(texto()).toContain('Pendiente de revisión');
    expect(texto()).toContain('Falta: Precio por noche');
    expect(texto()).toContain('Publicación bloqueada por administrador');
  });

  it('avisa cuando no tiene publicaciones', () => {
    const { fixture, http, texto } = crear();
    expect(texto()).toContain('Cargando');
    listar(http).flush([]);
    fixture.detectChanges();
    expect(texto()).toContain('Todavía no tienes publicaciones');
  });

  it('filtra por texto y por estado', () => {
    const { fixture, http, texto } = crear();
    listar(http).flush(LISTA);
    fixture.detectChanges();
    const input: HTMLInputElement = fixture.nativeElement.querySelector('input');
    input.value = 'cartagena';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(filas(fixture)).toBe(1);

    input.value = '';
    input.dispatchEvent(new Event('input'));
    const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    select.value = 'BLOQUEADA';
    select.dispatchEvent(new Event('change'));
    input.value = 'bogota';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(texto()).toContain('No hay publicaciones con esos filtros');
  });

  it('MA-108: solo ofrece pausar las activas y pide confirmación', () => {
    const { fixture, http, pagina, texto } = crear();
    listar(http).flush(LISTA);
    fixture.detectChanges();
    const pausar = botones(fixture, 'Pausar');
    expect(pausar.length).toBe(1);

    pausar[0].click();
    fixture.detectChanges();
    expect(texto()).toContain('¿Pausar?');

    pagina['pausar'](LISTA[0]);
    http.expectOne({ method: 'POST', url: '/api/publicaciones/1/pausar?idUsuario=7' }).flush({});
    listar(http).flush(LISTA);
    expect(pagina['exito']()).toContain('Pausaste');
  });

  it('MA-109: muestra el error del back si la publicación ya no está activa', () => {
    const { http, pagina } = crear();
    listar(http).flush(LISTA);
    pagina['pausar'](LISTA[0]);
    http
      .expectOne('/api/publicaciones/1/pausar?idUsuario=7')
      .flush({ message: 'Solo se pueden pausar publicaciones activas' }, { status: 409, statusText: 'Conflict' });
    expect(pagina['error']()).toBe('Solo se pueden pausar publicaciones activas');
  });

  it('muestra el error si no puede cargar', () => {
    const { http, pagina } = crear();
    listar(http).flush(null, { status: 0, statusText: '' });
    expect(pagina['error']()).toContain('No se pudo conectar');
  });

  it('MA-64 / MA-61: ofrece crear, editar y completar según el estado', () => {
    const { fixture, http } = crear();
    listar(http).flush(LISTA);
    fixture.detectChanges();
    const links = enlaces(fixture);
    expect(links).toContain('+ Nueva publicación /anfitrion/publicaciones/nueva');
    expect(links).toContain('Editar /anfitrion/publicaciones/1/editar');
    expect(links).toContain('Editar /anfitrion/publicaciones/2/editar');
    expect(links).toContain('Completar /anfitrion/publicaciones/4/editar');
    expect(links).toContain('Disponibilidad /anfitrion/disponibilidad?publicacion=1');
    expect(links).toContain('Disponibilidad /anfitrion/disponibilidad?publicacion=3');
    expect(links).toContain('Editar /anfitrion/publicaciones/5/editar');
  });

  it('muestra el aviso con que vuelve del formulario', () => {
    const { fixture, http, texto } = crear('borrador');
    listar(http).flush(LISTA);
    fixture.detectChanges();
    expect(texto()).toContain('Guardamos tu publicación como borrador');
  });

  it('MA-61: pide confirmación, elimina y muestra el error 409 del back', () => {
    const { fixture, http, pagina, texto } = crear();
    listar(http).flush(LISTA);
    fixture.detectChanges();
    expect(botones(fixture, 'Eliminar').length).toBe(5);

    botones(fixture, 'Eliminar')[3].click();
    fixture.detectChanges();
    expect(texto()).toContain('¿Eliminar?');
    botones(fixture, 'Cancelar')[0].click();
    fixture.detectChanges();
    expect(texto()).not.toContain('¿Eliminar?');

    pagina['eliminar'](LISTA[3]);
    http.expectOne({ method: 'DELETE', url: '/api/publicaciones/4?idUsuario=7' }).flush(null, { status: 204, statusText: 'No Content' });
    listar(http).flush(LISTA.filter((p) => p.idPublicacion !== 4));
    expect(pagina['exito']()).toContain('Eliminaste');

    pagina['eliminar'](LISTA[0]);
    http
      .expectOne('/api/publicaciones/1?idUsuario=7')
      .flush(
        { message: 'No se puede eliminar la publicación porque tiene datos asociados, como reservas' },
        { status: 409, statusText: 'Conflict' }
      );
    expect(pagina['error']()).toContain('tiene datos asociados');
  });
});