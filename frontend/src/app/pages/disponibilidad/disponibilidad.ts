import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Insignia } from '../../components/atoms/insignia/insignia';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { PublicacionAnfitrion, TipoPeriodo } from '../../models/publicacion-anfitrion.model';
import { PublicacionAnfitrionService } from '../../services/publicacion-anfitrion.service';
import { SesionService } from '../../services/sesion.service';
import { aISO, nombreMes, periodoCubreDia, semanasDelMes } from '../../utils/calendario';
import { mensajeDelBack } from '../../utils/errores';
import { TIPOS_PERIODO } from '../../utils/etiquetas';

/** Si un día cae en varios periodos se muestra el de mayor prioridad. */
const PRIORIDAD: TipoPeriodo[] = ['RESERVADO', 'BLOQUEADO', 'DISPONIBLE'];

/**
 * A-03 (MA-77 / MA-78): calendario con los periodos de cada publicación, tal como los devuelve el back.
 * Solo consulta: el back no tiene endpoints para agregar o quitar periodos después de crear la publicación.
 */
@Component({
  selector: 'app-disponibilidad',
  imports: [PlantillaPrincipal, RouterLink, DatePipe, Alerta, Insignia],
  templateUrl: './disponibilidad.html',
  styleUrl: './disponibilidad.css'
})
export class Disponibilidad {
  protected readonly tipos = TIPOS_PERIODO;
  protected readonly diasSemana = ['L', 'M', 'M', 'J', 'V', 'S', 'D'];
  protected readonly hoy = aISO(new Date());

  protected readonly publicaciones = signal<PublicacionAnfitrion[]>([]);
  protected readonly idPublicacion = signal(Number(inject(ActivatedRoute).snapshot.queryParamMap.get('publicacion')) || 0);
  protected readonly error = signal('');

  protected readonly seleccionada = computed(() =>
    this.publicaciones().find((p) => p.idPublicacion === this.idPublicacion())
  );
  protected readonly periodos = computed(() =>
    [...(this.seleccionada()?.periodos ?? [])].sort((a, b) => a.fechaInicio.localeCompare(b.fechaInicio))
  );

  private readonly mesInicio = signal(new Date(new Date().getFullYear(), new Date().getMonth(), 1));

  protected readonly meses = computed(() =>
    [0, 1].map((desplazamiento) => {
      const fecha = new Date(this.mesInicio().getFullYear(), this.mesInicio().getMonth() + desplazamiento, 1);
      return {
        titulo: nombreMes(fecha.getFullYear(), fecha.getMonth()),
        semanas: semanasDelMes(fecha.getFullYear(), fecha.getMonth())
      };
    })
  );

  constructor() {
    const idUsuario = inject(SesionService).usuario()?.idUsuario ?? 0;
    inject(PublicacionAnfitrionService)
      .listar(idUsuario)
      .subscribe({
        next: (lista) => {
          this.publicaciones.set(lista);
          if (!this.seleccionada() && lista.length) {
            this.idPublicacion.set(lista[0].idPublicacion);
          }
        },
        error: (e: HttpErrorResponse) => this.error.set(mensajeDelBack(e))
      });
  }

  protected cambiarPublicacion(evento: Event): void {
    this.idPublicacion.set(Number((evento.target as HTMLSelectElement).value));
  }

  protected moverMes(cantidad: number): void {
    const actual = this.mesInicio();
    this.mesInicio.set(new Date(actual.getFullYear(), actual.getMonth() + cantidad, 1));
  }

  /** Tipo que se pinta en un día del calendario, o null si no tiene periodo (misma regla de noches del back). */
  protected tipoDelDia(fecha: string): TipoPeriodo | null {
    const tipos = this.periodos()
      .filter((p) => periodoCubreDia(p, fecha))
      .map((p) => p.tipo);
    return PRIORIDAD.find((tipo) => tipos.includes(tipo)) ?? null;
  }
}