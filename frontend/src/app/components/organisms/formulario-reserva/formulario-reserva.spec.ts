import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { PublicacionDetalle } from '../../../models/publicacion.model';
import { PublicacionService } from '../../../services/publicacion.service';
import { SesionService } from '../../../services/sesion.service';
import { aISO, nochesEntre } from '../../../utils/calendario';
import { FormularioReserva } from './formulario-reserva';

const PUBLICACION: PublicacionDetalle = {
  idPublicacion: 15,
  titulo: 'Cabaña con vista al lago',
  descripcion: 'Tranquila',
  ciudad: 'Guatavita',
  ubicacion: 'Vereda El Lago',
  tipoAlojamiento: 'CABANA',
  capacidad: 4,
  precioPorNoche: 150000,
  tarifaLimpieza: 50000,
  reglas: null,
  modoAceptacion: 'INMEDIATA',
  politicaCancelacion: 'FLEXIBLE',
  servicios: [],
  imagenes: []
};

/** Fecha AAAA-MM-DD dentro de n días, para que las pruebas no dependan del día en que corren. */
function enDias(n: number): string {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() + n);
  return aISO(fecha);
}

const LLEGADA = enDias(10);
const SALIDA = enDias(13);

function crear(rol: 'HUESPED' | 'ANFITRION' | null = 'HUESPED', filtros = {}) {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [FormularioReserva],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
  });
  if (rol) {
    TestBed.inject(SesionService).iniciar({ idUsuario: 20, nombreCompleto: 'Luis Pérez', correo: 'luis@test.co', rol });
  }
  TestBed.inject(PublicacionService).ultimosFiltros = filtros;
  const fixture = TestBed.createComponent(FormularioReserva);
  fixture.componentRef.setInput('publicacion', PUBLICACION);
  fixture.detectChanges();
  const http = TestBed.inject(HttpTestingController);
  const el = fixture.nativeElement as HTMLElement;
  const escribir = (selector: string, valor: string) => {
    const input = el.querySelector<HTMLInputElement>(selector)!;
    input.value = valor;
    input.dispatchEvent(new Event('input'));
  };
  const llenar = (tarjeta = '4242424242424242') => {
    escribir('input#res-llegada', LLEGADA);
    escribir('input#res-salida', SALIDA);
    escribir('input#res-huespedes', '3');
    escribir('input#res-tarjeta', tarjeta);
  };
  const enviar = () => {
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  };
  return { fixture, http, el, escribir, llenar, enviar, texto: () => el.textContent as string };
}

describe('FormularioReserva', () => {
  it('cuenta las noches entre llegada y salida', () => {
    expect(nochesEntre('2026-12-20', '2026-12-23')).toBe(3);
    expect(nochesEntre('2026-12-23', '2026-12-20')).toBe(0);
    expect(nochesEntre('', '2026-12-20')).toBe(0);
  });

  it('sin sesión invita a iniciar sesión y no muestra el formulario', () => {
    const { el, texto } = crear(null);
    expect(texto()).toContain('inicia sesión');
    expect(el.querySelector('form')).toBeNull();
  });

  it('un anfitrión no puede reservar', () => {
    const { el, texto } = crear('ANFITRION');
    expect(texto()).toContain('solo están disponibles para huéspedes');
    expect(el.querySelector('form')).toBeNull();
  });

  it('precarga las fechas de la búsqueda y muestra el total estimado', () => {
    const { el, fixture, texto } = crear('HUESPED', { llegada: LLEGADA, salida: SALIDA, capacidad: 2 });
    fixture.detectChanges();
    expect(el.querySelector<HTMLInputElement>('input#res-llegada')!.value).toBe(LLEGADA);
    expect(el.querySelector<HTMLInputElement>('input#res-huespedes')!.value).toBe('2');
    expect(texto()).toContain('3 noches');
    expect(texto()).toMatch(/500[.,]000/); // 3 × 150.000 + 50.000
  });

  it('no envía si la salida no es posterior a la llegada o se pasa de la capacidad', () => {
    const { escribir, enviar, http, texto } = crear();
    escribir('input#res-llegada', SALIDA);
    escribir('input#res-salida', LLEGADA);
    escribir('input#res-huespedes', '9');
    escribir('input#res-tarjeta', '4242424242424242');
    enviar();
    expect(texto()).toContain('La salida debe ser posterior a la llegada');
    expect(texto()).toContain('admite máximo 4 huéspedes');
    http.expectNone('/api/reservas/confirmar');
  });

  it('MA-102: confirma y muestra el resumen que devuelve el back', () => {
    const { llenar, enviar, http, fixture, texto } = crear();
    llenar();
    enviar();
    const req = http.expectOne('/api/reservas/confirmar');
    expect(req.request.body).toEqual(
      expect.objectContaining({
        idHuesped: 20,
        idPublicacion: 15,
        fechaLlegada: LLEGADA,
        fechaSalida: SALIDA,
        numHuespedes: 3,
        numeroTarjeta: '4242424242424242'
      })
    );
    expect(req.request.body.claveIdempotencia).toBeTruthy();
    req.flush(
      {
        idReserva: 31, estado: 'CONFIRMADA', fechaLlegada: LLEGADA, fechaSalida: SALIDA, numHuespedes: 3,
        idPublicacion: 15, tituloPublicacion: 'Cabaña con vista al lago', noches: 3, precioBase: 450000,
        tarifaLimpieza: 50000, total: 500000, idTransaccion: 'SIM-123',
        mensaje: 'El pago fue aprobado y la reserva quedó confirmada'
      },
      { status: 201, statusText: 'Created' }
    );
    fixture.detectChanges();
    expect(texto()).toContain('El pago fue aprobado y la reserva quedó confirmada');
    expect(texto()).toContain('SIM-123');
  });

  it('con aprobación del anfitrión avisa que la solicitud quedó en espera', () => {
    const { llenar, enviar, http, fixture, texto } = crear();
    llenar();
    enviar();
    http.expectOne('/api/reservas/confirmar').flush({ estado: 'PENDIENTE', noches: 3, total: 500000 });
    fixture.detectChanges();
    expect(texto()).toContain('en espera de aprobación del anfitrión');
  });

  it('MA-103: reintentar con los mismos datos usa la misma clave; cambiar un dato genera otra', () => {
    const { llenar, enviar, escribir, http } = crear();
    llenar('4000000000000002');
    enviar();
    const primero = http.expectOne('/api/reservas/confirmar');
    const clave = primero.request.body.claveIdempotencia;
    primero.flush({ message: 'El pago fue rechazado' }, { status: 402, statusText: 'Payment Required' });
    enviar();
    const reintento = http.expectOne('/api/reservas/confirmar');
    expect(reintento.request.body.claveIdempotencia).toBe(clave);
    reintento.flush({ message: 'El pago fue rechazado' }, { status: 402, statusText: 'Payment Required' });
    escribir('input#res-tarjeta', '4242424242424242');
    enviar();
    expect(http.expectOne('/api/reservas/confirmar').request.body.claveIdempotencia).not.toBe(clave);
  });

  it('muestra el mensaje del back si el pago es rechazado o las fechas ya no están libres', () => {
    const { llenar, enviar, http, fixture, texto } = crear();
    llenar('4000000000000002');
    enviar();
    http
      .expectOne('/api/reservas/confirmar')
      .flush({ message: 'El pago fue rechazado' }, { status: 402, statusText: 'Payment Required' });
    fixture.detectChanges();
    expect(texto()).toContain('El pago fue rechazado');
  });
});
