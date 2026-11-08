import { TestBed } from '@angular/core/testing';
import { CanActivateFn, provideRouter, Router, UrlTree } from '@angular/router';
import { LoginResponse } from '../models/login.model';
import { SesionService } from '../services/sesion.service';
import { alInicioDelRol, soloAnfitrion } from './guards-rol';

const usuario = (rol: string): LoginResponse =>
  ({ idUsuario: 7, nombreCompleto: 'Ana', correo: 'ana@correo.com', rol }) as LoginResponse;

function correr(guard: CanActivateFn, rol: string | null): string | boolean {
  TestBed.resetTestingModule();
  sessionStorage.clear();
  TestBed.configureTestingModule({ providers: [provideRouter([])] });
  if (rol) {
    TestBed.inject(SesionService).iniciar(usuario(rol));
  }
  const resultado = TestBed.runInInjectionContext(() => guard({} as never, {} as never));
  return resultado instanceof UrlTree ? TestBed.inject(Router).serializeUrl(resultado) : (resultado as boolean);
}

describe('guards por rol', () => {
  it('alInicioDelRol lleva al anfitrión a sus publicaciones, al huésped a su perfil y sin sesión al login', () => {
    expect(correr(alInicioDelRol, 'ANFITRION')).toBe('/anfitrion/publicaciones');
    expect(correr(alInicioDelRol, 'HUESPED')).toBe('/perfil');
    expect(correr(alInicioDelRol, null)).toBe('/login');
  });

  it('soloAnfitrion deja pasar al anfitrión y devuelve a los demás a su inicio', () => {
    expect(correr(soloAnfitrion, 'ANFITRION')).toBe(true);
    expect(correr(soloAnfitrion, 'HUESPED')).toBe('/perfil');
    expect(correr(soloAnfitrion, null)).toBe('/login');
  });
});