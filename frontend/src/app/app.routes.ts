import { Routes } from '@angular/router';
import { Inicio } from './pages/inicio/inicio';
import { Registro } from './pages/registro/registro';
import { Login } from './pages/login/login';
import { RecuperarContrasena } from './pages/recuperar-contrasena/recuperar-contrasena';
import { conSesion, sinSesion } from './utils/guards';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'registro', component: Registro, canActivate: [sinSesion] },
  { path: 'login', component: Login, canActivate: [sinSesion] },
  { path: 'recuperar-contrasena', component: RecuperarContrasena },
  { path: 'inicio', component: Inicio, canActivate: [conSesion] },
  { path: '**', redirectTo: 'login' }
];
