import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EstadoReserva, ReservaListado } from '../../models/reserva.model';
import { SesionService } from '../../services/sesion.service';
import { MisReservas } from './mis-reservas';

const reserva = (idReserva: number, estado: EstadoReserva, titulo: string): ReservaListado => ({
  idReserva, estado, fechaLlegada: '2026-12-20', fechaSalida: '2026-12-23', numHuespedes: 2, total: 500000,
  idPublicacion: 15, tituloPublicacion: titulo, idHuesped: 20, nombreHuesped: 'Luis', idAnfitrion: 7, nombreAnfitrion: 'Ana Gómez'
});

const LISTA = [
  reserva(31, 'CONFIRMADA', 'Cabaña con vista al lago'),
  reserva(32, 'PENDIENTE', 'Apartamento en Chapinero'),
  reserva(33, 'CANCELADA', 'Finca en Villeta')
];

function crear(lista: ReservaListado[] = LISTA) {
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
    expect(texto()).toContain('20/12/2026');
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
    fixture.detectChanges();
    const texto = fixture.nativeElement.textContent as string;
    expect(texto).toContain('No static resource');
    expect(texto).not.toContain('Aún no tienes reservas');
  });
});
