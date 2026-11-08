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