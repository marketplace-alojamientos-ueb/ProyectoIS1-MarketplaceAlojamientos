import { Component, signal } from '@angular/core';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { FormularioLogin } from '../../components/organisms/formulario-login/formulario-login';
import { PlantillaAutenticacion } from '../../components/templates/plantilla-autenticacion/plantilla-autenticacion';
import { LoginRequest } from '../../models/login.model';

@Component({
  selector: 'app-login',
  imports: [PlantillaAutenticacion, FormularioLogin, Alerta],
  templateUrl: './login.html'
})
export class Login {
  protected readonly mensaje = signal('');

  protected iniciarSesion(datos: LoginRequest): void {
    // PROTOTIPO: aún no se llama al back. Al integrar:
    //   anfitrionService.iniciarSesion(datos).subscribe({
    //     next: (r) => { sessionStorage.setItem('idUsuario', String(r.idUsuario)); router.navigate([...]); },
    //     error: (e) => mensaje de e.error.message (401 / 403)
    //   });
    this.mensaje.set(`Prototipo: aquí se iniciaría sesión con ${datos.correo}.`);
  }
}
