import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Boton } from '../../components/atoms/boton/boton';
import { SelectorRol } from '../../components/molecules/selector-rol/selector-rol';
import { FormularioRegistro } from '../../components/organisms/formulario-registro/formulario-registro';
import { PlantillaAutenticacion } from '../../components/templates/plantilla-autenticacion/plantilla-autenticacion';
import { RegistroUsuarioRequest, Rol } from '../../models/usuario.model';
import { AnfitrionService } from '../../services/anfitrion.service';
import { CuentaService } from '../../services/cuenta.service';
import { HuespedService } from '../../services/huesped.service';
import { mensajeDelBack } from '../../utils/errores';

@Component({
  selector: 'app-registro',
  imports: [PlantillaAutenticacion, SelectorRol, FormularioRegistro, Alerta, Boton],
  templateUrl: './registro.html'
})
export class Registro {
  /** Servicio del back según el tipo de cuenta elegido. */
  private readonly cuentas: Record<Rol, CuentaService> = {
    ANFITRION: inject(AnfitrionService),
    HUESPED: inject(HuespedService)
  };
  private readonly router = inject(Router);

  protected readonly rol = signal<Rol>('ANFITRION');
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  /** Nombre del usuario registrado; vacío mientras se llena el formulario. */
  protected readonly registrado = signal('');

  protected readonly titulo = computed(() =>
    this.rol() === 'ANFITRION' ? 'Regístrate como anfitrión' : 'Regístrate como huésped'
  );
  protected readonly subtitulo = computed(() =>
    this.rol() === 'ANFITRION'
      ? 'Crea tu cuenta para empezar a publicar alojamientos'
      : 'Crea tu cuenta para reservar alojamientos'
  );

  protected registrar(datos: RegistroUsuarioRequest): void {
    const servicio = this.cuentas[this.rol()];
    this.error.set('');
    this.cargando.set(true);
    servicio
      .registrar(datos)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (usuario) => this.registrado.set(usuario.nombreCompleto),
        error: (e: HttpErrorResponse) => this.error.set(mensajeDelBack(e))
      });
  }

  protected cambiarRol(rol: Rol): void {
    this.rol.set(rol);
    this.error.set('');
  }

  protected irALogin(): void {
    void this.router.navigate(['/login']);
  }
}
