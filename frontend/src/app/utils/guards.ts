import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SesionService } from '../services/sesion.service';

/** Deja pasar solo si hay sesión iniciada; si no, manda al login. */
export const conSesion: CanActivateFn = () =>
  inject(SesionService).usuario() ? true : inject(Router).createUrlTree(['/login']);

/** Login y registro: si ya hay sesión, manda al inicio. */
export const sinSesion: CanActivateFn = () =>
  inject(SesionService).usuario() ? inject(Router).createUrlTree(['/inicio']) : true;
