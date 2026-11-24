import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ReservaCancelada, SimulacionCancelacion } from '../../../models/reserva.model';
import { ReservaService } from '../../../services/reserva.service';
import { SesionService } from '../../../services/sesion.service';
import { mensajeDelBack } from '../../../utils/errores';
import { Alerta } from '../../atoms/alerta/alerta';
import { Boton } from '../../atoms/boton/boton';
import { Etiqueta } from '../../atoms/etiqueta/etiqueta';

const MAX_MOTIVO = 500;

/**
 * Panel para cancelar una reserva, para el huésped (MA-95 / MA-97) o el anfitrión (MA-96).
 * Al huésped primero le muestra cuánto se le devolvería (simulación); al anfitrión le avisa que se devuelve el total.
 * Pide el motivo y, al confirmar, avisa al padre con la respuesta del back.
 */
@Component({
  selector: 'app-cancelar-reserva',
  imports: [ReactiveFormsModule, CurrencyPipe, Alerta, Boton, Etiqueta],
  templateUrl: './cancelar-reserva.html',
  styleUrl: './cancelar-reserva.css'
})
export class CancelarReserva implements OnInit {
  readonly idReserva = input.required<number>();
  readonly rol = input<'HUESPED' | 'ANFITRION'>('HUESPED');
  /** La reserva quedó cancelada; trae la devolución. */
  readonly cancelada = output<ReservaCancelada>();
  /** El usuario decidió no cancelar. */
  readonly volver = output<void>();

  private readonly servicio = inject(ReservaService);
  private readonly idUsuario = inject(SesionService).usuario()?.idUsuario ?? 0;

  protected readonly maxMotivo = MAX_MOTIVO;
  protected readonly simulacion = signal<SimulacionCancelacion | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  /** Si el back dice que ya no se puede cancelar (403/409), no se deja confirmar. */
  protected readonly bloqueada = signal(false);

  protected readonly motivo = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(MAX_MOTIVO)]
  });

  ngOnInit(): void {
    if (this.rol() === 'HUESPED') {
      this.servicio.simularCancelacion(this.idReserva(), this.idUsuario).subscribe({
        next: (s) => this.simulacion.set(s),
        error: (e: HttpErrorResponse) => this.bloquear(e)
      });
    }
  }

  protected errorMotivo(): string {
    if (!this.motivo.touched) {
      return '';
    }
    if (this.motivo.hasError('required') || !this.motivo.value.trim()) {
      return 'Escribe el motivo de la cancelación';
    }
    if (this.motivo.hasError('maxlength')) {
      return `El motivo no puede tener más de ${MAX_MOTIVO} caracteres`;
    }
    return '';
  }

  confirmar(): void {
    this.motivo.markAsTouched();
    if (this.errorMotivo() || this.cargando() || this.bloqueada()) {
      return;
    }
    const motivo = this.motivo.value.trim();
    const peticion =
      this.rol() === 'HUESPED'
        ? this.servicio.cancelar(this.idReserva(), { idHuesped: this.idUsuario, motivo })
        : this.servicio.cancelarComoAnfitrion(this.idReserva(), { idAnfitrion: this.idUsuario, motivo });
    this.cargando.set(true);
    this.error.set('');
    peticion.subscribe({
      next: (respuesta) => {
        this.cargando.set(false);
        this.cancelada.emit(respuesta);
      },
      error: (e: HttpErrorResponse) => {
        this.cargando.set(false);
        this.bloquear(e);
      }
    });
  }

  private bloquear(e: HttpErrorResponse): void {
    this.error.set(mensajeDelBack(e));
    if (e.status === 403 || e.status === 409) {
      this.bloqueada.set(true);
    }
  }
}
