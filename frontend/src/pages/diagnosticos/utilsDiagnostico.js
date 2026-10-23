import { hoyISO } from '../../utils/formato.js'

export const FORM_DIAGNOSTICO_VACIO = {
  descripcion: '',
  observaciones: '',
  idTipoDiagnostico: '',
  fecha: '',
}

export function diagnosticoAFormulario(d) {
  return {
    descripcion: d.descripcion ?? '',
    observaciones: d.observaciones ?? '',
    idTipoDiagnostico: d.idTipoDiagnostico != null ? String(d.idTipoDiagnostico) : '',
    fecha: d.fecha ?? '',
  }
}

export function formularioADiagnostico(f) {
  return {
    descripcion: f.descripcion.trim(),
    observaciones: f.observaciones.trim() === '' ? null : f.observaciones.trim(),
    idTipoDiagnostico: Number(f.idTipoDiagnostico),
    // Si no se indica, el servidor usa la fecha de hoy al crear.
    fecha: f.fecha || null,
  }
}

export function validarDiagnostico(f) {
  const e = {}
  if (!f.descripcion.trim()) e.descripcion = 'La descripción es obligatoria'
  else if (f.descripcion.trim().length > 255) e.descripcion = 'Máximo 255 caracteres'
  if (!f.idTipoDiagnostico) e.idTipoDiagnostico = 'Seleccione el tipo de diagnóstico'
  if (f.fecha && f.fecha > hoyISO()) e.fecha = 'La fecha no puede ser futura'
  return e
}
