import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { TarjetaPublicacion } from '../../components/molecules/tarjeta-publicacion/tarjeta-publicacion';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { PublicacionService } from '../../services/publicacion.service';

/** Listado de alojamientos para el huésped, con búsqueda por ciudad o título. */
@Component({
  selector: 'app-explorar',
  imports: [PlantillaPrincipal, TarjetaPublicacion, Alerta],
  templateUrl: './explorar.html',
  styleUrl: './explorar.css'
})
export class Explorar {
  private readonly publicaciones = toSignal(inject(PublicacionService).listar(), { initialValue: [] });

  protected readonly busqueda = signal('');

  protected readonly resultados = computed(() => {
    const texto = normalizar(this.busqueda());
    return this.publicaciones().filter(
      (p) => normalizar(p.ciudad).includes(texto) || normalizar(p.titulo).includes(texto)
    );
  });

  protected buscar(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }
}

/** Minúsculas y sin tildes, para que "bogota" encuentre "Bogotá". */
function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replaceAll(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}