import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Boton } from '../../components/atoms/boton/boton';
import { FormularioRestablecer } from '../../components/organisms/formulario-restablecer/formulario-restablecer';
import { FormularioSolicitarCodigo } from '../../components/organisms/formulario-solicitar-codigo/formulario-solicitar-codigo';
import { PlantillaAutenticacion } from '../../components/templates/plantilla-autenticacion/plantilla-autenticacion';
import { RecuperarContrasenaRequest } from '../../models/recuperacion-contrasena.model';
import { UsuarioService } from '../../services/usuario.service';
import { mensajeDelBack } from '../../utils/errores';

/** Paso 1: pedir el código. Paso 2: código + contraseña nueva. 'listo': contraseña cambiada. */
type Paso = 1 | 2 | 'listo';

@Component({
  selector: 'app-recuperar-contrasena',
  imports: [PlantillaAutenticacion, FormularioSolicitarCodigo, FormularioRestablecer, Alerta, Boton],
  templateUrl: './recuperar-contrasena.html'
})
export class RecuperarContrasena {
  private readonly usuarioService = inject(UsuarioService);
  private readonly router = inject(Router);

  protected readonly paso = signal<Paso>(1);
  protected readonly correo = signal('');
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  /** Mensaje que devuelve el back en cada paso. */
  protected readonly mensaje = signal('');

  protected solicitarCodigo(datos: RecuperarContrasenaRequest): void {
    this.error.set('');
    this.cargando.set(true);
    this.usuarioService
      .solicitarRecuperacion(datos)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (r) => {
          this.mensaje.set(r.mensaje);
          this.correo.set(datos.correo);
          this.paso.set(2);
        },
        error: (e: HttpErrorResponse) => this.error.set(mensajeDelBack(e))
      });
  }

  protected restablecer(datos: { codigo: string; nuevaPassword: string }): void {
    this.error.set('');
    this.cargando.set(true);
    this.usuarioService
      .restablecerContrasena({ correo: this.correo(), ...datos })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (r) => {
          this.mensaje.set(r.mensaje);
          this.paso.set('listo');
        },
        error: (e: HttpErrorResponse) => this.error.set(mensajeDelBack(e))
      });
  }

  protected volverAlPaso1(): void {
    this.error.set('');
    this.paso.set(1);
  }

  protected irALogin(): void {
    void this.router.navigate(['/login']);
  }
}
