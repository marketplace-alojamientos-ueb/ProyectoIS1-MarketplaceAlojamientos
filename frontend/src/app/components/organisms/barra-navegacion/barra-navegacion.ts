import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Logo } from '../../atoms/logo/logo';
import { Boton } from '../../atoms/boton/boton';
import { SesionService } from '../../../services/sesion.service';

/** Barra superior de las pantallas del huésped: logo, menú y sesión. */
@Component({
  selector: 'app-barra-navegacion',
  imports: [RouterLink, RouterLinkActive, Logo, Boton],
  templateUrl: './barra-navegacion.html',
  styleUrl: './barra-navegacion.css'
})
export class BarraNavegacion {
  private readonly router = inject(Router);
  protected readonly sesion = inject(SesionService);

  protected cerrarSesion(): void {
    this.sesion.cerrar();
    void this.router.navigate(['/login']);
  }
}