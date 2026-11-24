import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EstadoReserva, ReservaListado } from '../../models/reserva.model';
import { SesionService } from '../../services/sesion.service';
import { MisReservas } from './mis-reservas';

/** Fechas futuras por defecto; las estancias terminadas usan fechas pasadas. */
const reserva = (idReserva: number, estado: EstadoReserva, titulo: string, fechaSalida = '2099-12-23'): ReservaListado => ({
  idReserva, estado, fechaLlegada: '2026-12-20', fechaSalida, numHuespedes: 2, total: 500000,
  idPublicacion: 15, tituloPublicacion: titulo, idHuesped: 20, nombreHuesped: 'Luis', idAnfitrion: 7, nombreAnfitrion: 'Ana Gómez'
});

const LISTA = [
  reserva(31, 'CONFIRMADA', 'Cabaña con vista al lago'),
  reserva(32, 'PENDIENTE', 'Apartamento en Chapinero'),
  reserva(33, 'CANCELADA', 'Finca en Villeta')
];

function crear(lista: ReservaListado[] = LISTA, resenas: { idReserva: number }[] = []) {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [MisReservas],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
  });
  TestBed.inject(SesionService).iniciar({ idUsuario: 20, nombreCompleto: 'Luis', correo: 'l@l.co', rol: 'HUESPED' });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(MisReservas);
  fixture.detectChanges();
  http.expectOne((r) => r.url === '/api/reservas/huesped' && r.params.get('idHuesped') === '20').flush(lista);
  http.expectOne('/api/resenas/huesped/20').flush(resenas);
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;
  const botones = (texto: string) => [...el.querySelectorAll('button')].filter((b) => b.textContent?.trim() === texto);
  return { fixture, http, el, botones, texto: () => el.textContent as string };
}

describe('MisReservas', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('lista las reservas del huésped con su estado, fechas y total', () => {
    const { texto } = crear();
    expect(texto()).toContain('Cabaña con vista al lago');
    expect(texto()).toContain('Confirmada');
    expect(texto()).toContain('Pendiente de aprobación');
    expect(texto()).toContain('Cancelada');
    expect(texto()).toContain('20/12/2026 – 23/12/2099');
    expect(texto()).toMatch(/500[.,]000/);
  });

  it('sin reservas lo dice', () => {
    const { texto } = crear([]);
    expect(texto()).toContain('Aún no tienes reservas');
  });

  it('solo ofrece cancelar las pendientes y confirmadas', () => {
    const { botones } = crear();
    expect(botones('Cancelar reserva').length).toBe(2);
  });

  it('MA-95: cancelar abre el panel con la simulación y al terminar actualiza el estado', () => {
    const { botones, fixture, http, el, texto } = crear();
    botones('Cancelar reserva')[0].click();
    fixture.detectChanges();
    http.expectOne((r) => r.url === '/api/reservas/31/cancelacion/simulacion').flush({
      idReserva: 31, estado: 'CONFIRMADA', politica: 'FLEXIBLE', valorTotal: 500000, valorDevolucion: 500000,
      aplicaDevolucion: true, mensaje: ''
    });
    fixture.detectChanges();
    expect(texto()).toContain('devolverían');
    const area = el.querySelector('textarea')!;
    area.value = 'Cambio de planes';
    area.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    [...el.querySelectorAll('button')].find((b) => b.textContent?.trim() === 'Confirmar cancelación')!.click();
    http.expectOne('/api/reservas/31/cancelar').flush({
      idReserva: 31, estado: 'CANCELADA', valorDevolucion: 500000, estadoDevolucion: 'DEVUELTA', mensaje: ''
    });
    fixture.detectChanges();
    expect(texto()).toMatch(/Cancelaste la reserva\. Se te devolverán \$\s*500[.,]000/);
    expect(botones('Cancelar reserva').length).toBe(1);
  });

  it('si falla la consulta muestra el error y no dice que no hay reservas', () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [MisReservas],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    });
    TestBed.inject(SesionService).iniciar({ idUsuario: 20, nombreCompleto: 'Luis', correo: 'l@l.co', rol: 'HUESPED' });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(MisReservas);
    fixture.detectChanges();
    http
      .expectOne((r) => r.url === '/api/reservas/huesped')
      .flush({ message: 'No static resource api/reservas/huesped.' }, { status: 404, statusText: 'Not Found' });
    http.expectOne('/api/resenas/huesped/20').flush([]);
    fixture.detectChanges();
    const texto = fixture.nativeElement.textContent as string;
    expect(texto).toContain('No static resource');
    expect(texto).not.toContain('Aún no tienes reservas');
  });

  it('MA-81: en una estancia terminada ofrece "Dejar reseña" en vez de cancelar y la publica', () => {
    const terminada = reserva(40, 'CONFIRMADA', 'Casa en Villa de Leyva', '2026-01-10');
    const { botones, fixture, http, el, texto } = crear([terminada]);
    expect(botones('Cancelar reserva').length).toBe(0);
    botones('Dejar reseña')[0].click();
    fixture.detectChanges();
    el.querySelectorAll<HTMLInputElement>('input[type="radio"]')[4].click();
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    const req = http.expectOne((r) => r.url === '/api/resenas' && r.method === 'POST');
    expect(req.request.body).toEqual({ idHuesped: 20, idReserva: 40, calificacion: 5, comentario: null });
    req.flush({});
    fixture.detectChanges();
    expect(texto()).toContain('Gracias, publicamos tu reseña');
    expect(texto()).toContain('Ya dejaste tu reseña');
    expect(botones('Dejar reseña').length).toBe(0);
  });

  it('si la reserva ya tiene reseña no vuelve a ofrecerla', () => {
    const terminada = reserva(41, 'COMPLETADA', 'Casa en Villa de Leyva', '2026-01-10');
    const { botones, texto } = crear([terminada], [{ idReserva: 41 }]);
    expect(botones('Dejar reseña').length).toBe(0);
    expect(texto()).toContain('Ya dejaste tu reseña');
  });
});
