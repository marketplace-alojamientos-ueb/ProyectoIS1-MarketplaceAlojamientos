import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Boton } from '../../components/atoms/boton/boton';
import { FormularioRegistro } from '../../components/organisms/formulario-registro/formulario-registro';
import { PlantillaAutenticacion } from '../../components/templates/plantilla-autenticacion/plantilla-autenticacion';
import { AnfitrionRequest } from '../../models/anfitrion.model';

@Component({
  selector: 'app-registro',
  imports: [PlantillaAutenticacion, FormularioRegistro, Alerta, Boton],
  templateUrl: './registro.html'
})
export class Registro {
  private readonly router = inject(Router);
  /** Nombre del anfitrión registrado; vacío mientras se llena el formulario. */
  protected readonly registrado = signal('');

  protected registrar(datos: AnfitrionRequest): void {
    // PROTOTIPO: aún no se llama al back. Al integrar:
    //   anfitrionService.registrar(datos).subscribe({
    //     next: (r) => mostrar éxito con r.nombreCompleto,
    //     error: (e) => mensaje de e.error.message (400 / 409 cédula o correo repetidos)
    //   });
    this.registrado.set(`${datos.primerNombre} ${datos.primerApellido}`);
  }

  protected irALogin(): void {
    this.router.navigate(['/login']);
  }
}
