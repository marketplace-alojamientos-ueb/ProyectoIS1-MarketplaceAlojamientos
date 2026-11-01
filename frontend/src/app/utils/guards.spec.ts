import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, RouterStateSnapshot, UrlTree } from '@angular/router';
import { SesionService } from '../services/sesion.service';
import { conSesion, sinSesion } from './guards';

describe('Guards de sesión', () => {
  const ruta = {} as ActivatedRouteSnapshot;
  const estado = {} as RouterStateSnapshot;

  function ejecutar(guard: typeof conSesion): boolean | UrlTree {
    return TestBed.runInInjectionContext(() => guard(ruta, estado)) as boolean | UrlTree;
  }

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('sin sesión: deja entrar al login y manda /inicio al login', () => {
    expect(ejecutar(sinSesion)).toBe(true);
    expect(String(ejecutar(conSesion))).toBe('/login');
  });

  it('con sesión: deja entrar a /inicio y manda el login a /inicio', () => {
    TestBed.inject(SesionService).iniciar({ idUsuario: 1, nombreCompleto: 'Ana', correo: 'a@b.co', rol: 'HUESPED' });
    expect(ejecutar(conSesion)).toBe(true);
    expect(String(ejecutar(sinSesion))).toBe('/inicio');
  });
});
