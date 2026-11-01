import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Boton } from '../../components/atoms/boton/boton';
import { SelectorRol } from '../../components/molecules/selector-rol/selector-rol';
import { FormularioLogin } from '../../components/organisms/formulario-login/formulario-login';
import { PlantillaAutenticacion } from '../../components/templates/plantilla-autenticacion/plantilla-autenticacion';
import { LoginRequest } from '../../models/login.model';
import { Rol } from '../../models/usuario.model';
import { AnfitrionService } from '../../services/anfitrion.service';
import { CuentaService } from '../../services/cuenta.service';
import { HuespedService } from '../../services/huesped.service';
import { SesionService } from '../../services/sesion.service';
import { UsuarioService } from '../../services/usuario.service';
import { mensajeDelBack } from '../../utils/errores';

@Component({
  selector: 'app-login',
  imports: [PlantillaAutenticacion, SelectorRol, FormularioLogin, Alerta, Boton],
  templateUrl: './login.html'
})
export class Login {
  /** Servicio del back según el tipo de cuenta elegido. */
  private readonly cuentas: Record<Rol, CuentaService> = {
    ANFITRION: inject(AnfitrionService),
    HUESPED: inject(HuespedService)
  };
  private readonly usuarioService = inject(UsuarioService);
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  protected readonly rol = signal<Rol>('ANFITRION');
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  protected readonly exito = signal('');
  /** Credenciales del último intento cuando el back respondió que la cuenta está desactivada. */
  protected readonly cuentaDesactivada = signal<LoginRequest | null>(null);

  protected readonly subtitulo = computed(() =>
    this.rol() === 'ANFITRION'
      ? 'Ingresa como anfitrión para administrar tus alojamientos'
      : 'Ingresa como huésped para encontrar y reservar alojamientos'
  );

  protected iniciarSesion(datos: LoginRequest): void {
    const servicio = this.cuentas[this.rol()];
    this.limpiarMensajes();
    this.cargando.set(true);
    servicio
      .iniciarSesion(datos)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (usuario) => {
          this.sesion.iniciar(usuario);
          this.router.navigate(['/inicio']);
        },
        error: (e: HttpErrorResponse) => {
          this.error.set(mensajeDelBack(e));
          if (e.status === 403 && e.error?.message === 'La cuenta está desactivada') {
            this.cuentaDesactivada.set(datos);
          }
        }
      });
  }

  protected activarCuenta(): void {
    const datos = this.cuentaDesactivada();
    if (!datos) {
      return;
    }
    this.limpiarMensajes();
    this.cargando.set(true);
    this.usuarioService
      .activarCuenta(datos)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (r) => this.exito.set(`${r.mensaje}. Ya puedes iniciar sesión.`),
        error: (e: HttpErrorResponse) => this.error.set(mensajeDelBack(e))
      });
  }

  protected cambiarRol(rol: Rol): void {
    this.rol.set(rol);
    this.limpiarMensajes();
  }

  private limpiarMensajes(): void {
    this.error.set('');
    this.exito.set('');
    this.cuentaDesactivada.set(null);
  }
}
