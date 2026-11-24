import { TestBed } from '@angular/core/testing';
import { DatosResena } from '../../../models/resena.model';
import { FormularioResena } from './formulario-resena';

function crear(inicial: DatosResena | null = null) {
  TestBed.configureTestingModule({ imports: [FormularioResena] });
  const fixture = TestBed.createComponent(FormularioResena);
  fixture.componentRef.setInput('inicial', inicial);
  const enviados: DatosResena[] = [];
  fixture.componentInstance.enviar.subscribe((d) => enviados.push(d));
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;
  const enviar = () => {
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  };
  return { fixture, el, enviar, enviados };
}

describe('FormularioResena', () => {
  it('exige la calificación antes de enviar', () => {
    const { enviar, enviados, el } = crear();
    enviar();
    expect(el.textContent).toContain('Elige una calificación de 1 a 5');
    expect(enviados.length).toBe(0);
  });

  it('envía la calificación elegida y deja el comentario vacío como null', () => {
    const { el, enviar, enviados } = crear();
    el.querySelectorAll<HTMLInputElement>('input[type="radio"]')[3].click();
    enviar();
    expect(enviados).toEqual([{ calificacion: 4, comentario: null }]);
  });

  it('al editar carga los valores actuales', () => {
    const { el, enviar, enviados } = crear({ calificacion: 2, comentario: 'Regular' });
    expect(el.querySelectorAll<HTMLInputElement>('input[type="radio"]')[1].checked).toBe(true);
    expect(el.querySelector('textarea')!.value).toBe('Regular');
    enviar();
    expect(enviados).toEqual([{ calificacion: 2, comentario: 'Regular' }]);
  });
});
