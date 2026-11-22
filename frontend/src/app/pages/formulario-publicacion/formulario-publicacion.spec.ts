import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { SesionService } from '../../services/sesion.service';
import { aISO } from '../../utils/calendario';
import { FormularioPublicacion } from './formulario-publicacion';

const POLITICAS = [{ codigo: 'FLEXIBLE', nombre: 'Flexible', descripcion: 'Devolución según condiciones' }];

function crear(id: string | null = null) {
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [FormularioPublicacion],
    providers: [
      provideRouter([]),
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap(id ? { id } : {}) } } }
    ]
  });
  TestBed.inject(SesionService).iniciar({ idUsuario: 7, nombreCompleto: 'Ana', correo: 'a@a.co', rol: 'ANFITRION' });
  const http = TestBed.inject(HttpTestingController);
  const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  const fixture = TestBed.createComponent(FormularioPublicacion);
  fixture.detectChanges();
  http.expectOne('/api/publicaciones/politicas-cancelacion').flush(POLITICAS);
  return { fixture, http, navegar, pagina: fixture.componentInstance, texto: () => fixture.nativeElement.textContent as string };
}

const enDias = (dias: number) => aISO(new Date(Date.now() + dias * 86_400_000));

describe('FormularioPublicacion', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('MA-76: crea una publicación incompleta y el back la deja en borrador', () => {
    const { http, navegar, pagina } = crear();
    pagina['form'].controls.titulo.setValue('  Loft  ');
    pagina['guardar']();
    const req = http.expectOne({ method: 'POST', url: '/api/publicaciones' });
    expect(req.request.body.titulo).toBe('Loft');
    expect(req.request.body.ciudad).toBeNull();
    expect(req.request.body.idAnfitrion).toBe(7);
    req.flush({ estado: 'BORRADOR' });
    expect(navegar).toHaveBeenCalledWith(['/anfitrion/publicaciones'], { queryParams: { aviso: 'borrador' } });
  });

  it('MA-64 / MA-77: crea una publicación completa con periodos', () => {
    const { http, navegar, pagina } = crear();
    pagina['form'].setValue({
      titulo: 'Loft',
      descripcion: 'Iluminado',
      tipoAlojamiento: 'APARTAMENTO',
      capacidad: '2',
      ciudad: 'Bogotá',
      ubicacion: 'Calle 57',
      precioPorNoche: '150000',
      tarifaLimpieza: '',
      reglas: '',
      politicaCancelacion: 'FLEXIBLE',
      modoAceptacion: 'CON_APROBACION'
    });
    pagina['nuevoInicio'].setValue(enDias(5));
    pagina['nuevoFin'].setValue(enDias(10));
    pagina['agregarPeriodo']();
    expect(pagina['pasos']().every((p) => p.completo)).toBe(true);

    pagina['guardar']();
    const req = http.expectOne({ method: 'POST', url: '/api/publicaciones' });
    expect(req.request.body.periodos).toEqual([{ fechaInicio: enDias(5), fechaFin: enDias(10) }]);
    expect(req.request.body.capacidad).toBe(2);
    req.flush({ estado: 'PENDIENTE_REVISION' });
    expect(navegar).toHaveBeenCalledWith(['/anfitrion/publicaciones'], { queryParams: { aviso: 'revision' } });
  });

  it('MA-77: valida los periodos con las mismas reglas del back', () => {
    const { pagina } = crear();
    const probar = (inicio: string, fin: string) => {
      pagina['nuevoInicio'].setValue(inicio);
      pagina['nuevoFin'].setValue(fin);
      pagina['agregarPeriodo']();
      return pagina['errorPeriodo']();
    };
    expect(probar('', '')).toContain('Escoge');
    expect(probar(enDias(-3), enDias(2))).toContain('pasado');
    expect(probar(enDias(5), enDias(5))).toContain('posterior');
    expect(probar(enDias(5), enDias(9))).toBe('');
    expect(probar(enDias(8), enDias(12))).toContain('se cruza');
    expect(probar(enDias(9), enDias(12))).toBe('');
    expect(pagina['periodos']().length).toBe(2);
    pagina['quitarPeriodo'](0);
    expect(pagina['periodos']().length).toBe(1);
    pagina['irA']('seccion-basica');
  });

  it('no envía si hay campos con formato inválido y muestra errores del back', () => {
    const { fixture, http, pagina, texto } = crear();
    pagina['form'].controls.capacidad.setValue('cero');
    pagina['guardar']();
    expect(pagina['mensaje']()).toContain('Revisa los campos');

    pagina['form'].controls.capacidad.setValue('');
    pagina['guardar']();
    http
      .expectOne('/api/publicaciones')
      .flush({ message: 'Solo un anfitrión puede crear publicaciones' }, { status: 403, statusText: 'Forbidden' });
    fixture.detectChanges();
    expect(texto()).toContain('Solo un anfitrión puede crear publicaciones');
  });

  it('MA-61: carga la publicación, exige todos los campos y guarda los cambios', () => {
    const { fixture, http, navegar, pagina, texto } = crear('3');
    http.expectOne('/api/publicaciones/3/edicion?idUsuario=7').flush({
      idPublicacion: 3,
      estado: 'ACTIVA',
      titulo: 'Cabaña',
      descripcion: 'Con chimenea',
      tipoAlojamiento: 'CABANA',
      capacidad: 4,
      ciudad: 'Guatavita',
      ubicacion: 'Vereda El Lago',
      precioPorNoche: 180000,
      tarifaLimpieza: 0,
      reglas: null,
      modoAceptacion: 'INMEDIATA',
      politicaCancelacion: 'FLEXIBLE',
      periodos: [{ idPeriodo: 1, fechaInicio: '2026-12-01', fechaFin: '2026-12-15', tipo: 'DISPONIBLE' }],
      camposFaltantes: []
    });
    fixture.detectChanges();
    expect(texto()).toContain('Editar publicación');
    expect(texto()).toContain('01/12/2026');

    pagina['form'].controls.titulo.setValue('');
    pagina['guardar']();
    expect(pagina['mensaje']()).toContain('Revisa los campos');

    pagina['form'].controls.titulo.setValue('Cabaña renovada');
    pagina['guardar']();
    const req = http.expectOne({ method: 'PUT', url: '/api/publicaciones/3' });
    expect(req.request.body.idUsuario).toBe(7);
    expect(req.request.body.periodos).toBeUndefined();
    req.flush({ estado: 'ACTIVA' });
    expect(navegar).toHaveBeenCalledWith(['/anfitrion/publicaciones'], { queryParams: { aviso: 'editada' } });
  });

  it('avisa si no puede cargar la publicación a editar', () => {
    const { http, pagina } = crear('9');
    http
      .expectOne('/api/publicaciones/9/edicion?idUsuario=7')
      .flush({ message: 'Edición no autorizada' }, { status: 403, statusText: 'Forbidden' });
    expect(pagina['mensaje']()).toBe('Edición no autorizada');
  });
});