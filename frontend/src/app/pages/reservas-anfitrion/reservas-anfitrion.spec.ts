import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EstadoReserva, ReservaListado } from '../../models/reserva.model';
import { SesionService } from '../../services/sesion.service';
import { ReservasAnfitrion } from './reservas-anfitrion';

const reserva = (idReserva: number, estado: EstadoReserva, huesped: string): ReservaListado => ({
  idReserva, estado, fechaLlegada: '2026-12-20', fechaSalida: '2026-12-23', numHuespedes: 2, total: 500000,
  idPublicacion: 15, tituloPublicacion: 'Cabaña con vista al lago', idHuesped: 20, nombreHuesped: huesped,
  idAnfitrion: 7, nombreAnfitrion: 'Ana'
});

const LISTA = [reserva(31, 'CONFIRMADA', 'Luis Pérez'), reserva(32, 'PENDIENTE', 'Marta Ruiz'), reserva(33, 'CANCELADA', 'Pedro Gil')];

function crear(lista: ReservaListado[] = LISTA) {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [ReservasAnfitrion],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
  });
  TestBed.inject(SesionService).iniciar({ idUsuario: 7, nombreCompleto: 'Ana', correo: 'a@a.co', rol: 'ANFITRION' });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(ReservasAnfitrion);
  fixture.detectChanges();
  http.expectOne((r) => r.url === '/api/reservas/anfitrion' && r.params.get('idAnfitrion') === '7').flush(lista);
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;
  const botones = (texto: string) => [...el.querySelectorAll('button')].filter((b) => b.textContent?.trim() === texto);
  return { fixture, http, el, botones, texto: () => el.textContent as string };
}

describe('ReservasAnfitrion', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('lista las reservas de sus alojamientos con huésped, estado y total', () => {
    const { texto, el } = crear();
    expect(texto()).toContain('Luis Pérez');
    expect(texto()).toContain('Pendiente de aprobación');
    expect(texto()).toMatch(/500[.,]000/);
    expect(el.querySelectorAll('article').length).toBe(3);
  });

  it('filtra por estado', () => {
    const { el, fixture, texto } = crear();
    const select = el.querySelector('select')!;
    select.value = 'CANCELADA';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(el.querySelectorAll('article').length).toBe(1);
    expect(texto()).toContain('Pedro Gil');
  });

  it('sin reservas lo dice', () => {
    const { texto } = crear([]);
    expect(texto()).toContain('Aún no tienes reservas');
  });

  it('MA-96: cancela sin simulación y avisa que al huésped se le devuelve el total', () => {
    const { botones, fixture, http, el, texto } = crear();
    expect(botones('Cancelar reserva').length).toBe(2);
    botones('Cancelar reserva')[0].click();
    fixture.detectChanges();
    http.expectNone((r) => r.url.includes('simulacion'));
    expect(texto()).toContain('se le devuelve el total');
    const area = el.querySelector('textarea')!;
    area.value = 'Imprevisto en la propiedad';
    area.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    [...el.querySelectorAll('button')].find((b) => b.textContent?.trim() === 'Confirmar cancelación')!.click();
    const req = http.expectOne('/api/reservas/31/cancelar-anfitrion');
    expect(req.request.body).toEqual({ idAnfitrion: 7, motivo: 'Imprevisto en la propiedad' });
    req.flush({ idReserva: 31, estado: 'CANCELADA', valorDevolucion: 500000, estadoDevolucion: 'DEVUELTA', mensaje: '' });
    fixture.detectChanges();
    expect(texto()).toMatch(/Al huésped se le devolverán \$\s*500[.,]000/);
    expect(botones('Cancelar reserva').length).toBe(1);
  });
});
