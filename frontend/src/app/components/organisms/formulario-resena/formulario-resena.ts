import { Component, input, OnInit, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatosResena } from '../../../models/resena.model';
import { Boton } from '../../atoms/boton/boton';

const MAX_COMENTARIO = 1000;

/**
 * Calificación de 1 a 5 y comentario opcional (máximo 1000), mismas reglas del back (MA-81).
 * Sirve para editar una reseña y, cuando exista la lista de reservas terminadas, para crearla.
 */
@Component({
  selector: 'app-formulario-resena',
  imports: [ReactiveFormsModule, Boton],
  templateUrl: './formulario-resena.html',
  styleUrl: './formulario-resena.css'
})
export class FormularioResena implements OnInit {
  /** Valores para editar; vacío al crear. */
  readonly inicial = input<DatosResena | null>(null);
  readonly textoBoton = input('Guardar reseña');
  readonly cargando = input(false);
  /** Prefijo para que los ids no se repitan si hay varios formularios en la página. */
  readonly idCampo = input('resena');
  readonly enviar = output<DatosResena>();
  readonly cancelar = output<void>();

  protected readonly opciones = [1, 2, 3, 4, 5];
  protected readonly maxComentario = MAX_COMENTARIO;

  protected readonly form = new FormGroup({
    calificacion: new FormControl<number | null>(null, Validators.required),
    comentario: new FormControl('', { nonNullable: true, validators: Validators.maxLength(MAX_COMENTARIO) })
  });

  ngOnInit(): void {
    const inicial = this.inicial();
    if (inicial) {
      this.form.setValue({ calificacion: inicial.calificacion, comentario: inicial.comentario ?? '' });
    }
  }

  protected errorCalificacion(): string {
    const control = this.form.controls.calificacion;
    return control.touched && control.invalid ? 'Elige una calificación de 1 a 5' : '';
  }

  guardar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.cargando()) {
      return;
    }
    const { calificacion, comentario } = this.form.getRawValue();
    this.enviar.emit({ calificacion: calificacion!, comentario: comentario.trim() || null });
  }
}
