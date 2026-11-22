import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { SesionService } from '../../services/sesion.service';
import { RecuperarContrasena } from './recuperar-contrasena';

function configurar(): void {
  TestBed.configureTestingModule({
    imports: [RecuperarContrasena],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
  });
}

describe('RecuperarContrasena', () => {
  beforeEach(() => sessionStorage.clear());

  it('sin sesión deja el correo vacío', () => {
    configurar();
    const fixture = TestBed.createComponent(RecuperarContrasena);
    fixture.detectChanges();
    const campo: HTMLInputElement = fixture.nativeElement.querySelector('input');
    expect(campo.value).toBe('');
  });

  it('con sesión (desde Mi perfil) trae el correo ya escrito', () => {
    configurar();
    TestBed.inject(SesionService).iniciar({ idUsuario: 7, nombreCompleto: 'Ana', correo: 'ana@correo.com', rol: 'HUESPED' });
    const fixture = TestBed.createComponent(RecuperarContrasena);
    fixture.detectChanges();
    const campo: HTMLInputElement = fixture.nativeElement.querySelector('input');
    expect(campo.value).toBe('ana@correo.com');
  });
});