import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { PublicacionDetalle } from '../../models/publicacion.model';
import { PublicacionAnfitrionService } from '../../services/publicacion-anfitrion.service';
import { PublicacionService } from '../../services/publicacion.service';
import { mensajeDelBack } from '../../utils/errores';
import { MODOS_ACEPTACION, textoTipo } from '../../utils/etiquetas';

/**
 * H-02 (MA-100): detalle de un alojamiento activo (GET /api/publicaciones/{id}/detalle), sin datos del anfitrión.
 * La cotización y la reserva se agregan cuando existan en el back.
 */
@Component({
  selector: 'app-detalle-publicacion',
  imports: [PlantillaPrincipal, RouterLink, CurrencyPipe, Alerta],
  templateUrl: './detalle-publicacion.html',
  styleUrl: './detalle-publicacion.css'
})
export class DetallePublicacion {
  protected readonly publicacion = signal<PublicacionDetalle | null>(null);
  protected readonly error = signal('');
  protected readonly textoTipo = textoTipo;
  protected readonly modos = MODOS_ACEPTACION;

  /** El detalle trae el código de la política; su nombre y descripción vienen de las políticas del back. */
  private readonly politicas = toSignal(inject(PublicacionAnfitrionService).listarPoliticas(), { initialValue: [] });
  protected readonly politica = computed(() => {
    const codigo = this.publicacion()?.politicaCancelacion;
    return this.politicas().find((p) => p.codigo === codigo) ?? { codigo, nombre: codigo, descripcion: '' };
  });

  constructor() {
    const id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
    inject(PublicacionService)
      .detalle(id)
      .subscribe({
        next: (p) => this.publicacion.set(p),
        error: (e: HttpErrorResponse) => this.error.set(mensajeDelBack(e))
      });
  }
}