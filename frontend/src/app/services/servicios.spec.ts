import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LoginResponse } from '../models/login.model';
import { RegistroUsuarioRequest } from '../models/usuario.model';
import { AnfitrionService } from './anfitrion.service';
import { HuespedService } from './huesped.service';
import { SesionService } from './sesion.service';
import { UsuarioService } from './usuario.service';

const registro: RegistroUsuarioRequest = {
  cedula: '1012345678',
  primerNombre: 'Ana',
  segundoNombre: null,
  primerApellido: 'Gómez',
  segundoApellido: 'Rojas',
  correo: 'ana@correo.com',
  password: 'Clave1234'
};

const login = { correo: 'ana@correo.com', password: 'Clave1234' };

describe('Servicios HTTP', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  for (const [Servicio, url] of [
    [AnfitrionService, '/api/anfitriones'],
    [HuespedService, '/api/huespedes']
  ] as const) {
    it(`${Servicio.name} llama a ${url}`, () => {
      const servicio = TestBed.inject(Servicio);
      const { password, cedula, ...datos } = registro;

      servicio.listarTodos().subscribe();
      servicio.buscarPorId(7).subscribe();
      servicio.registrar(registro).subscribe();
      servicio.actualizar(7, datos).subscribe();
      servicio.desactivar(7).subscribe();
      servicio.iniciarSesion(login).subscribe();

      http.expectOne({ method: 'GET', url }).flush([]);
      http.expectOne({ method: 'GET', url: `${url}/7` }).flush({});
      expect(http.expectOne({ method: 'POST', url }).request.body).toEqual(registro);
      expect(http.expectOne({ method: 'PUT', url: `${url}/7` }).request.body).toEqual(datos);
      http.expectOne({ method: 'DELETE', url: `${url}/7` }).flush(null);
      expect(http.expectOne({ method: 'POST', url: `${url}/login` }).request.body).toEqual(login);
    });
  }

  it('UsuarioService llama a /api/usuarios', () => {
    const servicio = TestBed.inject(UsuarioService);
    const restablecer = { correo: login.correo, codigo: '123456', nuevaPassword: 'Nueva1234' };

    servicio.solicitarRecuperacion({ correo: login.correo }).subscribe();
    servicio.restablecerContrasena(restablecer).subscribe();
    servicio.activarCuenta(login).subscribe();

    expect(http.expectOne('/api/usuarios/recuperar-contrasena').request.body).toEqual({ correo: login.correo });
    expect(http.expectOne('/api/usuarios/restablecer-contrasena').request.body).toEqual(restablecer);
    expect(http.expectOne('/api/usuarios/activar-cuenta').request.body).toEqual(login);
  });
});

describe('SesionService', () => {
  const usuario: LoginResponse = { idUsuario: 7, nombreCompleto: 'Ana Gómez', correo: 'ana@correo.com', rol: 'ANFITRION' };

  beforeEach(() => sessionStorage.clear());

  it('guarda la sesión y la recupera al recargar', () => {
    TestBed.inject(SesionService).iniciar(usuario);
    expect(sessionStorage.getItem('sesion')).toBe(JSON.stringify(usuario));

    TestBed.resetTestingModule();
    expect(TestBed.inject(SesionService).usuario()).toEqual(usuario);
  });

  it('cierra la sesión', () => {
    const sesion = TestBed.inject(SesionService);
    sesion.iniciar(usuario);
    sesion.cerrar();
    expect(sesion.usuario()).toBeNull();
    expect(sessionStorage.getItem('sesion')).toBeNull();
  });
});
