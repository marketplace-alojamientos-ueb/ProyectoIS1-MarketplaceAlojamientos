/** Lo que se guarda antes de ir a Stripe para que, al volver, la app sepa de qué alojamiento era el pago. */
export interface PagoPendiente {
  idPublicacion: number;
  tituloPublicacion: string;
}

const CLAVE = 'pagoPendiente';

export function guardarPagoPendiente(pago: PagoPendiente): void {
  try {
    sessionStorage.setItem(CLAVE, JSON.stringify(pago));
  } catch {
    // Sin sessionStorage solo se pierde el enlace para volver al alojamiento.
  }
}

export function leerPagoPendiente(): PagoPendiente | null {
  try {
    const valor = sessionStorage.getItem(CLAVE);
    return valor ? (JSON.parse(valor) as PagoPendiente) : null;
  } catch {
    return null;
  }
}

export function borrarPagoPendiente(): void {
  try {
    sessionStorage.removeItem(CLAVE);
  } catch {
    // Nada que borrar.
  }
}
