import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Boton } from '../../components/atoms/boton/boton';
import { FormularioRestablecer } from '../../components/organisms/formulario-restablecer/formulario-restablecer';
import { FormularioSolicitarCodigo } from '../../components/organisms/formulario-solicitar-codigo/formulario-solicitar-codigo';
import { PlantillaAutenticacion } from '../../components/templates/plantilla-autenticacion/plantilla-autenticacion';
import { RecuperarContrasenaRequest } from '../../models/recuperacion-contrasena.model';

/** Paso 1: pedir el código. Paso 2: código + contraseña nueva. 'listo': contraseña cambiada. */
type Paso = 1 | 2 | 'listo';

@Component({
  selector: 'app-recuperar-contrasena',
  imports: [PlantillaAutenticacion, FormularioSolicitarCodigo, FormularioRestablecer, Alerta, Boton],
  templateUrl: './recuperar-contrasena.html'
})
export class RecuperarContrasena {
  private readonly router = inject(Router);
  protected readonly paso = signal<Paso>(1);
  protected readonly correo = signal('');

  protected solicitarCodigo(datos: RecuperarContrasenaRequest): void {
    // PROTOTIPO: aún no se llama al back. Al integrar:
    //   usuarioService.solicitarRecuperacion(datos).subscribe(() => pasar al paso 2);
    this.correo.set(datos.correo);
    this.paso.set(2);
  }

  protected restablecer(datos: { codigo: string; nuevaPassword: string }): void {
    // PROTOTIPO: aún no se llama al back. Al integrar:
    //   usuarioService.restablecerContrasena({ correo: this.correo(), ...datos }).subscribe({
    //     next: () => paso 'listo',
    //     error: (e) => mensaje de e.error.message ("El código es inválido o ya expiró")
    //   });
    this.paso.set('listo');
  }

  protected irALogin(): void {
    this.router.navigate(['/login']);
  }
}
