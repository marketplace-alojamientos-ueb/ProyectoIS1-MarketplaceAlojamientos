import { Injectable } from '@angular/core';
import { CuentaService } from './cuenta.service';

/** Llamadas a HuespedController (/api/huespedes). */
@Injectable({ providedIn: 'root' })
export class HuespedService extends CuentaService {
  constructor() {
    super('/api/huespedes');
  }
}
