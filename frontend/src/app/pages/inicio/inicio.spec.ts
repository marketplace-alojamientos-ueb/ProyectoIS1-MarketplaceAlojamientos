import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { SesionService } from '../../services/sesion.service';
import { Inicio } from './inicio';

describe('Inicio', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ imports: [Inicio], providers: [provideRouter([])] });
  });

  it('saluda al usuario y cierra la sesión', () => {
    const sesion = TestBed.inject(SesionService);
    sesion.iniciar({ idUsuario: 7, nombreCompleto: 'Ana Gómez', correo: 'ana@correo.com', rol: 'ANFITRION' });
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(Inicio);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Hola, Ana Gómez');
    expect(fixture.nativeElement.textContent).toContain('Ingresaste como anfitrión');

    fixture.componentInstance['cerrarSesion']();
    expect(sesion.usuario()).toBeNull();
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });
});
