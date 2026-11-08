import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models/usuario.model';
import { SesionService } from '../services/sesion.service';

/** Pantalla después de iniciar sesión. */
const INICIO_POR_ROL: Record<Rol, string> = {
  ANFITRION: '/perfil',
  HUESPED: '/perfil'
};

/** Después del login lleva a la pantalla principal de cada rol. Sin sesión, al login. */
export const alInicioDelRol: CanActivateFn = () => {
  const usuario = inject(SesionService).usuario();
  const router = inject(Router);
  return router.createUrlTree([usuario ? INICIO_POR_ROL[usuario.rol] : '/login']);
};