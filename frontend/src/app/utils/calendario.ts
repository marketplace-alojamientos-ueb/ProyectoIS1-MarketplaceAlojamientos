
export function aISO(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}


export function periodosSeCruzan(inicioA: string, finA: string, inicioB: string, finB: string): boolean {
  return inicioA < finB && inicioB < finA;
}

/**
 * Misma regla del back (MA-77 / MA-78): un periodo cubre las noches desde su inicio hasta el día anterior al fin,
 * porque la fecha de fin es el día de salida.
 */
export function periodoCubreDia(periodo: { fechaInicio: string; fechaFin: string }, fecha: string): boolean {
  return periodo.fechaInicio <= fecha && fecha < periodo.fechaFin;
}

export interface DiaCalendario {
  fecha: string;
  numero: number;
}

/** Semanas de un mes (mes de 0 a 11), empezando en lunes. Los huecos antes del día 1 y después del último son null. */
export function semanasDelMes(anio: number, mes: number): (DiaCalendario | null)[][] {
  const totalDias = new Date(anio, mes + 1, 0).getDate();
  const huecosIniciales = (new Date(anio, mes, 1).getDay() + 6) % 7;
  const celdas: (DiaCalendario | null)[] = Array.from({ length: huecosIniciales }, () => null);
  for (let numero = 1; numero <= totalDias; numero++) {
    celdas.push({ fecha: aISO(new Date(anio, mes, numero)), numero });
  }
  while (celdas.length % 7 !== 0) {
    celdas.push(null);
  }
  const semanas: (DiaCalendario | null)[][] = [];
  for (let i = 0; i < celdas.length; i += 7) {
    semanas.push(celdas.slice(i, i + 7));
  }
  return semanas;
}

/** "Diciembre 2026" */
export function nombreMes(anio: number, mes: number): string {
  const nombre = new Date(anio, mes, 1).toLocaleDateString('es-CO', { month: 'long' });
  return `${nombre.charAt(0).toUpperCase()}${nombre.slice(1)} ${anio}`;
}