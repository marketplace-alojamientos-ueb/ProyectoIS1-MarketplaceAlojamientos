import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MensajeResponse } from '../models/mensaje.model';
import {
  ReservaCancelada,
  ReservaCancelarAnfitrionRequest,
  ReservaCancelarRequest,
  ReservaCheckout,
  ReservaCheckoutRequest,
  ReservaConfirmada,
  ReservaConfirmarRequest,
  ReservaListado,
  ReservaPendiente,
  SimulacionCancelacion
} from '../models/reserva.model';

const URL = '/api/reservas';

/**
 * Reservas (ReservaController): el huésped confirma y paga (MA-102 / MA-103); el anfitrión acepta o rechaza (MA-60);
 * cualquiera de los dos cancela (MA-95 / MA-96), y el huésped puede simular la devolución antes (MA-97).
 */
@Injectable({ providedIn: 'root' })
export class ReservaService {
  private readonly http = inject(HttpClient);

  /**
   * Pago con Stripe, paso 1: crea la sesión de pago en Stripe (modo de pruebas) y devuelve la URL a la que se manda
   * al huésped. Todavía no hay reserva. 400 datos, 403 huésped no habilitado, 409 fechas no disponibles,
   * 502 error de Stripe, 503 el back no tiene la llave de Stripe.
   */
  iniciarCheckout(datos: ReservaCheckoutRequest): Observable<ReservaCheckout> {
    return this.http.post<ReservaCheckout>(`${URL}/checkout`, datos);
  }

  /**
   * Pago con Stripe, paso 2: cuando Stripe devuelve al huésped, el back verifica el pago y crea la reserva.
   * Se puede repetir con la misma sesión sin cobrar de nuevo. 402 sin pagar, 409 fechas ocupadas (el back ya devolvió el pago).
   */
  confirmarCheckout(idSesion: string, idHuesped: number): Observable<ReservaConfirmada> {
    const params = new HttpParams().set('idHuesped', idHuesped);
    return this.http.post<ReservaConfirmada>(`${URL}/checkout/${encodeURIComponent(idSesion)}/confirmar`, null, { params });
  }

  /** Pago simulado anterior (MA-102); la app ahora paga con Stripe (iniciarCheckout). 201 con el resumen. 400 datos, 402 pago rechazado, 403 huésped no habilitado, 409 fechas no disponibles. */
  confirmar(datos: ReservaConfirmarRequest): Observable<ReservaConfirmada> {
    return this.http.post<ReservaConfirmada>(`${URL}/confirmar`, datos);
  }

  /** "Mis reservas": todas las del huésped, de la más reciente a la más antigua; [] si no tiene. 403 si no es huésped activo. */
  delHuesped(idHuesped: number): Observable<ReservaListado[]> {
    return this.http.get<ReservaListado[]>(`${URL}/huesped`, { params: new HttpParams().set('idHuesped', idHuesped) });
  }

  /** Reservas de los alojamientos del anfitrión, de la más reciente a la más antigua; [] si no tiene. */
  delAnfitrion(idAnfitrion: number): Observable<ReservaListado[]> {
    return this.http.get<ReservaListado[]>(`${URL}/anfitrion`, { params: this.anfitrion(idAnfitrion) });
  }

  /** MA-60: reservas PENDIENTES de los alojamientos del anfitrión; [] si no tiene. */
  pendientes(idAnfitrion: number): Observable<ReservaPendiente[]> {
    return this.http.get<ReservaPendiente[]>(`${URL}/pendientes`, { params: this.anfitrion(idAnfitrion) });
  }

  /** MA-60: la reserva pasa a CONFIRMADA. 409 si ya no está PENDIENTE o las fechas ya no están libres. */
  aceptar(idReserva: number, idAnfitrion: number): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${URL}/${idReserva}/aceptar`, null, { params: this.anfitrion(idAnfitrion) });
  }

  /** MA-60: la reserva pasa a RECHAZADA. 409 si ya no está PENDIENTE. */
  rechazar(idReserva: number, idAnfitrion: number): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${URL}/${idReserva}/rechazar`, null, { params: this.anfitrion(idAnfitrion) });
  }

  /** MA-97: cuánto se le devolvería al huésped si cancela ahora. No cambia nada. 403 si no es suya, 409 si ya no se puede cancelar. */
  simularCancelacion(idReserva: number, idHuesped: number): Observable<SimulacionCancelacion> {
    const params = new HttpParams().set('idHuesped', idHuesped);
    return this.http.get<SimulacionCancelacion>(`${URL}/${idReserva}/cancelacion/simulacion`, { params });
  }

  /** MA-95: el huésped cancela; la devolución sigue la política del alojamiento. 400 motivo, 403 no es suya, 409 no se puede cancelar. */
  cancelar(idReserva: number, datos: ReservaCancelarRequest): Observable<ReservaCancelada> {
    return this.http.post<ReservaCancelada>(`${URL}/${idReserva}/cancelar`, datos);
  }

  /** MA-96: el anfitrión cancela; al huésped siempre se le devuelve el total. Mismos errores que cancelar. */
  cancelarComoAnfitrion(idReserva: number, datos: ReservaCancelarAnfitrionRequest): Observable<ReservaCancelada> {
    return this.http.post<ReservaCancelada>(`${URL}/${idReserva}/cancelar-anfitrion`, datos);
  }

  private anfitrion(idAnfitrion: number): HttpParams {
    return new HttpParams().set('idAnfitrion', idAnfitrion);
  }
}
