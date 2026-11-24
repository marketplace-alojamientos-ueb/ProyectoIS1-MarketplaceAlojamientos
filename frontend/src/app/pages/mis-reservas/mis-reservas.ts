import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Insignia } from '../../components/atoms/insignia/insignia';
import { CancelarReserva } from '../../components/organisms/cancelar-reserva/cancelar-reserva';
import { FormularioResena } from '../../components/organisms/formulario-resena/formulario-resena';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { DatosResena } from '../../models/resena.model';
import { ReservaCancelada, ReservaListado } from '../../models/reserva.model';
import { ResenaService } from '../../services/resena.service';
import { ReservaService } from '../../services/reserva.service';
import { SesionService } from '../../services/sesion.service';
import { aISO } from '../../utils/calendario';
import { mensajeDelBack } from '../../utils/errores';
import { ESTADOS_RESERVA, sePuedeCancelar } from '../../utils/etiquetas';

/**
 * H-05 (MA-95 / MA-97 / MA-81): reservas del huésped (GET /api/reservas/huesped).
 * En las pendientes o confirmadas que no han terminado puede cancelar: el panel le muestra primero cuánto se le
 * devolvería y le pide el motivo. En las estancias terminadas (COMPLETADA, o CONFIRMADA con la salida ya pasada,
 * igual que exige el back) puede dejar una reseña, una sola por reserva.
 */
@Component({
  selector: 'app-mis-reservas',
  imports: [PlantillaPrincipal, RouterLink, CurrencyPipe, DatePipe, Alerta, Insignia, CancelarReserva, FormularioResena],
  templateUrl: './mis-reservas.html',
  styleUrl: './mis-reservas.css'
})
export class MisReservas {
  private readonly servicio = inject(ReservaService);
  private readonly resenas = inject(ResenaService);
  private readonly hoy = aISO(new Date());
  private readonly idUsuario = inject(SesionService).usuario()?.idUsuario ?? 0;

  protected readonly estados = ESTADOS_RESERVA;
  protected readonly reservas = signal<ReservaListado[]>([]);
  protected readonly cargado = signal(false);
  /** Reserva con el panel de cancelación abierto. */
  protected readonly cancelando = signal<number | null>(null);
  /** Reserva con el formulario de reseña abierto. */
  protected readonly resenando = signal<number | null>(null);
  protected readonly guardandoResena = signal(false);
  /** Reservas que ya tienen reseña del huésped (el back solo permite una). */
  protected readonly conResena = signal<Set<number>>(new Set());
  protected readonly exito = signal('');
  protected readonly error = signal('');

  constructor() {
    this.servicio.delHuesped(this.idUsuario).subscribe({
      next: (lista) => {
        this.reservas.set(lista);
        this.cargado.set(true);
      },
      error: (e: HttpErrorResponse) => {
        this.cargado.set(true);
        this.error.set(mensajeDelBack(e));
      }
    });
    this.resenas.porHuesped(this.idUsuario).subscribe({
      next: (lista) => this.conResena.set(new Set(lista.map((r) => r.idReserva)))
    });
  }

  /** La estancia ya terminó: COMPLETADA, o CONFIRMADA con la fecha de salida ya pasada. */
  protected terminada(reserva: ReservaListado): boolean {
    return reserva.estado === 'COMPLETADA' || (reserva.estado === 'CONFIRMADA' && reserva.fechaSalida < this.hoy);
  }

  protected puedeCancelar(reserva: ReservaListado): boolean {
    return sePuedeCancelar(reserva.estado) && !this.terminada(reserva);
  }

  protected abrirResena(reserva: ReservaListado): void {
    this.exito.set('');
    this.error.set('');
    this.cancelando.set(null);
    this.resenando.set(reserva.idReserva);
  }

  protected guardarResena(reserva: ReservaListado, datos: DatosResena): void {
    this.guardandoResena.set(true);
    this.error.set('');
    this.resenas.crear({ idHuesped: this.idUsuario, idReserva: reserva.idReserva, ...datos }).subscribe({
      next: () => {
        this.guardandoResena.set(false);
        this.resenando.set(null);
        this.conResena.update((ids) => new Set(ids).add(reserva.idReserva));
        this.exito.set('Gracias, publicamos tu reseña.');
      },
      error: (e: HttpErrorResponse) => {
        this.guardandoResena.set(false);
        this.error.set(mensajeDelBack(e));
      }
    });
  }

  protected abrirCancelacion(reserva: ReservaListado): void {
    this.exito.set('');
    this.error.set('');
    this.cancelando.set(reserva.idReserva);
  }

  /** El panel avisa que se canceló: se actualiza el estado en la lista sin volver a consultar. */
  protected alCancelar(respuesta: ReservaCancelada): void {
    this.cancelando.set(null);
    this.reservas.update((lista) =>
      lista.map((r) => (r.idReserva === respuesta.idReserva ? { ...r, estado: respuesta.estado } : r))
    );
    this.exito.set(
      respuesta.estadoDevolucion === 'DEVUELTA'
        ? `Cancelaste la reserva. Se te devolverán ${this.pesos(respuesta.valorDevolucion)}.`
        : 'Cancelaste la reserva. Según la política del alojamiento, no hay devolución.'
    );
  }

  private pesos(valor: number): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor);
  }
}
