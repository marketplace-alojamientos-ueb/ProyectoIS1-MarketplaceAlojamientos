import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PublicacionDetalle } from '../../../models/publicacion.model';
import { NavegadorService } from '../../../services/navegador.service';
import { PublicacionService } from '../../../services/publicacion.service';
import { ReservaService } from '../../../services/reserva.service';
import { SesionService } from '../../../services/sesion.service';
import { aISO, nochesEntre } from '../../../utils/calendario';
import { mensajeDelBack } from '../../../utils/errores';
import { guardarPagoPendiente } from '../../../utils/pago-pendiente';
import { mensajeError } from '../../../utils/validaciones';
import { Alerta } from '../../atoms/alerta/alerta';
import { Boton } from '../../atoms/boton/boton';
import { Etiqueta } from '../../atoms/etiqueta/etiqueta';
import { CampoFormulario } from '../../molecules/campo-formulario/campo-formulario';

const ENTERO_POSITIVO = /^[1-9]\d*$/;

/** La salida debe ser posterior a la llegada (misma regla del back). */
function salidaPosterior(grupo: AbstractControl): ValidationErrors | null {
  const { fechaLlegada, fechaSalida } = grupo.value;
  return fechaLlegada && fechaSalida && fechaSalida <= fechaLlegada ? { salidaAntes: true } : null;
}

/**
 * H-03 (MA-102): el huésped elige fechas y huéspedes, ve el total estimado y paga con Stripe en modo de pruebas.
 * Paso 1 del pago: POST /api/reservas/checkout crea la sesión de pago y el huésped sale a la página de Stripe.
 * Al pagar, Stripe lo devuelve a /reservas/pago-exitoso, donde se crea la reserva (página PagoExitoso).
 */
@Component({
  selector: 'app-formulario-reserva',
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe, Alerta, Boton, Etiqueta, CampoFormulario],
  templateUrl: './formulario-reserva.html',
  styleUrl: './formulario-reserva.css'
})
export class FormularioReserva {
  readonly publicacion = input.required<PublicacionDetalle>();

  private readonly servicio = inject(ReservaService);
  private readonly navegador = inject(NavegadorService);
  protected readonly usuario = inject(SesionService).usuario;
  protected readonly hoy = aISO(new Date());

  /** Mientras se crea la sesión de Stripe; evita crear dos si el huésped da doble clic. */
  protected readonly cargando = signal(false);
  protected readonly error = signal('');

  protected readonly form = new FormGroup(
    {
      fechaLlegada: new FormControl('', { nonNullable: true, validators: Validators.required }),
      fechaSalida: new FormControl('', { nonNullable: true, validators: Validators.required }),
      numHuespedes: new FormControl('1', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(ENTERO_POSITIVO)]
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
    this.form.valueChanges.subscribe(() => this.error.set(''));
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

  pagar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.errorLlegada() || this.errorHuespedes() || this.cargando()) {
      return;
    }
    const v = this.form.getRawValue();
    const p = this.publicacion();
    this.cargando.set(true);
    this.error.set('');
    this.servicio
      .iniciarCheckout({
        idHuesped: this.usuario()!.idUsuario,
        idPublicacion: p.idPublicacion,
        fechaLlegada: v.fechaLlegada,
        fechaSalida: v.fechaSalida,
        numHuespedes: Number(v.numHuespedes)
      })
      .subscribe({
        next: (checkout) => {
          // El botón queda deshabilitado mientras el navegador sale a Stripe.
          guardarPagoPendiente({ idPublicacion: p.idPublicacion, tituloPublicacion: p.titulo });
          this.navegador.irA(checkout.urlPago);
        },
        error: (e: HttpErrorResponse) => {
          this.cargando.set(false);
          this.error.set(mensajeDelBack(e));
        }
      });
  }
}
