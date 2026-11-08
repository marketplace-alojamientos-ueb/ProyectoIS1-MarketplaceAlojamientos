import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models/usuario.model';
import { SesionService } from '../services/sesion.service';

const INICIO_POR_ROL: Record<Rol, string> = {
  ANFITRION: '/anfitrion/publicaciones',
  HUESPED: '/perfil'
};


export const alInicioDelRol: CanActivateFn = () => {
  const usuario = inject(SesionService).usuario();
  const router = inject(Router);
  return router.createUrlTree([usuario ? INICIO_POR_ROL[usuario.rol] : '/login']);
};


export const soloAnfitrion: CanActivateFn = () => {
  const usuario = inject(SesionService).usuario();
  if (usuario?.rol === 'ANFITRION') {
    return true;
  }
  return inject(Router).createUrlTree([usuario ? INICIO_POR_ROL[usuario.rol] : '/login']);
};