import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Insignia } from '../../components/atoms/insignia/insignia';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { EstadoPublicacion, PublicacionAnfitrion } from '../../models/publicacion-anfitrion.model';
import { PublicacionAnfitrionService } from '../../services/publicacion-anfitrion.service';
import { SesionService } from '../../services/sesion.service';
import { mensajeDelBack } from '../../utils/errores';
import { ESTADOS_PUBLICACION } from '../../utils/etiquetas';


@Component({
  selector: 'app-mis-publicaciones',
  imports: [PlantillaPrincipal, CurrencyPipe, Alerta, Insignia],
  templateUrl: './mis-publicaciones.html',
  styleUrl: './mis-publicaciones.css'
})
export class MisPublicaciones {
  private readonly servicio = inject(PublicacionAnfitrionService);
  private readonly idUsuario = inject(SesionService).usuario()?.idUsuario ?? 0;

  protected readonly estados = ESTADOS_PUBLICACION;
  protected readonly ordenEstados: EstadoPublicacion[] = ['ACTIVA', 'PAUSADA', 'PENDIENTE_REVISION', 'BORRADOR', 'BLOQUEADA'];

  protected readonly publicaciones = signal<PublicacionAnfitrion[]>([]);
  protected readonly cargado = signal(false);
  protected readonly busqueda = signal('');
  protected readonly filtroEstado = signal<EstadoPublicacion | 'TODOS'>('TODOS');

  protected readonly porPausar = signal<number | null>(null);
  protected readonly error = signal('');
  protected readonly exito = signal('');

  protected readonly resumen = computed(() =>
    this.ordenEstados.map((estado) => ({
      estado,
      cantidad: this.publicaciones().filter((p) => p.estado === estado).length
    }))
  );

  protected readonly filtradas = computed(() => {
    const texto = this.busqueda().toLowerCase().trim();
    const estado = this.filtroEstado();
    return this.publicaciones().filter(
      (p) =>
        (estado === 'TODOS' || p.estado === estado) &&
        `${p.titulo ?? ''} ${p.ciudad ?? ''}`.toLowerCase().includes(texto)
    );
  });

  constructor() {
    this.cargar();
  }

  protected buscar(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  protected filtrar(evento: Event): void {
    this.filtroEstado.set((evento.target as HTMLSelectElement).value as EstadoPublicacion | 'TODOS');
  }


  protected pausar(publicacion: PublicacionAnfitrion): void {
    this.porPausar.set(null);
    this.error.set('');
    this.exito.set('');
    this.servicio.pausar(publicacion.idPublicacion, this.idUsuario).subscribe({
      next: () => {
        this.exito.set(`Pausaste "${publicacion.titulo}". Ya no aparece en las búsquedas.`);
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.error.set(mensajeDelBack(e))
    });
  }

  private cargar(): void {
    this.servicio.listar(this.idUsuario).subscribe({
      next: (lista) => {
        this.publicaciones.set(lista);
        this.cargado.set(true);
      },
      error: (e: HttpErrorResponse) => this.error.set(mensajeDelBack(e))
    });
  }
}