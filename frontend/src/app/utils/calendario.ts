
export function aISO(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}


export function periodosSeCruzan(inicioA: string, finA: string, inicioB: string, finB: string): boolean {
  return inicioA < finB && inicioB < finA;
}