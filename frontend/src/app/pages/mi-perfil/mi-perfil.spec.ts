import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Rol } from '../../models/usuario.model';
import { SesionService } from '../../services/sesion.service';
import { MiPerfil } from './mi-perfil';

const USUARIO = {
  idUsuario: 7,
  cedula: '1012345678',
  primerNombre: 'Ana',
  segundoNombre: null,
  primerApellido: 'Gómez',
  segundoApellido: 'Rojas',
  nombreCompleto: 'Ana Gómez Rojas',
  correo: 'ana@correo.com',
  estadoCuenta: 'ACTIVA',
  restringido: false
};

function crear(rol: Rol, url: string) {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [MiPerfil],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
  });
  const sesion = TestBed.inject(SesionService);
  sesion.iniciar({ idUsuario: 7, nombreCompleto: 'Ana Gómez Rojas', correo: 'ana@correo.com', rol });
  const http = TestBed.inject(HttpTestingController);
  const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  const fixture = TestBed.createComponent(MiPerfil);
  fixture.detectChanges();
  http.expectOne({ method: 'GET', url: `${url}/7` }).flush(USUARIO);
  fixture.detectChanges();
  return { fixture, http, sesion, navegar, pagina: fixture.componentInstance as any, texto: () => fixture.nativeElement.textContent as string };
}

describe('MiPerfil', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('muestra los datos del anfitrión y guarda los cambios', () => {
    const { fixture, http, sesion, pagina, texto } = crear('ANFITRION', '/api/anfitriones');
    expect(texto()).toContain('1012345678');
    expect(texto()).toContain('Tus publicaciones dejarán de aparecer');
    expect(pagina.form.controls.primerNombre.value).toBe('Ana');

    pagina.form.controls.primerNombre.setValue('');
    pagina.guardar();
    expect(pagina.form.controls.primerNombre.touched).toBe(true);

    pagina.form.controls.primerNombre.setValue('Ana María');
    pagina.guardar();
    const req = http.expectOne({ method: 'PUT', url: '/api/anfitriones/7' });
    expect(req.request.body.segundoNombre).toBeNull();
    req.flush({ ...USUARIO, primerNombre: 'Ana María', nombreCompleto: 'Ana María Gómez Rojas' });
    fixture.detectChanges();
    expect(texto()).toContain('Guardamos tus datos');
    expect(sesion.usuario()?.nombreCompleto).toBe('Ana María Gómez Rojas');

    pagina.form.controls.primerNombre.setValue('Otro');
    pagina.cancelar();
    expect(pagina.form.controls.primerNombre.value).toBe('Ana María');
  });

  it('muestra el error del back al guardar', () => {
    const { http, pagina } = crear('HUESPED', '/api/huespedes');
    pagina.guardar();
    http
      .expectOne({ method: 'PUT', url: '/api/huespedes/7' })
      .flush({ message: 'El correo ya está registrado' }, { status: 409, statusText: 'Conflict' });
    expect(pagina.errorBack()).toBe('El correo ya está registrado');
  });

  it('desactiva la cuenta del huésped después de confirmar', () => {
    const { fixture, http, sesion, navegar, pagina, texto } = crear('HUESPED', '/api/huespedes');
    pagina.confirmarDesactivar.set(true);
    fixture.detectChanges();
    expect(texto()).toContain('¿Seguro que quieres desactivar');

    pagina.desactivar();
    http.expectOne({ method: 'DELETE', url: '/api/huespedes/7' }).flush(null, { status: 500, statusText: 'Error' });
    expect(pagina.confirmarDesactivar()).toBe(false);

    pagina.desactivar();
    http.expectOne({ method: 'DELETE', url: '/api/huespedes/7' }).flush(null);
    expect(sesion.usuario()).toBeNull();
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });

  it('avisa si no puede cargar el perfil', () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [MiPerfil],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    });
    TestBed.inject(SesionService).iniciar({ idUsuario: 7, nombreCompleto: 'Ana', correo: 'a@a.co', rol: 'HUESPED' });
    const fixture = TestBed.createComponent(MiPerfil);
    TestBed.inject(HttpTestingController).expectOne('/api/huespedes/7').flush(null, { status: 404, statusText: 'Not Found' });
    expect((fixture.componentInstance as any).errorBack()).toContain('error inesperado');
  });
});