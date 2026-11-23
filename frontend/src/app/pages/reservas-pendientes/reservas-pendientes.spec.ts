import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ReservaPendiente } from '../../models/reserva.model';
import { SesionService } from '../../services/sesion.service';
import { ReservasPendientes } from './reservas-pendientes';

const reserva = (idReserva: number, tituloPublicacion: string, nombreHuesped: string): ReservaPendiente => ({
  idReserva,
  estado: 'PENDIENTE',
  fechaLlegada: '2026-12-20',
  fechaSalida: '2026-12-23',
  numHuespedes: 2,
  idPublicacion: 15,
  tituloPublicacion,
  idHuesped: 20,
  nombreHuesped
});

const LISTA = [reserva(31, 'Apartamento en Chapinero', 'Luis Pérez'), reserva(32, 'Cabaña con vista al lago', 'Ana Gómez')];

function crear() {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [ReservasPendientes],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
  });
  TestBed.inject(SesionService).iniciar({ idUsuario: 7, nombreCompleto: 'Ana', correo: 'a@a.co', rol: 'ANFITRION' });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(ReservasPendientes);
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;
  const pendientes = () => http.expectOne((r) => r.url === '/api/reservas/pendientes' && r.params.get('idAnfitrion') === '7');
  const boton = (texto: string, fila = 0) =>
    [...el.querySelectorAll('tbody tr')[fila].querySelectorAll('button')].find((b) => b.textContent?.trim() === texto)!;
  const clic = (texto: string, fila = 0) => {
    boton(texto, fila).click();
    fixture.detectChanges();
  };
  return { fixture, http, el, pendientes, clic, texto: () => el.textContent as string };
}

describe('ReservasPendientes', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('MA-60 escenario 1: lista las reservas pendientes del anfitrión con sus datos', () => {
    const { fixture, pendientes, texto } = crear();
    pendientes().flush(LISTA);
    fixture.detectChanges();
    expect(texto()).toContain('Apartamento en Chapinero');
    expect(texto()).toContain('Luis Pérez');
    expect(texto()).toContain('20/12/2026');
    expect(texto()).toContain('23/12/2026');
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(2);
  });

  it('MA-60 escenario 4: sin pendientes muestra "No tienes reservas pendientes"', () => {
    const { fixture, pendientes, texto } = crear();
    pendientes().flush([]);
    fixture.detectChanges();
    expect(texto()).toContain('No tienes reservas pendientes');
  });

  it('MA-60 escenario 2: aceptar la quita de la lista y muestra el mensaje del back', () => {
    const { fixture, http, pendientes, clic, texto } = crear();
    pendientes().flush(LISTA);
    fixture.detectChanges();
    clic('Aceptar');
    const req = http.expectOne((r) => r.url === '/api/reservas/31/aceptar');
    expect(req.request.method).toBe('POST');
    expect(req.request.params.get('idAnfitrion')).toBe('7');
    req.flush({ mensaje: 'La reserva quedó confirmada' });
    fixture.detectChanges();
    expect(texto()).toContain('La reserva quedó confirmada');
    expect(texto()).not.toContain('Apartamento en Chapinero');
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
  });

  it('MA-60 escenario 3: rechazar pide confirmación, la quita de la lista y muestra el mensaje', () => {
    const { fixture, http, pendientes, clic, texto } = crear();
    pendientes().flush(LISTA);
    fixture.detectChanges();
    clic('Rechazar', 1);
    expect(texto()).toContain('¿Rechazar?');
    http.expectNone((r) => r.url.includes('/rechazar'));
    clic('Sí, rechazar', 1);
    http.expectOne((r) => r.url === '/api/reservas/32/rechazar').flush({ mensaje: 'La reserva fue rechazada' });
    fixture.detectChanges();
    expect(texto()).toContain('La reserva fue rechazada');
    expect(texto()).not.toContain('Cabaña con vista al lago');
  });

  it('cancelar el rechazo no llama al back', () => {
    const { fixture, http, pendientes, clic, texto } = crear();
    pendientes().flush(LISTA);
    fixture.detectChanges();
    clic('Rechazar');
    clic('Cancelar');
    http.expectNone((r) => r.url.includes('/rechazar'));
    expect(texto()).not.toContain('¿Rechazar?');
  });

  it('si la reserva ya no está pendiente (409) muestra el mensaje y recarga la lista', () => {
    const { fixture, http, pendientes, clic, texto } = crear();
    pendientes().flush(LISTA);
    fixture.detectChanges();
    clic('Aceptar');
    http
      .expectOne((r) => r.url === '/api/reservas/31/aceptar')
      .flush({ message: 'La reserva ya no está pendiente' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(texto()).toContain('La reserva ya no está pendiente');
    pendientes().flush([LISTA[1]]);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
  });
});
