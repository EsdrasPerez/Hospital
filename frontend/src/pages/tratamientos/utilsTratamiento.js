export const FORM_TRATAMIENTO_VACIO = {
  descripcion: '',
  indicaciones: '',
  duracionDias: '',
  fechaInicio: '',
  fechaFin: '',
  idDiagnostico: '',
}

export function tratamientoAFormulario(t) {
  return {
    descripcion: t.descripcion ?? '',
    indicaciones: t.indicaciones ?? '',
    duracionDias: t.duracionDias != null ? String(t.duracionDias) : '',
    fechaInicio: t.fechaInicio ?? '',
    fechaFin: t.fechaFin ?? '',
    idDiagnostico: t.idDiagnostico != null ? String(t.idDiagnostico) : '',
  }
}

export function formularioATratamiento(f) {
  return {
    descripcion: f.descripcion.trim(),
    indicaciones: f.indicaciones.trim() === '' ? null : f.indicaciones.trim(),
    duracionDias: f.duracionDias === '' ? null : Number(f.duracionDias),
    // Si no se indica, el servidor usa la fecha de hoy al crear.
    fechaInicio: f.fechaInicio || null,
    fechaFin: f.fechaFin || null,
    idDiagnostico: Number(f.idDiagnostico),
  }
}

export function validarTratamiento(f) {
  const e = {}
  if (!f.descripcion.trim()) e.descripcion = 'La descripción es obligatoria'
  else if (f.descripcion.trim().length > 255) e.descripcion = 'Máximo 255 caracteres'
  if (!f.idDiagnostico) e.idDiagnostico = 'Seleccione el diagnóstico'
  if (f.duracionDias !== '') {
    const n = Number(f.duracionDias)
    if (!Number.isInteger(n) || n <= 0) e.duracionDias = 'Debe ser un número entero mayor a cero'
  }
  if (f.fechaInicio && f.fechaFin && f.fechaFin < f.fechaInicio) {
    e.fechaFin = 'La fecha de fin no puede ser anterior a la de inicio'
  }
  return e
}
