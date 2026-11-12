import { aISO, nombreMes, periodoCubreDia, periodosSeCruzan, semanasDelMes } from './calendario';

describe('calendario', () => {
  it('convierte fechas a AAAA-MM-DD, el formato que usa el back', () => {
    expect(aISO(new Date(2026, 0, 5))).toBe('2026-01-05');
  });

  it('MA-77: detecta periodos que se cruzan con la misma regla del back', () => {
    expect(periodosSeCruzan('2026-12-01', '2026-12-10', '2026-12-05', '2026-12-15')).toBe(true);
    expect(periodosSeCruzan('2026-12-05', '2026-12-15', '2026-12-01', '2026-12-10')).toBe(true);
    expect(periodosSeCruzan('2026-12-01', '2026-12-05', '2026-12-05', '2026-12-09')).toBe(false);
    expect(periodosSeCruzan('2026-12-01', '2026-12-04', '2026-12-05', '2026-12-09')).toBe(false);
  });

  it('MA-77 / MA-78: un periodo cubre las noches desde el inicio hasta el día antes del fin, como en el back', () => {
    const periodo = { fechaInicio: '2026-12-10', fechaFin: '2026-12-15' };
    expect(periodoCubreDia(periodo, '2026-12-09')).toBe(false);
    expect(periodoCubreDia(periodo, '2026-12-10')).toBe(true);
    expect(periodoCubreDia(periodo, '2026-12-14')).toBe(true);
    expect(periodoCubreDia(periodo, '2026-12-15')).toBe(false);
  });

  it('arma las semanas del mes empezando en lunes', () => {
    const semanas = semanasDelMes(2026, 11);
    expect(semanas[0][0]).toBeNull();
    expect(semanas[0][1]?.fecha).toBe('2026-12-01');
    expect(semanas.every((s) => s.length === 7)).toBe(true);
    expect(semanas.flat().filter(Boolean).length).toBe(31);
  });

  it('nombra el mes en español con mayúscula', () => {
    expect(nombreMes(2026, 11)).toBe('Diciembre 2026');
  });
});
