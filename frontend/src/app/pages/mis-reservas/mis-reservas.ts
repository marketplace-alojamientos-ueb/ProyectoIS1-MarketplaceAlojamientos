import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Insignia } from '../../components/atoms/insignia/insignia';
import { CancelarReserva } from '../../components/organisms/cancelar-reserva/cancelar-reserva';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { ReservaCancelada, ReservaListado } from '../../models/reserva.model';
import { ReservaService } from '../../services/reserva.service';
import { SesionService } from '../../services/sesion.service';
import { mensajeDelBack } from '../../utils/errores';
import { ESTADOS_RESERVA, sePuedeCancelar } from '../../utils/etiquetas';

/**
 * H-05 (MA-95 / MA-97): reservas del huésped (GET /api/reservas/huesped). En las pendientes o confirmadas puede
 * cancelar: el panel le muestra primero cuánto se le devolvería y le pide el motivo.
 */
@Component({
  selector: 'app-mis-reservas',
  imports: [PlantillaPrincipal, RouterLink, CurrencyPipe, DatePipe, Alerta, Insignia, CancelarReserva],
  templateUrl: './mis-reservas.html',
  styleUrl: './mis-reservas.css'
})
export class MisReservas {
  private readonly servicio = inject(ReservaService);
  private readonly idUsuario = inject(SesionService).usuario()?.idUsuario ?? 0;

  protected readonly estados = ESTADOS_RESERVA;
  protected readonly sePuedeCancelar = sePuedeCancelar;
  protected readonly reservas = signal<ReservaListado[]>([]);
  protected readonly cargado = signal(false);
  /** Reserva con el panel de cancelación abierto. */
  protected readonly cancelando = signal<number | null>(null);
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
