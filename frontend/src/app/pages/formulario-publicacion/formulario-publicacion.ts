import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Alerta } from '../../components/atoms/alerta/alerta';
import { Boton } from '../../components/atoms/boton/boton';
import { Etiqueta } from '../../components/atoms/etiqueta/etiqueta';
import { Insignia } from '../../components/atoms/insignia/insignia';
import { CampoFormulario } from '../../components/molecules/campo-formulario/campo-formulario';
import { PlantillaPrincipal } from '../../components/templates/plantilla-principal/plantilla-principal';
import {
  DatosPublicacion,
  ModoAceptacion,
  Periodo,
  PeriodoDisponibilidad,
  PublicacionAnfitrion
} from '../../models/publicacion-anfitrion.model';
import { PublicacionAnfitrionService } from '../../services/publicacion-anfitrion.service';
import { SesionService } from '../../services/sesion.service';
import { aISO, periodosSeCruzan } from '../../utils/calendario';
import { mensajeDelBack } from '../../utils/errores';
import { TIPOS_ALOJAMIENTO, TIPOS_PERIODO } from '../../utils/etiquetas';
import { mensajeError } from '../../utils/validaciones';

const ENTERO_POSITIVO = /^[1-9]\d*$/;
const ENTERO = /^\d+$/;

/** Campos que el back exige para que la publicación no quede en BORRADOR (y todos al editar). */
const OBLIGATORIOS = [
  'titulo',
  'descripcion',
  'tipoAlojamiento',
  'capacidad',
  'ciudad',
  'ubicacion',
  'precioPorNoche',
  'politicaCancelacion'
] as const;

/**
 * A-02: crear (MA-64, MA-76, MA-77) o editar (MA-61) una publicación.
 * Al crear se puede enviar incompleta: el back la deja en BORRADOR y dice qué falta.
 * Al editar el back exige todos los campos y no modifica los periodos.
 */
@Component({
  selector: 'app-formulario-publicacion',
  imports: [PlantillaPrincipal, ReactiveFormsModule, RouterLink, DatePipe, CampoFormulario, Etiqueta, Alerta, Boton, Insignia],
  templateUrl: './formulario-publicacion.html',
  styleUrl: './formulario-publicacion.css'
})
export class FormularioPublicacion {
  private readonly servicio = inject(PublicacionAnfitrionService);
  private readonly router = inject(Router);
  private readonly idUsuario = inject(SesionService).usuario()?.idUsuario ?? 0;
  private readonly idPublicacion = Number(inject(ActivatedRoute).snapshot.paramMap.get('id')) || undefined;

  protected readonly editando = this.idPublicacion !== undefined;
  protected readonly tipos = TIPOS_ALOJAMIENTO;
  protected readonly tiposPeriodo = TIPOS_PERIODO;
  protected readonly hoy = aISO(new Date());
  protected readonly politicas = toSignal(this.servicio.listarPoliticas(), { initialValue: [] });
  protected readonly error = mensajeError;

  protected readonly form = new FormGroup({
    titulo: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(52)] }),
    descripcion: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    tipoAlojamiento: new FormControl('', { nonNullable: true }),
    capacidad: new FormControl('', { nonNullable: true, validators: [Validators.pattern(ENTERO_POSITIVO)] }),
    ciudad: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(50)] }),
    ubicacion: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(100)] }),
    precioPorNoche: new FormControl('', { nonNullable: true, validators: [Validators.pattern(ENTERO_POSITIVO)] }),
    tarifaLimpieza: new FormControl('', { nonNullable: true, validators: [Validators.pattern(ENTERO)] }),
    reglas: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(1000)] }),
    politicaCancelacion: new FormControl('', { nonNullable: true }),
    modoAceptacion: new FormControl<ModoAceptacion>('INMEDIATA', { nonNullable: true })
  });

  /** Para agregar periodos (solo al crear). */
  protected readonly nuevoInicio = new FormControl('', { nonNullable: true });
  protected readonly nuevoFin = new FormControl('', { nonNullable: true });
  protected readonly periodos = signal<Periodo[]>([]);
  protected readonly periodosGuardados = signal<PeriodoDisponibilidad[]>([]);
  protected readonly errorPeriodo = signal('');

  protected readonly mensaje = signal('');
  protected readonly cargando = signal(false);

  private readonly valores = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  /** Pasos del menú lateral; cada uno se marca cuando su sección está completa. */
  protected readonly pasos = computed(() => {
    const v = this.valores();
    const lleno = (...campos: (string | undefined)[]) => campos.every((c) => !!c?.trim()) && this.form.valid;
    const pasos = [
      {
        id: 'seccion-basica',
        texto: 'Información básica',
        completo: lleno(v.titulo, v.descripcion, v.tipoAlojamiento, v.capacidad, v.ciudad, v.ubicacion)
      },
      { id: 'seccion-precio', texto: 'Precio y tarifas', completo: lleno(v.precioPorNoche) },
      { id: 'seccion-reglas', texto: 'Reglas y políticas', completo: lleno(v.politicaCancelacion) }
    ];
    if (!this.editando) {
      pasos.push({ id: 'seccion-periodos', texto: 'Disponibilidad', completo: this.periodos().length > 0 });
    }
    return pasos;
  });

  protected readonly largoTitulo = computed(() => this.valores().titulo?.length ?? 0);

  constructor() {
    if (this.idPublicacion !== undefined) {
      for (const campo of OBLIGATORIOS) {
        this.form.controls[campo].addValidators(Validators.required);
      }
      this.servicio.obtenerParaEditar(this.idPublicacion, this.idUsuario).subscribe({
        next: (p) => this.cargar(p),
        error: (e: HttpErrorResponse) => this.mensaje.set(mensajeDelBack(e))
      });
    }
  }

  protected irA(id: string): void {
    document.getElementById(id)?.scrollIntoView?.({ behavior: 'smooth' });
  }

  /** Mismas reglas que valida el back (MA-77): fin posterior al inicio, sin fechas pasadas y sin cruces entre periodos. */
  protected agregarPeriodo(): void {
    const fechaInicio = this.nuevoInicio.value;
    const fechaFin = this.nuevoFin.value;
    let problema = '';
    if (!fechaInicio || !fechaFin) {
      problema = 'Escoge la fecha de inicio y la de fin';
    } else if (fechaInicio < this.hoy) {
      problema = 'La fecha de inicio no puede estar en el pasado';
    } else if (fechaFin <= fechaInicio) {
      problema = 'La fecha de fin debe ser posterior a la de inicio';
    } else if (this.periodos().some((p) => periodosSeCruzan(fechaInicio, fechaFin, p.fechaInicio, p.fechaFin))) {
      problema = 'Ese periodo se cruza con otro que ya agregaste';
    }
    this.errorPeriodo.set(problema);
    if (problema) {
      return;
    }
    this.periodos.update((lista) => [...lista, { fechaInicio, fechaFin }].sort((a, b) => a.fechaInicio.localeCompare(b.fechaInicio)));
    this.nuevoInicio.reset();
    this.nuevoFin.reset();
  }

  protected quitarPeriodo(indice: number): void {
    this.periodos.update((lista) => lista.filter((_, i) => i !== indice));
  }

  protected guardar(): void {
    this.form.markAllAsTouched();
    this.mensaje.set('');
    if (this.form.invalid) {
      this.mensaje.set('Revisa los campos marcados en rojo.');
      return;
    }
    this.cargando.set(true);
    const peticion =
      this.idPublicacion === undefined
        ? this.servicio.crear({ ...this.datos(), idAnfitrion: this.idUsuario, periodos: this.periodos() })
        : this.servicio.editar(this.idPublicacion, { ...this.datos(), idUsuario: this.idUsuario });
    peticion.pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: (p) => {
        const aviso = this.editando ? 'editada' : p.estado === 'BORRADOR' ? 'borrador' : 'revision';
        void this.router.navigate(['/anfitrion/publicaciones'], { queryParams: { aviso } });
      },
      error: (e: HttpErrorResponse) => this.mensaje.set(mensajeDelBack(e))
    });
  }

  /** Los campos vacíos van como null para que el back los cuente como faltantes. */
  private datos(): DatosPublicacion {
    const v = this.form.getRawValue();
    const texto = (valor: string) => valor.trim() || null;
    return {
      titulo: texto(v.titulo),
      descripcion: texto(v.descripcion),
      ubicacion: texto(v.ubicacion),
      ciudad: texto(v.ciudad),
      tipoAlojamiento: texto(v.tipoAlojamiento),
      capacidad: v.capacidad ? Number(v.capacidad) : null,
      precioPorNoche: v.precioPorNoche ? Number(v.precioPorNoche) : null,
      tarifaLimpieza: Number(v.tarifaLimpieza) || 0,
      reglas: texto(v.reglas),
      modoAceptacion: v.modoAceptacion,
      politicaCancelacion: texto(v.politicaCancelacion)
    };
  }

  private cargar(p: PublicacionAnfitrion): void {
    const texto = (valor: string | number | null) => (valor === null || valor === undefined ? '' : String(valor));
    this.form.setValue({
      titulo: texto(p.titulo),
      descripcion: texto(p.descripcion),
      tipoAlojamiento: texto(p.tipoAlojamiento),
      capacidad: texto(p.capacidad),
      ciudad: texto(p.ciudad),
      ubicacion: texto(p.ubicacion),
      precioPorNoche: texto(p.precioPorNoche),
      tarifaLimpieza: texto(p.tarifaLimpieza),
      reglas: texto(p.reglas),
      politicaCancelacion: texto(p.politicaCancelacion),
      modoAceptacion: p.modoAceptacion ?? 'INMEDIATA'
    });
    this.periodosGuardados.set(p.periodos ?? []);
  }
}