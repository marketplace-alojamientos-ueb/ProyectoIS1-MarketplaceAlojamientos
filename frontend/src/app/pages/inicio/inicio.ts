import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Boton } from '../../components/atoms/boton/boton';
import { PlantillaAutenticacion } from '../../components/templates/plantilla-autenticacion/plantilla-autenticacion';
import { SesionService } from '../../services/sesion.service';

/**
 * Pantalla a la que se llega después de iniciar sesión.
 * Provisional: solo saluda y permite cerrar sesión; "Mi perfil" y el resto llegan en otras ramas.
 */
@Component({
  selector: 'app-inicio',
  imports: [PlantillaAutenticacion, Alerta, Boton],
  templateUrl: './inicio.html'
})
export class Inicio {
  private readonly router = inject(Router);
  protected readonly sesion = inject(SesionService);

  protected cerrarSesion(): void {
    this.sesion.cerrar();
    void this.router.navigate(['/login']);
  }
}
