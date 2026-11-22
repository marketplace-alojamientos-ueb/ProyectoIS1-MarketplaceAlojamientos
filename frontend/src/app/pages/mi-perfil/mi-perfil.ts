import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Boton } from '../../components/atoms/boton/boton';
import { CampoFormulario } from '../../components/molecules/campo-formulario/campo-formulario';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import { Rol, UsuarioResponse } from '../../models/usuario.model';
import { AnfitrionService } from '../../services/anfitrion.service';
import { CuentaService } from '../../services/cuenta.service';
import { HuespedService } from '../../services/huesped.service';
import { SesionService } from '../../services/sesion.service';
import { mensajeDelBack } from '../../utils/errores';
import { mensajeError, validadoresCorreo } from '../../utils/validaciones';


@Component({
  selector: 'app-mi-perfil',
  imports: [PlantillaPrincipal, ReactiveFormsModule, RouterLink, CampoFormulario, Alerta, Boton],
  templateUrl: './mi-perfil.html',
  styleUrl: './mi-perfil.css'
})
export class MiPerfil {
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);
  private readonly usuarioSesion = this.sesion.usuario()!;
  private readonly cuentas: Record<Rol, CuentaService> = {
    ANFITRION: inject(AnfitrionService),
    HUESPED: inject(HuespedService)
  };
  private readonly servicio = this.cuentas[this.usuarioSesion.rol];

  protected readonly rol = this.usuarioSesion.rol;
  protected readonly usuario = signal<UsuarioResponse | null>(null);
  protected readonly cargando = signal(false);
  protected readonly confirmarDesactivar = signal(false);
  protected readonly exito = signal('');
  protected readonly errorBack = signal('');
  protected readonly error = mensajeError;

  protected readonly form = new FormGroup({
    primerNombre: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(50)] }),
    segundoNombre: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(50)] }),
    primerApellido: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(50)]
    }),
    segundoApellido: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(50)]
    }),
    correo: new FormControl('', { nonNullable: true, validators: validadoresCorreo })
  });

  constructor() {
    this.servicio.buscarPorId(this.usuarioSesion.idUsuario).subscribe({
      next: (u) => this.mostrar(u),
      error: (e: HttpErrorResponse) => this.errorBack.set(mensajeDelBack(e))
    });
  }

  protected guardar(): void {
    this.exito.set('');
    this.errorBack.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const datos = this.form.getRawValue();
    this.cargando.set(true);
    this.servicio
      .actualizar(this.usuarioSesion.idUsuario, { ...datos, segundoNombre: datos.segundoNombre.trim() || null })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (u) => {
          this.mostrar(u);
          // La barra muestra el nombre guardado en la sesión.
          this.sesion.iniciar({ ...this.usuarioSesion, nombreCompleto: u.nombreCompleto, correo: u.correo });
          this.exito.set('Guardamos tus datos.');
        },
        error: (e: HttpErrorResponse) => this.errorBack.set(mensajeDelBack(e))
      });
  }

  protected cancelar(): void {
    const u = this.usuario();
    if (u) {
      this.mostrar(u);
    }
  }

  protected desactivar(): void {
    this.errorBack.set('');
    this.servicio.desactivar(this.usuarioSesion.idUsuario).subscribe({
      next: () => {
        this.sesion.cerrar();
        void this.router.navigate(['/login']);
      },
      error: (e: HttpErrorResponse) => {
        this.confirmarDesactivar.set(false);
        this.errorBack.set(mensajeDelBack(e));
      }
    });
  }

  private mostrar(u: UsuarioResponse): void {
    this.usuario.set(u);
    this.form.reset({
      primerNombre: u.primerNombre,
      segundoNombre: u.segundoNombre ?? '',
      primerApellido: u.primerApellido,
      segundoApellido: u.segundoApellido,
      correo: u.correo
    });
  }
}