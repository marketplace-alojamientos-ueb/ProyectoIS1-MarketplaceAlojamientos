import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PublicacionAnfitrion } from '../models/publicacion-anfitrion.model';

const URL = '/api/publicaciones';


@Injectable({ providedIn: 'root' })
export class PublicacionAnfitrionService {
  private readonly http = inject(HttpClient);


  listar(idAnfitrion: number): Observable<PublicacionAnfitrion[]> {
    return this.http.get<PublicacionAnfitrion[]>(URL, { params: { idAnfitrion } });
  }

  pausar(idPublicacion: number, idUsuario: number): Observable<PublicacionAnfitrion> {
    return this.http.post<PublicacionAnfitrion>(`${URL}/${idPublicacion}/pausar`, null, { params: { idUsuario } });
  }
}