import { Component, computed, inject, input } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Logo } from '../../atoms/logo/logo';
import { Boton } from '../../atoms/boton/boton';
import { Rol } from '../../../models/usuario.model';
import { SesionService } from '../../../services/sesion.service';

const ENLACES: Record<Rol, { texto: string; ruta: string }[]> = {
  HUESPED: [{ texto: 'Mi perfil', ruta: '/perfil' }],
  ANFITRION: [
    { texto: 'Mis publicaciones', ruta: '/anfitrion/publicaciones' },
    { texto: 'Mi perfil', ruta: '/perfil' }
  ]
};

@Component({
  selector: 'app-barra-navegacion',
  imports: [RouterLink, RouterLinkActive, Logo, Boton],
  templateUrl: './barra-navegacion.html',
  styleUrl: './barra-navegacion.css'
})
export class BarraNavegacion {
  private readonly router = inject(Router);
  protected readonly sesion = inject(SesionService);

  readonly rol = input<Rol>('HUESPED');

  protected readonly enlaces = computed(() => ENLACES[this.rol()]);
  protected readonly inicio = computed(() => this.enlaces()[0].ruta);

  protected cerrarSesion(): void {
    this.sesion.cerrar();
    void this.router.navigate(['/login']);
  }
}