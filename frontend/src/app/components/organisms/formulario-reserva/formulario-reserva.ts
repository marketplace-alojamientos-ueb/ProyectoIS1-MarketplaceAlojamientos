import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { PublicacionDetalle } from '../../../models/publicacion.model';
import { ReservaConfirmada } from '../../../models/reserva.model';
import { PublicacionService } from '../../../services/publicacion.service';
import { ReservaService } from '../../../services/reserva.service';
import { SesionService } from '../../../services/sesion.service';
import { aISO, nochesEntre } from '../../../utils/calendario';
import { mensajeDelBack } from '../../../utils/errores';
import { mensajeError } from '../../../utils/validaciones';
import { Alerta } from '../../atoms/alerta/alerta';
import { Boton } from '../../atoms/boton/boton';
import { Etiqueta } from '../../atoms/etiqueta/etiqueta';
import { CampoFormulario } from '../../molecules/campo-formulario/campo-formulario';

const ENTERO_POSITIVO = /^[1-9]\d*$/;
const TARJETA = /^\d{16}$/;

/** La salida debe ser posterior a la llegada (misma regla del back). */
function salidaPosterior(grupo: AbstractControl): ValidationErrors | null {
  const { fechaLlegada, fechaSalida } = grupo.value;
  return fechaLlegada && fechaSalida && fechaSalida <= fechaLlegada ? { salidaAntes: true } : null;
}

/**
 * H-03 (MA-102 / MA-103): el huésped elige fechas y huéspedes, ve el total estimado y confirma con pago simulado
 * (POST /api/reservas/confirmar). El total real lo calcula el back y llega en la respuesta.
 * La clave de idempotencia se genera una vez por intento: si el huésped cambia algún dato, es un intento nuevo.
 */
@Component({
  selector: 'app-formulario-reserva',
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe, DatePipe, Alerta, Boton, Etiqueta, CampoFormulario],
  templateUrl: './formulario-reserva.html',
  styleUrl: './formulario-reserva.css'
})
export class FormularioReserva {
  readonly publicacion = input.required<PublicacionDetalle>();

  private readonly servicio = inject(ReservaService);
  protected readonly usuario = inject(SesionService).usuario;
  protected readonly hoy = aISO(new Date());

  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  protected readonly resultado = signal<ReservaConfirmada | null>(null);
  private clave = crypto.randomUUID();

  protected readonly form = new FormGroup(
    {
      fechaLlegada: new FormControl('', { nonNullable: true, validators: Validators.required }),
      fechaSalida: new FormControl('', { nonNullable: true, validators: Validators.required }),
      numHuespedes: new FormControl('1', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(ENTERO_POSITIVO)]
      }),
      numeroTarjeta: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(TARJETA)]
      })
    },
    { validators: salidaPosterior }
  );

  private readonly valores = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  protected readonly noches = computed(() => nochesEntre(this.valores().fechaLlegada ?? '', this.valores().fechaSalida ?? ''));
  protected readonly totalEstimado = computed(() => {
    const p = this.publicacion();
    return this.noches() * p.precioPorNoche + (p.tarifaLimpieza ?? 0);
  });

  constructor() {
    // Si el huésped llegó desde la búsqueda, se precargan sus fechas y número de personas.
    const filtros = inject(PublicacionService).ultimosFiltros;
    this.form.patchValue({
      fechaLlegada: filtros.llegada ?? '',
      fechaSalida: filtros.salida ?? '',
      numHuespedes: filtros.capacidad ? String(filtros.capacidad) : '1'
    });
    // Cualquier cambio en los datos es un intento de reserva distinto.
    this.form.valueChanges.subscribe(() => {
      this.clave = crypto.randomUUID();
      this.error.set('');
    });
  }

  protected errorCampo(control: AbstractControl, mensajePatron?: string): string {
    return mensajeError(control, mensajePatron);
  }

  protected errorLlegada(): string {
    const control = this.form.controls.fechaLlegada;
    if (control.touched && control.value && control.value < this.hoy) {
      return 'La llegada no puede ser una fecha pasada';
    }
    return mensajeError(control);
  }

  protected errorSalida(): string {
    const control = this.form.controls.fechaSalida;
    if (control.touched && this.form.hasError('salidaAntes')) {
      return 'La salida debe ser posterior a la llegada';
    }
    return mensajeError(control);
  }

  protected errorHuespedes(): string {
    const control = this.form.controls.numHuespedes;
    const capacidad = this.publicacion().capacidad;
    if (control.touched && ENTERO_POSITIVO.test(control.value) && Number(control.value) > capacidad) {
      return `Este alojamiento admite máximo ${capacidad} huéspedes`;
    }
    return mensajeError(control, 'Escribe un número mayor que 0');
  }

  confirmar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.errorLlegada() || this.errorHuespedes() || this.cargando()) {
      return;
    }
    const v = this.form.getRawValue();
    this.cargando.set(true);
    this.error.set('');
    this.servicio
      .confirmar({
        idHuesped: this.usuario()!.idUsuario,
        idPublicacion: this.publicacion().idPublicacion,
        fechaLlegada: v.fechaLlegada,
        fechaSalida: v.fechaSalida,
        numHuespedes: Number(v.numHuespedes),
        claveIdempotencia: this.clave,
        numeroTarjeta: v.numeroTarjeta
      })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (reserva) => this.resultado.set(reserva),
        error: (e: HttpErrorResponse) => this.error.set(mensajeDelBack(e))
      });
  }
}
