import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ReservaService } from './reserva.service';

describe('ReservaService', () => {
  let http: HttpTestingController;
  let servicio: ReservaService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
    servicio = TestBed.inject(ReservaService);
  });

  afterEach(() => http.verify());

  it('MA-102: confirma la reserva enviando el cuerpo completo', () => {
    const datos = {
      idHuesped: 20,
      idPublicacion: 15,
      fechaLlegada: '2026-12-20',
      fechaSalida: '2026-12-23',
      numHuespedes: 3,
      claveIdempotencia: 'clave-1',
      numeroTarjeta: '4242424242424242'
    };
    servicio.confirmar(datos).subscribe();
    const req = http.expectOne('/api/reservas/confirmar');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(datos);
    req.flush({});
  });

  it('MA-60: consulta las pendientes del anfitrión', () => {
    servicio.pendientes(7).subscribe();
    const req = http.expectOne((r) => r.url === '/api/reservas/pendientes');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('idAnfitrion')).toBe('7');
    req.flush([]);
  });

  it('MA-60: acepta y rechaza indicando el anfitrión', () => {
    servicio.aceptar(31, 7).subscribe();
    servicio.rechazar(32, 7).subscribe();
    const aceptar = http.expectOne((r) => r.url === '/api/reservas/31/aceptar');
    const rechazar = http.expectOne((r) => r.url === '/api/reservas/32/rechazar');
    expect(aceptar.request.method).toBe('POST');
    expect(aceptar.request.params.get('idAnfitrion')).toBe('7');
    expect(rechazar.request.params.get('idAnfitrion')).toBe('7');
    aceptar.flush({ mensaje: 'ok' });
    rechazar.flush({ mensaje: 'ok' });
  });

  it('MA-97: simula la devolución indicando el huésped', () => {
    servicio.simularCancelacion(31, 20).subscribe();
    const req = http.expectOne((r) => r.url === '/api/reservas/31/cancelacion/simulacion');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('idHuesped')).toBe('20');
    req.flush({});
  });

  it('MA-95 y MA-96: cancela como huésped y como anfitrión con el motivo', () => {
    servicio.cancelar(31, { idHuesped: 20, motivo: 'Cambio de planes' }).subscribe();
    servicio.cancelarComoAnfitrion(32, { idAnfitrion: 7, motivo: 'Imprevisto' }).subscribe();
    const huesped = http.expectOne('/api/reservas/31/cancelar');
    const anfitrion = http.expectOne('/api/reservas/32/cancelar-anfitrion');
    expect(huesped.request.method).toBe('POST');
    expect(huesped.request.body).toEqual({ idHuesped: 20, motivo: 'Cambio de planes' });
    expect(anfitrion.request.body).toEqual({ idAnfitrion: 7, motivo: 'Imprevisto' });
    huesped.flush({});
    anfitrion.flush({});
  });
});
