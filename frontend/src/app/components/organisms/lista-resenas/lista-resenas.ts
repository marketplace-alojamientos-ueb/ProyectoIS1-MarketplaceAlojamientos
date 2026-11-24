import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Resena } from '../../../models/resena.model';
import { ResenaService } from '../../../services/resena.service';
import { SesionService } from '../../../services/sesion.service';
import { mensajeDelBack } from '../../../utils/errores';
import { Alerta } from '../../atoms/alerta/alerta';
import { Estrellas } from '../../atoms/estrellas/estrellas';

const MAX_MOTIVO = 500;

/**
 * Reseñas visibles de un alojamiento con su promedio (MA-81) y botón "Reportar" en cada una (MA-82).
 * Solo reporta quien tiene sesión y no es el autor; el motivo es obligatorio.
 */
@Component({
  selector: 'app-lista-resenas',
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe, Alerta, Estrellas],
  templateUrl: './lista-resenas.html',
  styleUrl: './lista-resenas.css'
})
export class ListaResenas implements OnInit {
  readonly idPublicacion = input.required<number>();

  private readonly servicio = inject(ResenaService);
  protected readonly usuario = inject(SesionService).usuario;

  protected readonly maxMotivo = MAX_MOTIVO;
  protected readonly resenas = signal<Resena[]>([]);
  protected readonly cargado = signal(false);
  /** Reseña con el formulario de reporte abierto. */
  protected readonly reportando = signal<number | null>(null);
  protected readonly enviando = signal(false);
  protected readonly exito = signal('');
  protected readonly error = signal('');

  protected readonly promedio = computed(() => {
    const lista = this.resenas();
    return lista.length ? lista.reduce((suma, r) => suma + r.calificacion, 0) / lista.length : 0;
  });

  protected readonly motivo = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(MAX_MOTIVO)]
  });

  ngOnInit(): void {
    this.servicio.porPublicacion(this.idPublicacion()).subscribe({
      next: (lista) => {
        this.resenas.set(lista);
        this.cargado.set(true);
      },
      error: (e: HttpErrorResponse) => {
        this.cargado.set(true);
        this.error.set(mensajeDelBack(e));
      }
    });
  }

  /** El autor no puede reportar su propia reseña (el back responde 409). */
  protected puedeReportar(resena: Resena): boolean {
    const usuario = this.usuario();
    return !!usuario && usuario.idUsuario !== resena.idHuesped;
  }

  protected abrirReporte(resena: Resena): void {
    this.motivo.reset('');
    this.exito.set('');
    this.error.set('');
    this.reportando.set(resena.idResena);
  }

  protected errorMotivo(): string {
    return this.motivo.touched && !this.motivo.value.trim() ? 'Escribe el motivo del reporte' : '';
  }

  reportar(resena: Resena): void {
    this.motivo.markAsTouched();
    if (this.errorMotivo() || this.motivo.invalid || this.enviando()) {
      return;
    }
    this.enviando.set(true);
    this.servicio
      .reportar(resena.idResena, { idUsuario: this.usuario()!.idUsuario, motivo: this.motivo.value.trim() })
      .subscribe({
        next: (respuesta) => {
          this.enviando.set(false);
          this.reportando.set(null);
          this.exito.set(respuesta.mensaje);
        },
        error: (e: HttpErrorResponse) => {
          this.enviando.set(false);
          this.error.set(mensajeDelBack(e));
        }
      });
  }
}
