import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginResponse } from '../../models/login.model';
import { AnfitrionService } from '../../services/anfitrion.service';
import { HuespedService } from '../../services/huesped.service';
import { SesionService } from '../../services/sesion.service';
import { UsuarioService } from '../../services/usuario.service';
import { Login } from './login';

const credenciales = { correo: 'ana@correo.com', password: 'Clave1234' };
const usuario: LoginResponse = { idUsuario: 7, nombreCompleto: 'Ana Gómez', correo: credenciales.correo, rol: 'HUESPED' };

describe('Login', () => {
  const anfitrion = { iniciarSesion: vi.fn() };
  const huesped = { iniciarSesion: vi.fn() };
  const usuarios = { activarCuenta: vi.fn() };

  function crear() {
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([]),
        { provide: AnfitrionService, useValue: anfitrion },
        { provide: HuespedService, useValue: huesped },
        { provide: UsuarioService, useValue: usuarios }
      ]
    });
    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();
    return { fixture, login: fixture.componentInstance };
  }

  beforeEach(() => {
    vi.resetAllMocks();
    sessionStorage.clear();
  });

  it('inicia sesión como huésped, guarda la sesión y va a /inicio', () => {
    huesped.iniciarSesion.mockReturnValue(of(usuario));
    const { login } = crear();
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    login['cambiarRol']('HUESPED');
    login['iniciarSesion'](credenciales);

    expect(huesped.iniciarSesion).toHaveBeenCalledWith(credenciales);
    expect(anfitrion.iniciarSesion).not.toHaveBeenCalled();
    expect(TestBed.inject(SesionService).usuario()).toEqual(usuario);
    expect(navegar).toHaveBeenCalledWith(['/inicio']);
  });

  it('muestra el error del back si las credenciales son incorrectas', () => {
    anfitrion.iniciarSesion.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 401, error: { message: 'Correo o contraseña incorrectos' } }))
    );
    const { fixture, login } = crear();

    login['iniciarSesion'](credenciales);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Correo o contraseña incorrectos');
    expect(login['cuentaDesactivada']()).toBeNull();
    expect(login['cargando']()).toBe(false);
  });

  it('permite activar la cuenta cuando está desactivada', () => {
    anfitrion.iniciarSesion.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 403, error: { message: 'La cuenta está desactivada' } }))
    );
    usuarios.activarCuenta.mockReturnValue(of({ mensaje: 'Cuenta activada correctamente' }));
    const { fixture, login } = crear();

    login['iniciarSesion'](credenciales);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Activar mi cuenta');

    login['activarCuenta']();
    fixture.detectChanges();

    expect(usuarios.activarCuenta).toHaveBeenCalledWith(credenciales);
    expect(fixture.nativeElement.textContent).toContain('Cuenta activada correctamente. Ya puedes iniciar sesión.');
  });

  it('muestra el error si no se pudo activar la cuenta', () => {
    anfitrion.iniciarSesion.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 403, error: { message: 'La cuenta está desactivada' } }))
    );
    usuarios.activarCuenta.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 409, error: { message: 'La cuenta ya está activa' } }))
    );
    const { login } = crear();

    login['activarCuenta']();
    expect(usuarios.activarCuenta).not.toHaveBeenCalled();

    login['iniciarSesion'](credenciales);
    login['activarCuenta']();
    expect(login['error']()).toBe('La cuenta ya está activa');
  });
});
