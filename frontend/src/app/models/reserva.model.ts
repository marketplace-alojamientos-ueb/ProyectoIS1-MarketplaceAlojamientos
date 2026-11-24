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

/** POST /api/reservas/checkout (ReservaCheckoutRequestDTO): igual que confirmar, sin tarjeta ni clave. */
export interface ReservaCheckoutRequest {
  idHuesped: number;
  idPublicacion: number;
  fechaLlegada: string;
  fechaSalida: string;
  numHuespedes: number;
}

/** Respuesta 201 de POST /api/reservas/checkout (ReservaCheckoutDTO): a urlPago se manda al huésped a pagar. */
export interface ReservaCheckout {
  idSesion: string;
  urlPago: string;
  idPublicacion: number;
  tituloPublicacion: string;
  noches: number;
  precioBase: number;
  tarifaLimpieza: number;
  total: number;
}

/** Respuesta 201 de POST /api/reservas/confirmar y de /checkout/{idSesion}/confirmar (ReservaConfirmadaDTO). */
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

/** Resultado de la devolución al cancelar (EstadoDevolucion del back). */
export type EstadoDevolucion = 'DEVUELTA' | 'SIN_DEVOLUCION';

/** Cuerpo de POST /api/reservas/{id}/cancelar (ReservaCancelarRequestDTO). Motivo obligatorio, máximo 500. */
export interface ReservaCancelarRequest {
  idHuesped: number;
  motivo: string;
}

/** Cuerpo de POST /api/reservas/{id}/cancelar-anfitrion (ReservaCancelarAnfitrionRequestDTO). */
export interface ReservaCancelarAnfitrionRequest {
  idAnfitrion: number;
  motivo: string;
}

/** Respuesta 200 de las dos cancelaciones (ReservaCanceladaDTO). */
export interface ReservaCancelada {
  idReserva: number;
  estado: EstadoReserva;
  valorDevolucion: number;
  estadoDevolucion: EstadoDevolucion;
  mensaje: string;
}

/** GET /api/reservas/{id}/cancelacion/simulacion?idHuesped= (SimulacionCancelacionDTO). Solo consulta. */
export interface SimulacionCancelacion {
  idReserva: number;
  estado: EstadoReserva;
  politica: string;
  valorTotal: number;
  valorDevolucion: number;
  aplicaDevolucion: boolean;
  mensaje: string;
}

/** Una reserva en "Mis reservas" (huésped) o en las reservas del anfitrión (ReservaListadoDTO). */
export interface ReservaListado {
  idReserva: number;
  estado: EstadoReserva;
  fechaLlegada: string;
  fechaSalida: string;
  numHuespedes: number;
  total: number;
  idPublicacion: number;
  tituloPublicacion: string;
  idHuesped: number;
  nombreHuesped: string;
  idAnfitrion: number;
  nombreAnfitrion: string;
}
