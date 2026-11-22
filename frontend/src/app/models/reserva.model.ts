/** Igual a los DTO de reservas del back (ReservaController: MA-60, MA-102, MA-103). */

export type EstadoReserva = 'PENDIENTE' | 'CONFIRMADA' | 'RECHAZADA' | 'CANCELADA' | 'COMPLETADA' | 'EN_REVISION_MANUAL';

/** POST /api/reservas/confirmar (ReservaConfirmarRequestDTO). Fechas AAAA-MM-DD. */
export interface ReservaConfirmarRequest {
  idHuesped: number;
  idPublicacion: number;
  fechaLlegada: string;
  fechaSalida: string;
  numHuespedes: number;
  /** UUID que el front genera una vez por intento: si la petición llega dos veces, el back no cobra dos veces. */
  claveIdempotencia: string;
  /** 16 dígitos. Las que terminan en 0002 se rechazan (pago simulado). */
  numeroTarjeta: string;
}

/** Respuesta 201 de POST /api/reservas/confirmar (ReservaConfirmadaDTO). */
export interface ReservaConfirmada {
  idReserva: number;
  estado: EstadoReserva;
  fechaLlegada: string;
  fechaSalida: string;
  numHuespedes: number;
  idPublicacion: number;
  tituloPublicacion: string;
  noches: number;
  precioBase: number;
  tarifaLimpieza: number;
  total: number;
  idTransaccion: string;
  mensaje: string;
}

/** GET /api/reservas/pendientes?idAnfitrion= (ReservaPendienteDTO). */
export interface ReservaPendiente {
  idReserva: number;
  estado: EstadoReserva;
  fechaLlegada: string;
  fechaSalida: string;
  numHuespedes: number;
  idPublicacion: number;
  tituloPublicacion: string;
  idHuesped: number;
  nombreHuesped: string;
}
