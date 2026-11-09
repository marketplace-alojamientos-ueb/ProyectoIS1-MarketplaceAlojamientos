import { Routes } from '@angular/router';
import { Inicio } from './pages/inicio/inicio';
import { Registro } from './pages/registro/registro';
import { Login } from './pages/login/login';
import { RecuperarContrasena } from './pages/recuperar-contrasena/recuperar-contrasena';
import { MiPerfil } from './pages/mi-perfil/mi-perfil';
import { MisPublicaciones } from './pages/mis-publicaciones/mis-publicaciones';
import { FormularioPublicacion } from './pages/formulario-publicacion/formulario-publicacion';
import { Disponibilidad } from './pages/disponibilidad/disponibilidad';
import { conSesion, sinSesion } from './utils/guards';
import { alInicioDelRol, soloAnfitrion } from './utils/guards-rol';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'registro', component: Registro, canActivate: [sinSesion] },
  { path: 'login', component: Login, canActivate: [sinSesion] },
  { path: 'recuperar-contrasena', component: RecuperarContrasena },
  { path: 'inicio', component: Inicio, canActivate: [alInicioDelRol] },
  { path: 'perfil', component: MiPerfil, canActivate: [conSesion] },
  { path: 'anfitrion/publicaciones', component: MisPublicaciones, canActivate: [soloAnfitrion] },
  { path: 'anfitrion/publicaciones/nueva', component: FormularioPublicacion, canActivate: [soloAnfitrion] },
  { path: 'anfitrion/publicaciones/:id/editar', component: FormularioPublicacion, canActivate: [soloAnfitrion] },
  { path: 'anfitrion/disponibilidad', component: Disponibilidad, canActivate: [soloAnfitrion] },
  { path: '**', redirectTo: 'login' }
];