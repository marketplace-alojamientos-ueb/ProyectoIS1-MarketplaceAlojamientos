import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { PublicacionDetalle } from '../../../models/publicacion.model';
import { NavegadorService } from '../../../services/navegador.service';
import { PublicacionService } from '../../../services/publicacion.service';
import { SesionService } from '../../../services/sesion.service';
import { aISO, nochesEntre } from '../../../utils/calendario';
import { leerPagoPendiente } from '../../../utils/pago-pendiente';
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
  const irA = vi.fn();
  TestBed.configureTestingModule({
    imports: [FormularioReserva],
    providers: [
      provideRouter([]),
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: NavegadorService, useValue: { irA } }
    ]
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
  const llenar = () => {
    escribir('input#res-llegada', LLEGADA);
    escribir('input#res-salida', SALIDA);
    escribir('input#res-huespedes', '3');
  };
  const enviar = () => {
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  };
  return { fixture, http, el, escribir, llenar, enviar, irA, texto: () => el.textContent as string };
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

  it('ya no pide la tarjeta: el pago se hace en Stripe', () => {
    const { el, texto } = crear();
    expect(el.querySelector('#res-tarjeta')).toBeNull();
    expect(texto()).toContain('Stripe');
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
    enviar();
    expect(texto()).toContain('La salida debe ser posterior a la llegada');
    expect(texto()).toContain('admite máximo 4 huéspedes');
    http.expectNone('/api/reservas/checkout');
  });

  it('MA-102 con Stripe: crea la sesión de pago y lleva al huésped a la página de Stripe', () => {
    const { llenar, enviar, http, irA, fixture, texto } = crear();
    llenar();
    enviar();
    const req = http.expectOne('/api/reservas/checkout');
    expect(req.request.body).toEqual({
      idHuesped: 20,
      idPublicacion: 15,
      fechaLlegada: LLEGADA,
      fechaSalida: SALIDA,
      numHuespedes: 3
    });
    fixture.detectChanges();
    expect(texto()).toContain('Llevándote a Stripe');
    req.flush({ idSesion: 'cs_test_1', urlPago: 'https://checkout.stripe.com/c/pay/cs_test_1', idPublicacion: 15 });
    expect(irA).toHaveBeenCalledWith('https://checkout.stripe.com/c/pay/cs_test_1');
    expect(leerPagoPendiente()).toEqual({ idPublicacion: 15, tituloPublicacion: 'Cabaña con vista al lago' });
  });

  it('no crea dos sesiones si el huésped da doble clic', () => {
    const { llenar, enviar, http } = crear();
    llenar();
    enviar();
    enviar();
    expect(http.match('/api/reservas/checkout').length).toBe(1);
  });

  it('muestra el mensaje del back si las fechas no están disponibles o falta la llave de Stripe', () => {
    const { llenar, enviar, http, irA, fixture, texto } = crear();
    llenar();
    enviar();
    http
      .expectOne('/api/reservas/checkout')
      .flush({ message: 'Las fechas seleccionadas ya no están disponibles' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(texto()).toContain('ya no están disponibles');
    expect(texto()).toContain('Reservar y pagar');
    expect(irA).not.toHaveBeenCalled();
  });
});
