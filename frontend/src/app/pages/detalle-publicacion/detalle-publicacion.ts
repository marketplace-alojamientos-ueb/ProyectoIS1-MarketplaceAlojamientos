import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Boton } from '../../components/atoms/boton/boton';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { PublicacionService } from '../../services/publicacion.service';

/**
 * Detalle de una publicación para el huésped.
 * Provisional: "Reservar" solo muestra un mensaje; la reserva real llega con su historia.
 */
@Component({
  selector: 'app-detalle-publicacion',
  imports: [PlantillaPrincipal, RouterLink, CurrencyPipe, Alerta, Boton],
  templateUrl: './detalle-publicacion.html',
  styleUrl: './detalle-publicacion.css'
})
export class DetallePublicacion {
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly publicacion = toSignal(inject(PublicacionService).obtener(this.id));
  protected readonly mensaje = signal('');

  protected reservar(): void {
    this.mensaje.set('La reserva aún no está disponible: se conectará cuando exista el back de reservas.');
  }
}