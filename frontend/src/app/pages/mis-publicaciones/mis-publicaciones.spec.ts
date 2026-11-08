import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
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

function crear() {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [MisPublicaciones],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
  });
  TestBed.inject(SesionService).iniciar({ idUsuario: 7, nombreCompleto: 'Ana', correo: 'a@a.co', rol: 'ANFITRION' });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(MisPublicaciones);
  fixture.detectChanges();
  return { fixture, http, pagina: fixture.componentInstance, texto: () => fixture.nativeElement.textContent as string };
}

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
    expect(texto()).toContain('Contacta al administrador');
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
    const botones = fixture.nativeElement.querySelectorAll('tbody button');
    expect(botones.length).toBe(1);

    botones[0].click();
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
});