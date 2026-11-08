import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, UrlTree } from '@angular/router';
import { LoginResponse } from '../models/login.model';
import { SesionService } from '../services/sesion.service';
import { alInicioDelRol } from './guards-rol';

const usuario = (rol: string): LoginResponse =>
  ({ idUsuario: 7, nombreCompleto: 'Ana', correo: 'ana@correo.com', rol }) as LoginResponse;

function correr(rol: string | null): string | boolean {
  sessionStorage.clear();
  TestBed.configureTestingModule({ providers: [provideRouter([])] });
  if (rol) {
    TestBed.inject(SesionService).iniciar(usuario(rol));
  }
  const resultado = TestBed.runInInjectionContext(() => alInicioDelRol({} as never, {} as never));
  return resultado instanceof UrlTree ? TestBed.inject(Router).serializeUrl(resultado) : (resultado as boolean);
}

describe('guards por rol', () => {
  it('alInicioDelRol lleva a cada rol a su pantalla y sin sesión al login', () => {
    expect(correr('ANFITRION')).toBe('/perfil');
    TestBed.resetTestingModule();
    expect(correr('HUESPED')).toBe('/perfil');
    TestBed.resetTestingModule();
    expect(correr(null)).toBe('/login');
  });
});