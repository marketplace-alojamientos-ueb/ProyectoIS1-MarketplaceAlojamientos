import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CrearPublicacionRequest,
  EditarPublicacionRequest,
  PoliticaCancelacion,
  PublicacionAnfitrion
} from '../models/publicacion-anfitrion.model';

const URL = '/api/publicaciones';

/** Publicaciones del anfitrión que inició sesión (PublicacionController). */
@Injectable({ providedIn: 'root' })
export class PublicacionAnfitrionService {
  private readonly http = inject(HttpClient);

  /** MA-107: todas las publicaciones del anfitrión, con su estado. */
  listar(idAnfitrion: number): Observable<PublicacionAnfitrion[]> {
    return this.http.get<PublicacionAnfitrion[]>(URL, { params: { idAnfitrion } });
  }

  /** MA-108 / MA-109: solo pausa publicaciones ACTIVAS (si no, el back responde 409). */
  pausar(idPublicacion: number, idUsuario: number): Observable<PublicacionAnfitrion> {
    return this.http.post<PublicacionAnfitrion>(`${URL}/${idPublicacion}/pausar`, null, { params: { idUsuario } });
  }

  /** MA-64 / MA-76 / MA-77: queda PENDIENTE_REVISION, o BORRADOR con camposFaltantes si va incompleta. */
  crear(publicacion: CrearPublicacionRequest): Observable<PublicacionAnfitrion> {
    return this.http.post<PublicacionAnfitrion>(URL, publicacion);
  }

  /** MA-64: políticas de cancelación que ofrece el back. */
  listarPoliticas(): Observable<PoliticaCancelacion[]> {
    return this.http.get<PoliticaCancelacion[]>(`${URL}/politicas-cancelacion`);
  }

  /** MA-61: datos para llenar el formulario de edición. */
  obtenerParaEditar(idPublicacion: number, idUsuario: number): Observable<PublicacionAnfitrion> {
    return this.http.get<PublicacionAnfitrion>(`${URL}/${idPublicacion}/edicion`, { params: { idUsuario } });
  }

  /** MA-61: guarda los cambios. El estado no cambia y los periodos no se modifican. */
  editar(idPublicacion: number, publicacion: EditarPublicacionRequest): Observable<PublicacionAnfitrion> {
    return this.http.put<PublicacionAnfitrion>(`${URL}/${idPublicacion}`, publicacion);
  }

  /** MA-61: el back responde 409 si la publicación tiene datos asociados, como reservas. */
  eliminar(idPublicacion: number, idUsuario: number): Observable<void> {
    return this.http.delete<void>(`${URL}/${idPublicacion}`, { params: { idUsuario } });
  }
}