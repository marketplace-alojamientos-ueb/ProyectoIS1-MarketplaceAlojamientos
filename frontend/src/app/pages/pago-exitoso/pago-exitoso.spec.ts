import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { SesionService } from '../../services/sesion.service';
import { guardarPagoPendiente, leerPagoPendiente } from '../../utils/pago-pendiente';
import { PagoExitoso } from './pago-exitoso';

const RESERVA = {
  idReserva: 31, estado: 'CONFIRMADA', fechaLlegada: '2026-12-20', fechaSalida: '2026-12-23', numHuespedes: 2,
  idPublicacion: 15, tituloPublicacion: 'Cabaña con vista al lago', noches: 3, precioBase: 450000,
  tarifaLimpieza: 50000, total: 500000, idTransaccion: 'pi_3Abc', mensaje: 'El pago fue aprobado y la reserva quedó confirmada'
};

function crear(sessionId: string | null) {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [PagoExitoso],
    providers: [
      provideRouter([]),
      provideHttpClient(),
      provideHttpClientTesting(),
      {
        provide: ActivatedRoute,
        useValue: { snapshot: { queryParamMap: convertToParamMap(sessionId ? { session_id: sessionId } : {}) } }
      }
    ]
  });
  TestBed.inject(SesionService).iniciar({ idUsuario: 20, nombreCompleto: 'Luis', correo: 'l@l.co', rol: 'HUESPED' });
  guardarPagoPendiente({ idPublicacion: 15, tituloPublicacion: 'Cabaña con vista al lago' });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(PagoExitoso);
  fixture.detectChanges();
  return { fixture, http, el: fixture.nativeElement as HTMLElement, texto: () => fixture.nativeElement.textContent as string };
}

describe('PagoExitoso', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('confirma el pago con el session_id de Stripe y muestra la reserva', () => {
    const { fixture, http, texto } = crear('cs_test_123');
    expect(texto()).toContain('Confirmando tu pago');
    const req = http.expectOne((r) => r.url === '/api/reservas/checkout/cs_test_123/confirmar');
    expect(req.request.method).toBe('POST');
    expect(req.request.params.get('idHuesped')).toBe('20');
    req.flush(RESERVA, { status: 201, statusText: 'Created' });
    fixture.detectChanges();
    expect(texto()).toContain('la reserva quedó confirmada');
    expect(texto()).toContain('pi_3Abc');
    expect(texto()).toMatch(/500[.,]000/);
    expect(leerPagoPendiente()).toBeNull();
  });

  it('si el alojamiento requiere aprobación avisa que quedó en espera', () => {
    const { fixture, http, texto } = crear('cs_test_123');
    http.expectOne((r) => r.url.includes('/checkout/')).flush({ ...RESERVA, estado: 'PENDIENTE' });
    fixture.detectChanges();
    expect(texto()).toContain('en espera de aprobación del anfitrión');
  });

  it('si el back responde con error muestra el mensaje y permite volver al alojamiento', () => {
    const { fixture, http, el, texto } = crear('cs_test_123');
    http
      .expectOne((r) => r.url.includes('/checkout/'))
      .flush({ message: 'Otra persona reservó esas fechas; te devolvimos el pago' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(texto()).toContain('te devolvimos el pago');
    expect(el.querySelector('a[href="/publicaciones/15"]')).not.toBeNull();
  });

  it('sin session_id no llama al back y lo explica', () => {
    const { http, texto } = crear(null);
    http.expectNone((r) => r.url.includes('/checkout/'));
    expect(texto()).toContain('No encontramos el pago');
  });
});
