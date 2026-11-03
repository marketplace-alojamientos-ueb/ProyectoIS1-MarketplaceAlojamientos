import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RegistroUsuarioRequest } from '../../models/usuario.model';
import { AnfitrionService } from '../../services/anfitrion.service';
import { HuespedService } from '../../services/huesped.service';
import { Registro } from './registro';

const datos: RegistroUsuarioRequest = {
  cedula: '1012345678',
  primerNombre: 'Laura',
  segundoNombre: null,
  primerApellido: 'Gómez',
  segundoApellido: 'Pérez',
  correo: 'laura@correo.com',
  password: 'Clave1234'
};

describe('Registro', () => {
  const anfitrion = { registrar: vi.fn() };
  const huesped = { registrar: vi.fn() };

  function crear() {
    TestBed.configureTestingModule({
      imports: [Registro],
      providers: [
        provideRouter([]),
        { provide: AnfitrionService, useValue: anfitrion },
        { provide: HuespedService, useValue: huesped }
      ]
    });
    const fixture = TestBed.createComponent(Registro);
    fixture.detectChanges();
    return { fixture, registro: fixture.componentInstance };
  }

  beforeEach(() => vi.resetAllMocks());

  it('registra un huésped y ofrece ir al login', () => {
    huesped.registrar.mockReturnValue(of({ nombreCompleto: 'Laura Gómez Pérez' }));
    const { fixture, registro } = crear();
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    registro['cambiarRol']('HUESPED');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Regístrate como huésped');

    registro['registrar'](datos);
    fixture.detectChanges();
    expect(huesped.registrar).toHaveBeenCalledWith(datos);
    expect(fixture.nativeElement.textContent).toContain('¡Listo, Laura Gómez Pérez!');

    registro['irALogin']();
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });

  it('muestra el error si la cédula o el correo ya existen', () => {
    anfitrion.registrar.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 409, error: { message: 'No se pudo completar el registro: la cédula 1012345678 ya está registrada en otra cuenta. Si es tuya, inicia sesión o recupera tu contraseña.' } }))
    );
    const { fixture, registro } = crear();

    registro['registrar'](datos);
    fixture.detectChanges();

    expect(anfitrion.registrar).toHaveBeenCalledWith(datos);
    expect(fixture.nativeElement.textContent).toContain('No se pudo completar el registro: la cédula 1012345678 ya está registrada en otra cuenta. Si es tuya, inicia sesión o recupera tu contraseña.');
    expect(registro['cargando']()).toBe(false);
  });
});
