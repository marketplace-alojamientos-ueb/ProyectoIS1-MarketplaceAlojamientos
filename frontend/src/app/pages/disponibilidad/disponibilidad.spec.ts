import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { PublicacionAnfitrion } from '../../models/publicacion-anfitrion.model';
import { SesionService } from '../../services/sesion.service';
import { Disponibilidad } from './disponibilidad';

const LISTA = [
  {
    idPublicacion: 1,
    titulo: 'Apartamento',
    estado: 'ACTIVA',
    periodos: [
      { idPeriodo: 2, fechaInicio: '2026-11-13', fechaFin: '2026-11-16', tipo: 'RESERVADO' },
      { idPeriodo: 1, fechaInicio: '2026-11-01', fechaFin: '2027-01-31', tipo: 'DISPONIBLE' },
      { idPeriodo: 3, fechaInicio: '2026-12-24', fechaFin: '2026-12-26', tipo: 'BLOQUEADO' }
    ]
  },
  { idPublicacion: 2, titulo: 'Cabaña', estado: 'PAUSADA', periodos: [] },
  { idPublicacion: 3, titulo: null, estado: 'BORRADOR', periodos: [] }
] as unknown as PublicacionAnfitrion[];

function crear(publicacion: string | null = null) {
  TestBed.resetTestingModule();
  sessionStorage.clear();
  TestBed.configureTestingModule({
    imports: [Disponibilidad],
    providers: [
      provideRouter([]),
      provideHttpClient(),
      provideHttpClientTesting(),
      {
        provide: ActivatedRoute,
        useValue: { snapshot: { queryParamMap: convertToParamMap(publicacion ? { publicacion } : {}) } }
      }
    ]
  });
  TestBed.inject(SesionService).iniciar({ idUsuario: 7, nombreCompleto: 'Ana', correo: 'a@a.co', rol: 'ANFITRION' });
  const http = TestBed.inject(HttpTestingController);
  const fixture = TestBed.createComponent(Disponibilidad);
  fixture.detectChanges();
  const req = http.expectOne('/api/publicaciones?idAnfitrion=7');
  return { fixture, req, pagina: fixture.componentInstance, texto: () => fixture.nativeElement.textContent as string };
}

describe('Disponibilidad', () => {
  it('MA-77 / MA-78: muestra los periodos de la primera publicación, ordenados y con fechas dd/mm/aaaa', () => {
    const { fixture, req, pagina, texto } = crear();
    req.flush(LISTA);
    fixture.detectChanges();
    expect(pagina['publicaciones']().length).toBe(3);
    expect(pagina['idPublicacion']()).toBe(1);
    expect(pagina['periodos']()[0].idPeriodo).toBe(1);
    expect(fixture.nativeElement.querySelectorAll('.periodos tbody tr').length).toBe(3);
    expect(texto()).toContain('01/11/2026');
    expect(texto()).toContain('Sin título');
  });

  it('pinta cada día como el back: el fin del periodo es el día de salida', () => {
    const { req, pagina } = crear();
    req.flush(LISTA);
    expect(pagina['tipoDelDia']('2026-11-14')).toBe('RESERVADO');
    // El 16 es el día de salida de la reserva: esa noche vuelve a estar disponible.
    expect(pagina['tipoDelDia']('2026-11-16')).toBe('DISPONIBLE');
    expect(pagina['tipoDelDia']('2026-12-25')).toBe('BLOQUEADO');
    expect(pagina['tipoDelDia']('2026-12-26')).toBe('DISPONIBLE');
    expect(pagina['tipoDelDia']('2027-01-31')).toBeNull();
  });

  it('abre la publicación que llega en la dirección, cambia de publicación y de mes', () => {
    const { fixture, req, pagina, texto } = crear('2');
    req.flush(LISTA);
    fixture.detectChanges();
    expect(texto()).toContain('Esta publicación aún no tiene periodos');

    const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    select.value = '1';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(pagina['periodos']().length).toBe(3);

    const titulo = pagina['meses']()[0].titulo;
    pagina['moverMes'](1);
    expect(pagina['meses']()[0].titulo).not.toBe(titulo);
  });

  it('avisa si no hay publicaciones o si falla la carga', () => {
    const vacio = crear();
    vacio.req.flush([]);
    vacio.fixture.detectChanges();
    expect(vacio.texto()).toContain('Todavía no tienes publicaciones');

    const conError = crear();
    conError.req.flush(null, { status: 500, statusText: 'Error' });
    conError.fixture.detectChanges();
    expect(conError.texto()).toContain('Ocurrió un error inesperado');
  });
});
