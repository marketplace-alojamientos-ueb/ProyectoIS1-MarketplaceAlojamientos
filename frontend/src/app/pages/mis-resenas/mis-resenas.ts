import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Estrellas } from '../../components/atoms/estrellas/estrellas';
import { Insignia } from '../../components/atoms/insignia/insignia';
import { FormularioResena } from '../../components/organisms/formulario-resena/formulario-resena';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { DatosResena, Resena } from '../../models/resena.model';
import { ResenaService } from '../../services/resena.service';
import { SesionService } from '../../services/sesion.service';
import { mensajeDelBack } from '../../utils/errores';

/**
 * H-04 (MA-81): reseñas que escribió el huésped, incluidas las ocultas por moderación.
 * Puede editar o eliminar las visibles; las ocultas solo se muestran con su estado (el back responde 409).
 */
@Component({
  selector: 'app-mis-resenas',
  imports: [PlantillaPrincipal, RouterLink, DatePipe, Alerta, Estrellas, Insignia, FormularioResena],
  templateUrl: './mis-resenas.html',
  styleUrl: './mis-resenas.css'
})
export class MisResenas {
  private readonly servicio = inject(ResenaService);
  private readonly idUsuario = inject(SesionService).usuario()?.idUsuario ?? 0;

  protected readonly resenas = signal<Resena[]>([]);
  protected readonly cargado = signal(false);
  /** Reseña que se está editando. */
  protected readonly editando = signal<number | null>(null);
  /** Reseña que espera confirmación para eliminarse. */
  protected readonly porEliminar = signal<number | null>(null);
  protected readonly guardando = signal(false);
  protected readonly exito = signal('');
  protected readonly error = signal('');

  constructor() {
    this.servicio.porHuesped(this.idUsuario).subscribe({
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

  protected editar(resena: Resena): void {
    this.limpiarAvisos();
    this.porEliminar.set(null);
    this.editando.set(resena.idResena);
  }

  protected guardar(resena: Resena, datos: DatosResena): void {
    this.guardando.set(true);
    this.limpiarAvisos();
    this.servicio.actualizar(resena.idResena, { idHuesped: this.idUsuario, ...datos }).subscribe({
      next: (actualizada) => {
        this.guardando.set(false);
        this.editando.set(null);
        this.resenas.update((lista) => lista.map((r) => (r.idResena === actualizada.idResena ? actualizada : r)));
        this.exito.set('Guardamos los cambios de tu reseña.');
      },
      error: (e: HttpErrorResponse) => {
        this.guardando.set(false);
        this.error.set(mensajeDelBack(e));
      }
    });
  }

  /** Se llama después de confirmar en la reseña. */
  protected eliminar(resena: Resena): void {
    this.porEliminar.set(null);
    this.limpiarAvisos();
    this.servicio.eliminar(resena.idResena, this.idUsuario).subscribe({
      next: (respuesta) => {
        this.resenas.update((lista) => lista.filter((r) => r.idResena !== resena.idResena));
        this.exito.set(respuesta.mensaje);
      },
      error: (e: HttpErrorResponse) => this.error.set(mensajeDelBack(e))
    });
  }

  private limpiarAvisos(): void {
    this.exito.set('');
    this.error.set('');
  }
}
