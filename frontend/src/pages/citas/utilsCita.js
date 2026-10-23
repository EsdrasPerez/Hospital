import { formatearHora, hoyISO } from '../../utils/formato.js'

export const nombreMedico = (m, id) =>
  m ? [m.nombre, m.apellidoPaterno, m.apellidoMaterno].filter(Boolean).join(' ') : `Médico #${id}`

export const nombrePaciente = (p, id) =>
  p ? [p.nombre, p.apellidoPaterno, p.apellidoMaterno].filter(Boolean).join(' ') : `Paciente #${id}`

export const FORM_CITA_VACIO = {
  idPaciente: '',
  idMedico: '',
  fecha: '',
  hora: '',
  motivo: '',
  idEstado: '',
}

export function citaAFormulario(c) {
  return {
    idPaciente: c.idPaciente != null ? String(c.idPaciente) : '',
    idMedico: c.idMedico != null ? String(c.idMedico) : '',
    fecha: c.fecha ?? '',
    hora: c.hora ? formatearHora(c.hora) : '',
    motivo: c.motivo ?? '',
    idEstado: c.idEstado != null ? String(c.idEstado) : '',
  }
}

/** LocalTime del backend acepta 'HH:mm'. activo se omite para no alterar el estado actual. */
export function formularioACita(f) {
  return {
    idPaciente: Number(f.idPaciente),
    idMedico: Number(f.idMedico),
    fecha: f.fecha,
    hora: f.hora,
    motivo: f.motivo.trim() === '' ? null : f.motivo.trim(),
    idEstado: Number(f.idEstado),
  }
}

/**
 * Replica las reglas del backend. `original` es el formulario inicial al editar: la fecha solo
 * se exige "no anterior a hoy" si es una cita nueva o si cambian la fecha o la hora.
 */
export function validarCita(f, original = null) {
  const e = {}
  if (!f.idPaciente) e.idPaciente = 'Seleccione un paciente'
  if (!f.idMedico) e.idMedico = 'Seleccione un médico'
  if (!f.idEstado) e.idEstado = 'Seleccione un estado'
  if (!f.fecha) e.fecha = 'La fecha es obligatoria'
  else if (f.fecha < hoyISO() && (!original || f.fecha !== original.fecha || f.hora !== original.hora)) {
    e.fecha = 'La fecha no puede ser anterior a hoy'
  }
  if (!f.hora) e.hora = 'La hora es obligatoria'
  if (f.motivo.trim().length > 255) e.motivo = 'Máximo 255 caracteres'
  return e
}
