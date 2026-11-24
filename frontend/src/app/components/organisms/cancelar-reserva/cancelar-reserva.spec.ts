import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ReservaCancelada } from '../../../models/reserva.model';
import { SesionService } from '../../../services/sesion.service';
import { CancelarReserva } from './cancelar-reserva';

const SIMULACION = (devolucion: number) => ({
  idReserva: 31,
  estado: 'CONFIRMADA',
  politica: 'FLEXIBLE',
  valorTotal: 500000,
  valorDevolucion: devolucion,
  aplicaDevolucion: devolucion > 0,
  mensaje: ''
});

function crear(rol: 'HUESPED' | 'ANFITRION' = 'HUESPED') {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [CancelarReserva],
    providers: [provideHttpClient(), provideHttpClientTesting()]
  });
  const idUsuario = rol === 'HUESPED' ? 20 : 7;
  TestBed.inject(SesionService).iniciar({ idUsuario, nombreCompleto: 'Luis', correo: 'l@l.co', rol });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(CancelarReserva);
  fixture.componentRef.setInput('idReserva', 31);
  fixture.componentRef.setInput('rol', rol);
  const emitidas: ReservaCancelada[] = [];
  let volvio = false;
  fixture.componentInstance.cancelada.subscribe((r) => emitidas.push(r));
  fixture.componentInstance.volver.subscribe(() => (volvio = true));
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;
  const escribirMotivo = (texto: string) => {
    const area = el.querySelector('textarea')!;
    area.value = texto;
    area.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  };
  const clic = (texto: string) => {
    [...el.querySelectorAll('button')].find((b) => b.textContent?.trim() === texto)!.click();
    fixture.detectChanges();
  };
  return {
    fixture, http, el, escribirMotivo, clic, emitidas,
    volvio: () => volvio,
    texto: () => el.textContent as string,
    simulacion: () => http.expectOne((r) => r.url === '/api/reservas/31/cancelacion/simulacion' && r.params.get('idHuesped') === '20')
  };
}

describe('CancelarReserva', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('MA-97: al huésped le muestra cuánto se le devolvería antes de confirmar', () => {
    const { fixture, simulacion, texto } = crear();
    simulacion().flush(SIMULACION(500000));
    fixture.detectChanges();
    expect(texto()).toMatch(/devolverían\s*\$\s*500[.,]000/);
  });

  it('MA-97: si no hay devolución lo dice', () => {
    const { fixture, simulacion, texto } = crear();
    simulacion().flush(SIMULACION(0));
    fixture.detectChanges();
    expect(texto()).toContain('no recibirías devolución');
  });

  it('MA-95: exige el motivo y cancela como huésped avisando al padre', () => {
    const { fixture, http, simulacion, clic, escribirMotivo, emitidas, texto } = crear();
    simulacion().flush(SIMULACION(500000));
    fixture.detectChanges();
    clic('Confirmar cancelación');
    expect(texto()).toContain('Escribe el motivo de la cancelación');
    http.expectNone('/api/reservas/31/cancelar');
    escribirMotivo('  Cambio de planes  ');
    clic('Confirmar cancelación');
    const req = http.expectOne('/api/reservas/31/cancelar');
    expect(req.request.body).toEqual({ idHuesped: 20, motivo: 'Cambio de planes' });
    const respuesta = { idReserva: 31, estado: 'CANCELADA', valorDevolucion: 500000, estadoDevolucion: 'DEVUELTA', mensaje: 'ok' };
    req.flush(respuesta);
    expect(emitidas).toEqual([respuesta]);
  });

  it('MA-96: el anfitrión no simula, ve que se devuelve el total y cancela con su id', () => {
    const { http, clic, escribirMotivo, emitidas, texto } = crear('ANFITRION');
    http.expectNone((r) => r.url.includes('simulacion'));
    expect(texto()).toContain('se le devuelve el total');
    escribirMotivo('Imprevisto en la propiedad');
    clic('Confirmar cancelación');
    const req = http.expectOne('/api/reservas/31/cancelar-anfitrion');
    expect(req.request.body).toEqual({ idAnfitrion: 7, motivo: 'Imprevisto en la propiedad' });
    req.flush({ idReserva: 31, estado: 'CANCELADA', valorDevolucion: 500000, estadoDevolucion: 'DEVUELTA', mensaje: 'ok' });
    expect(emitidas.length).toBe(1);
  });

  it('si la reserva ya no se puede cancelar (409) muestra el mensaje y no deja confirmar', () => {
    const { fixture, simulacion, el, texto } = crear();
    simulacion().flush({ message: 'La reserva no se puede cancelar porque su estado es CANCELADA' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(texto()).toContain('su estado es CANCELADA');
    expect(el.querySelector('textarea')).toBeNull();
    expect(texto()).not.toContain('Confirmar cancelación');
  });

  it('"Volver" avisa al padre sin cancelar', () => {
    const { fixture, http, simulacion, clic, volvio } = crear();
    simulacion().flush(SIMULACION(500000));
    fixture.detectChanges();
    clic('Volver');
    expect(volvio()).toBe(true);
    http.expectNone('/api/reservas/31/cancelar');
  });
});
