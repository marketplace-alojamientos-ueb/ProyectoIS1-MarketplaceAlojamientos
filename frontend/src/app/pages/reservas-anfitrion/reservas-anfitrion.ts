import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Insignia } from '../../components/atoms/insignia/insignia';
import { CancelarReserva } from '../../components/organisms/cancelar-reserva/cancelar-reserva';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { EstadoReserva, ReservaCancelada, ReservaListado } from '../../models/reserva.model';
import { ReservaService } from '../../services/reserva.service';
import { SesionService } from '../../services/sesion.service';
import { mensajeDelBack } from '../../utils/errores';
import { ESTADOS_RESERVA, sePuedeCancelar } from '../../utils/etiquetas';

/**
 * A-05 (MA-96): reservas de los alojamientos del anfitrión (GET /api/reservas/anfitrion), con filtro por estado.
 * En las pendientes o confirmadas puede cancelar con un motivo; al huésped siempre se le devuelve el total.
 */
@Component({
  selector: 'app-reservas-anfitrion',
  imports: [PlantillaPrincipal, CurrencyPipe, DatePipe, Alerta, Insignia, CancelarReserva],
  templateUrl: './reservas-anfitrion.html',
  styleUrl: './reservas-anfitrion.css'
})
export class ReservasAnfitrion {
  private readonly servicio = inject(ReservaService);
  private readonly idUsuario = inject(SesionService).usuario()?.idUsuario ?? 0;

  protected readonly estados = ESTADOS_RESERVA;
  protected readonly ordenEstados = Object.keys(ESTADOS_RESERVA) as EstadoReserva[];
  protected readonly sePuedeCancelar = sePuedeCancelar;
  protected readonly reservas = signal<ReservaListado[]>([]);
  protected readonly cargado = signal(false);
  protected readonly filtroEstado = signal<EstadoReserva | 'TODOS'>('TODOS');
  /** Reserva con el panel de cancelación abierto. */
  protected readonly cancelando = signal<number | null>(null);
  protected readonly exito = signal('');
  protected readonly error = signal('');

  protected readonly filtradas = computed(() => {
    const estado = this.filtroEstado();
    return estado === 'TODOS' ? this.reservas() : this.reservas().filter((r) => r.estado === estado);
  });

  constructor() {
    this.servicio.delAnfitrion(this.idUsuario).subscribe({
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

  protected filtrar(evento: Event): void {
    this.filtroEstado.set((evento.target as HTMLSelectElement).value as EstadoReserva | 'TODOS');
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
    const valor = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(
      respuesta.valorDevolucion
    );
    this.exito.set(`Cancelaste la reserva. Al huésped se le devolverán ${valor} y se le notificó.`);
  }
}
