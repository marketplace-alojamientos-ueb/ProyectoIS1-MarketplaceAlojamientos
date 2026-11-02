import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { SesionService } from '../../../services/sesion.service';
import { BarraNavegacion } from './barra-navegacion';

describe('BarraNavegacion', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ imports: [BarraNavegacion], providers: [provideRouter([])] });
  });

  it('sin sesión ofrece iniciar sesión', () => {
    const fixture = TestBed.createComponent(BarraNavegacion);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Iniciar sesión');
  });

  it('con sesión muestra el nombre y permite cerrarla', () => {
    const sesion = TestBed.inject(SesionService);
    sesion.iniciar({ idUsuario: 3, nombreCompleto: 'Kathe Murcia', correo: 'kathe@correo.com', rol: 'HUESPED' });
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(BarraNavegacion);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Kathe Murcia');

    fixture.componentInstance['cerrarSesion']();
    expect(sesion.usuario()).toBeNull();
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });
});