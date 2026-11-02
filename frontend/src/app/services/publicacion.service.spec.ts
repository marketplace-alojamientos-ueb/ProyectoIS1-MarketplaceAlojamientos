import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { PublicacionService } from './publicacion.service';

describe('PublicacionService', () => {
  let servicio: PublicacionService;

  beforeEach(() => {
    servicio = TestBed.inject(PublicacionService);
  });

  it('lista las publicaciones de ejemplo', async () => {
    const publicaciones = await firstValueFrom(servicio.listar());
    expect(publicaciones.length).toBeGreaterThan(0);
  });

  it('obtiene una publicación por su id', async () => {
    const publicacion = await firstValueFrom(servicio.obtener(1));
    expect(publicacion?.idPublicacion).toBe(1);
  });

  it('devuelve undefined si la publicación no existe', async () => {
    expect(await firstValueFrom(servicio.obtener(999))).toBeUndefined();
  });
});