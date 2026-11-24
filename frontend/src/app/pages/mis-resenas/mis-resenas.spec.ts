import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Resena } from '../../models/resena.model';
import { SesionService } from '../../services/sesion.service';
import { MisResenas } from './mis-resenas';

const resena = (idResena: number, titulo: string, visible = true): Resena => ({
  idResena, idReserva: 30 + idResena, idPublicacion: 15, tituloPublicacion: titulo, idHuesped: 20,
  nombreHuesped: 'Luis', calificacion: 3, comentario: 'Normal', fecha: '2026-11-23', visible
});

const LISTA = [resena(1, 'Cabaña con vista al lago'), resena(2, 'Apartamento en Chapinero', false)];

function crear() {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [MisResenas],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
  });
  TestBed.inject(SesionService).iniciar({ idUsuario: 20, nombreCompleto: 'Luis', correo: 'l@l.co', rol: 'HUESPED' });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(MisResenas);
  fixture.detectChanges();
  http.expectOne('/api/resenas/huesped/20').flush(LISTA);
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;
  const clic = (texto: string) => {
    [...el.querySelectorAll('button')].find((b) => b.textContent?.trim() === texto)!.click();
    fixture.detectChanges();
  };
  return { fixture, http, el, clic, texto: () => el.textContent as string };
}

describe('MisResenas', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('lista las reseñas del huésped y marca las ocultas sin dejar editarlas', () => {
    const { el, texto } = crear();
    expect(texto()).toContain('Cabaña con vista al lago');
    expect(texto()).toContain('Oculta por moderación');
    expect(texto()).toContain('Ya no se puede editar ni eliminar');
    expect([...el.querySelectorAll('button')].filter((b) => b.textContent?.trim() === 'Editar').length).toBe(1);
  });

  it('edita una reseña y muestra el cambio', () => {
    const { el, clic, http, fixture, texto } = crear();
    clic('Editar');
    el.querySelectorAll<HTMLInputElement>('input[type="radio"]')[4].click();
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    const req = http.expectOne((r) => r.url === '/api/resenas/1' && r.method === 'PUT');
    expect(req.request.body).toEqual({ idHuesped: 20, calificacion: 5, comentario: 'Normal' });
    req.flush({ ...LISTA[0], calificacion: 5 });
    fixture.detectChanges();
    expect(texto()).toContain('Guardamos los cambios de tu reseña');
    expect(el.querySelector('[aria-label="5 de 5 estrellas"]')).not.toBeNull();
  });

  it('elimina una reseña después de confirmar', () => {
    const { clic, http, fixture, texto } = crear();
    clic('Eliminar');
    expect(texto()).toContain('¿Eliminar la reseña?');
    clic('Sí, eliminar');
    const req = http.expectOne((r) => r.url === '/api/resenas/1' && r.method === 'DELETE');
    expect(req.request.params.get('idHuesped')).toBe('20');
    req.flush({ mensaje: 'La reseña fue eliminada' });
    fixture.detectChanges();
    expect(texto()).toContain('La reseña fue eliminada');
    expect(texto()).not.toContain('Cabaña con vista al lago');
  });
});
