import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MensajeResponse } from '../models/mensaje.model';
import {
  ReservaCancelada,
  ReservaCancelarAnfitrionRequest,
  ReservaCancelarRequest,
  ReservaConfirmada,
  ReservaConfirmarRequest,
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

  /** MA-102: 201 con el resumen. 400 datos, 402 pago rechazado, 403 huésped no habilitado, 409 fechas no disponibles. */
  confirmar(datos: ReservaConfirmarRequest): Observable<ReservaConfirmada> {
    return this.http.post<ReservaConfirmada>(`${URL}/confirmar`, datos);
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
