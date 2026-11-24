import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { ReservaConfirmada } from '../../models/reserva.model';
import { ReservaService } from '../../services/reserva.service';
import { SesionService } from '../../services/sesion.service';
import { mensajeDelBack } from '../../utils/errores';
import { borrarPagoPendiente, leerPagoPendiente } from '../../utils/pago-pendiente';

/**
 * H-03 (MA-102), paso 2 del pago con Stripe: Stripe devuelve al huésped a /reservas/pago-exitoso?session_id=cs_test_...
 * Al cargar se llama una sola vez a POST /api/reservas/checkout/{session_id}/confirmar, que verifica el pago con
 * Stripe y crea la reserva. Si el huésped recarga, el back devuelve la misma reserva sin cobrar de nuevo.
 */
@Component({
  selector: 'app-pago-exitoso',
  imports: [PlantillaPrincipal, RouterLink, CurrencyPipe, DatePipe, Alerta],
  templateUrl: './pago-exitoso.html',
  styleUrl: './pago-exitoso.css'
})
export class PagoExitoso {
  protected readonly confirmando = signal(true);
  protected readonly reserva = signal<ReservaConfirmada | null>(null);
  protected readonly error = signal('');
  /** Alojamiento del que venía el pago, para el botón "Volver al alojamiento" si algo falla. */
  protected readonly pagoPendiente = leerPagoPendiente();

  constructor() {
    const idSesion = inject(ActivatedRoute).snapshot.queryParamMap.get('session_id');
    const idHuesped = inject(SesionService).usuario()?.idUsuario ?? 0;
    if (!idSesion) {
      this.confirmando.set(false);
      this.error.set('No encontramos el pago. Vuelve al alojamiento e inténtalo de nuevo.');
      return;
    }
    inject(ReservaService)
      .confirmarCheckout(idSesion, idHuesped)
      .subscribe({
        next: (reserva) => {
          this.confirmando.set(false);
          this.reserva.set(reserva);
          borrarPagoPendiente();
        },
        error: (e: HttpErrorResponse) => {
          this.confirmando.set(false);
          this.error.set(mensajeDelBack(e));
        }
      });
  }
}
