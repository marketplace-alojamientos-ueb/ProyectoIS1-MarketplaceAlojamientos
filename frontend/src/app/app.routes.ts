import { Routes } from '@angular/router';
import { Registro } from './pages/registro/registro';
import { Login } from './pages/login/login';
import { RecuperarContrasena } from './pages/recuperar-contrasena/recuperar-contrasena';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'registro', component: Registro },
  { path: 'login', component: Login },
  { path: 'recuperar-contrasena', component: RecuperarContrasena },
  { path: '**', redirectTo: 'login' }
];
