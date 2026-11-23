import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MensajeResponse } from '../models/mensaje.model';
import { ReservaConfirmada, ReservaConfirmarRequest, ReservaPendiente } from '../models/reserva.model';

const URL = '/api/reservas';

/** Reservas (ReservaController): el huésped confirma y paga (MA-102 / MA-103); el anfitrión acepta o rechaza (MA-60). */
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

  private anfitrion(idAnfitrion: number): HttpParams {
    return new HttpParams().set('idAnfitrion', idAnfitrion);
  }
}
