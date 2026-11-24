import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Resena } from '../../../models/resena.model';
import { SesionService } from '../../../services/sesion.service';
import { ListaResenas } from './lista-resenas';

const resena = (idResena: number, idHuesped: number, nombreHuesped: string, calificacion: number): Resena => ({
  idResena, idReserva: 30 + idResena, idPublicacion: 15, tituloPublicacion: 'Cabaña', idHuesped, nombreHuesped,
  calificacion, comentario: 'Comentario de ' + nombreHuesped, fecha: '2026-11-23', visible: true
});

const LISTA = [resena(1, 20, 'Luis Pérez', 5), resena(2, 21, 'Ana Gómez', 4)];

function crear(idUsuario: number | null = 30) {
  sessionStorage.clear();
  TestBed.configureTestingModule({ imports: [ListaResenas], providers: [provideHttpClient(), provideHttpClientTesting()] });
  if (idUsuario) {
    TestBed.inject(SesionService).iniciar({ idUsuario, nombreCompleto: 'Kathe', correo: 'k@k.co', rol: 'HUESPED' });
  }
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(ListaResenas);
  fixture.componentRef.setInput('idPublicacion', 15);
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;
  const botones = (texto: string) => [...el.querySelectorAll('button')].filter((b) => b.textContent?.trim() === texto);
  const clic = (texto: string, i = 0) => {
    botones(texto)[i].click();
    fixture.detectChanges();
  };
  const cargar = (lista: Resena[]) => {
    http.expectOne('/api/resenas/publicacion/15').flush(lista);
    fixture.detectChanges();
  };
  return { fixture, http, el, botones, clic, cargar, texto: () => el.textContent as string };
}

describe('ListaResenas', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('MA-81: muestra las reseñas con su promedio', () => {
    const { cargar, texto } = crear();
    cargar(LISTA);
    expect(texto()).toContain('Luis Pérez');
    expect(texto()).toContain('Comentario de Ana Gómez');
    expect(texto()).toContain('4.5');
    expect(texto()).toContain('2 reseñas');
  });

  it('sin reseñas lo dice', () => {
    const { cargar, texto } = crear();
    cargar([]);
    expect(texto()).toContain('todavía no tiene reseñas');
  });

  it('MA-82: reporta una reseña con motivo obligatorio y muestra el mensaje del back', () => {
    const { cargar, clic, el, http, fixture, texto } = crear();
    cargar(LISTA);
    clic('Reportar');
    clic('Enviar reporte');
    expect(texto()).toContain('Escribe el motivo del reporte');
    http.expectNone('/api/resenas/1/reportar');
    const area = el.querySelector('textarea')!;
    area.value = 'Lenguaje ofensivo';
    area.dispatchEvent(new Event('input'));
    clic('Enviar reporte');
    const req = http.expectOne('/api/resenas/1/reportar');
    expect(req.request.body).toEqual({ idUsuario: 30, motivo: 'Lenguaje ofensivo' });
    req.flush({ mensaje: 'La reseña fue reportada, un administrador la va a revisar' });
    fixture.detectChanges();
    expect(texto()).toContain('un administrador la va a revisar');
  });

  it('no ofrece reportar al autor ni a quien no tiene sesión', () => {
    const autor = crear(20);
    autor.cargar(LISTA);
    expect(autor.botones('Reportar').length).toBe(1);
    TestBed.resetTestingModule();
    const anonimo = crear(null);
    anonimo.cargar(LISTA);
    expect(anonimo.botones('Reportar').length).toBe(0);
  });
});
