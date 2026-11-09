import { aISO, periodosSeCruzan } from './calendario';

describe('calendario', () => {
  it('convierte fechas a AAAA-MM-DD, el formato que usa el back', () => {
    expect(aISO(new Date(2026, 0, 5))).toBe('2026-01-05');
  });

  it('MA-77: detecta periodos que se cruzan con la misma regla del back', () => {
    expect(periodosSeCruzan('2026-12-01', '2026-12-10', '2026-12-05', '2026-12-15')).toBe(true);
    expect(periodosSeCruzan('2026-12-05', '2026-12-15', '2026-12-01', '2026-12-10')).toBe(true);
    // El fin es el día de salida: un periodo puede empezar el mismo día en que termina otro.
    expect(periodosSeCruzan('2026-12-01', '2026-12-05', '2026-12-05', '2026-12-09')).toBe(false);
    expect(periodosSeCruzan('2026-12-01', '2026-12-04', '2026-12-05', '2026-12-09')).toBe(false);
  });
});
