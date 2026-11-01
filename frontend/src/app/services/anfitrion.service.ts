import { Injectable } from '@angular/core';
import { CuentaService } from './cuenta.service';

/** Llamadas a AnfitrionController (/api/anfitriones). */
@Injectable({ providedIn: 'root' })
export class AnfitrionService extends CuentaService {
  constructor() {
    super('/api/anfitriones');
  }
}
