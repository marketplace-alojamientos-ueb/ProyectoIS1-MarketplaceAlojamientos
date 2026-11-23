import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { ReservaPendiente } from '../../models/reserva.model';
import { ReservaService } from '../../services/reserva.service';
import { SesionService } from '../../services/sesion.service';
import { nochesEntre } from '../../utils/calendario';
import { mensajeDelBack } from '../../utils/errores';

/**
 * A-04 (MA-60): reservas en espera de aprobación de los alojamientos del anfitrión (GET /api/reservas/pendientes).
 * Aceptar la deja CONFIRMADA y reserva las fechas; rechazar la deja RECHAZADA. En ambos casos sale de la lista.
 */
@Component({
  selector: 'app-reservas-pendientes',
  imports: [PlantillaPrincipal, DatePipe, Alerta],
  templateUrl: './reservas-pendientes.html',
  styleUrl: './reservas-pendientes.css'
})
export class ReservasPendientes {
  private readonly servicio = inject(ReservaService);
  private readonly idUsuario = inject(SesionService).usuario()?.idUsuario ?? 0;

  protected readonly reservas = signal<ReservaPendiente[]>([]);
  protected readonly cargado = signal(false);
  /** Reserva que espera confirmación para rechazarse. */
  protected readonly porRechazar = signal<number | null>(null);
  /** Reserva que se está procesando, para deshabilitar sus botones. */
  protected readonly procesando = signal<number | null>(null);
  protected readonly exito = signal('');
  protected readonly error = signal('');
  protected readonly noches = nochesEntre;

  constructor() {
    this.cargar();
  }

  protected aceptar(reserva: ReservaPendiente): void {
    this.decidir(reserva, this.servicio.aceptar(reserva.idReserva, this.idUsuario));
  }

  /** Se llama después de confirmar en la fila. */
  protected rechazar(reserva: ReservaPendiente): void {
    this.decidir(reserva, this.servicio.rechazar(reserva.idReserva, this.idUsuario));
  }

  private decidir(reserva: ReservaPendiente, peticion: ReturnType<ReservaService['aceptar']>): void {
    this.porRechazar.set(null);
    this.procesando.set(reserva.idReserva);
    this.exito.set('');
    this.error.set('');
    peticion.subscribe({
      next: (respuesta) => {
        this.procesando.set(null);
        this.reservas.update((lista) => lista.filter((r) => r.idReserva !== reserva.idReserva));
        this.exito.set(respuesta.mensaje);
      },
      error: (e: HttpErrorResponse) => {
        this.procesando.set(null);
        this.error.set(mensajeDelBack(e));
        // 409: la reserva ya no estaba pendiente o las fechas se ocuparon; se recarga para ver el estado real.
        if (e.status === 409) {
          this.cargar();
        }
      }
    });
  }

  private cargar(): void {
    this.servicio.pendientes(this.idUsuario).subscribe({
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
}
