import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Boton } from '../../components/atoms/boton/boton';
import { Etiqueta } from '../../components/atoms/etiqueta/etiqueta';
import { TarjetaPublicacion } from '../../components/molecules/tarjeta-publicacion/tarjeta-publicacion';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { FiltrosBusqueda, PublicacionBusqueda } from '../../models/publicacion.model';
import { PublicacionService } from '../../services/publicacion.service';
import { mensajeDelBack } from '../../utils/errores';
import { TIPOS_ALOJAMIENTO } from '../../utils/etiquetas';

const numero = (valor: string) => (valor.trim() ? Number(valor) : undefined);
const texto = (valor: number | undefined) => (valor === undefined ? '' : String(valor));

/**
 * H-01 (MA-99): búsqueda de alojamientos activos con los filtros que acepta GET /api/publicaciones/buscar:
 * ciudad, fechas, huéspedes, tipo, rango de precio y servicios.
 */
@Component({
  selector: 'app-explorar',
  imports: [PlantillaPrincipal, ReactiveFormsModule, TarjetaPublicacion, Alerta, Boton, Etiqueta],
  templateUrl: './explorar.html',
  styleUrl: './explorar.css'
})
export class Explorar {
  private readonly servicio = inject(PublicacionService);

  protected readonly tipos = TIPOS_ALOJAMIENTO;
  protected readonly resultados = signal<PublicacionBusqueda[]>([]);
  protected readonly cargando = signal(false);
  protected readonly buscado = signal(false);
  protected readonly error = signal('');

  protected readonly filtros = new FormGroup({
    ciudad: new FormControl('', { nonNullable: true }),
    llegada: new FormControl('', { nonNullable: true }),
    salida: new FormControl('', { nonNullable: true }),
    capacidad: new FormControl('', { nonNullable: true }),
    tipo: new FormControl('', { nonNullable: true }),
    precioMin: new FormControl('', { nonNullable: true }),
    precioMax: new FormControl('', { nonNullable: true }),
    /** Nombres separados por coma, por ejemplo "WiFi, Piscina". */
    servicios: new FormControl('', { nonNullable: true })
  });

  constructor() {
    // Al volver del detalle se repite la última búsqueda, como pide el back (MA-100).
    const { capacidad, precioMin, precioMax, servicios, ...resto } = this.servicio.ultimosFiltros;
    this.filtros.patchValue({
      ...resto,
      capacidad: texto(capacidad),
      precioMin: texto(precioMin),
      precioMax: texto(precioMax),
      servicios: (servicios ?? []).join(', ')
    });
    this.buscar();
  }

  protected buscar(): void {
    const v = this.filtros.getRawValue();
    const filtros: FiltrosBusqueda = {
      ciudad: v.ciudad.trim(),
      llegada: v.llegada,
      salida: v.salida,
      capacidad: numero(v.capacidad),
      tipo: v.tipo,
      precioMin: numero(v.precioMin),
      precioMax: numero(v.precioMax),
      servicios: v.servicios
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean)
    };
    this.servicio.ultimosFiltros = filtros;
    this.error.set('');
    this.cargando.set(true);
    this.servicio
      .buscar(filtros)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (lista) => {
          this.resultados.set(lista);
          this.buscado.set(true);
        },
        error: (e: HttpErrorResponse) => {
          this.resultados.set([]);
          this.error.set(mensajeDelBack(e));
        }
      });
  }

  protected limpiar(): void {
    this.filtros.reset();
    this.buscar();
  }
}