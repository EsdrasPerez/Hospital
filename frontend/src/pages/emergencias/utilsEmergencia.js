import { aInputFechaHora, deInputFechaHora } from '../../utils/formato.js'
import { nombrePaciente } from '../citas/utilsCita.js'

export const FORM_EMERGENCIA_VACIO = {
  idPaciente: '',
  nombreTemporal: '',
  motivo: '',
  signosVitales: '',
  fechaHoraIngreso: '',
  idTriaje: '',
  idMedico: '',
  idHabitacion: '',
}

export function emergenciaAFormulario(e) {
  const id = (v) => (v != null ? String(v) : '')
  return {
    idPaciente: id(e.idPaciente),
    nombreTemporal: e.nombreTemporal ?? '',
    motivo: e.motivo ?? '',
    signosVitales: e.signosVitales ?? '',
    fechaHoraIngreso: aInputFechaHora(e.fechaHoraIngreso),
    idTriaje: id(e.idTriaje),
    idMedico: id(e.idMedico),
    idHabitacion: id(e.idHabitacion),
  }
}

/**
 * Formulario -> EmergenciaDTO. La fecha de alta no se envía: el backend la conserva en el PUT y
 * solo se modifica con PATCH dar-alta. `activo` se omite para no alterarlo.
 */
export function formularioAEmergencia(f) {
  const textoONulo = (v) => (v.trim() === '' ? null : v.trim())
  const numeroONulo = (v) => (v ? Number(v) : null)
  return {
    idPaciente: numeroONulo(f.idPaciente),
    nombreTemporal: textoONulo(f.nombreTemporal),
    motivo: f.motivo.trim(),
    signosVitales: textoONulo(f.signosVitales),
    // Si no se indica al crear, el servidor usa la fecha y hora actuales.
    fechaHoraIngreso: deInputFechaHora(f.fechaHoraIngreso),
    idTriaje: numeroONulo(f.idTriaje),
    idMedico: numeroONulo(f.idMedico),
    idHabitacion: numeroONulo(f.idHabitacion),
  }
}

/** Replica las reglas del backend (EmergenciaServiceImpl) más las de la columna id_triaje NOT NULL. */
export function validarEmergencia(f) {
  const e = {}
  if (!f.motivo.trim()) e.motivo = 'El motivo de atención es obligatorio'
  if (!f.idPaciente && !f.nombreTemporal.trim()) {
    e.idPaciente = 'Seleccione un paciente o indique un nombre temporal'
    e.nombreTemporal = 'Indique un nombre temporal si el paciente no está registrado'
  }
  if (f.nombreTemporal.trim().length > 150) e.nombreTemporal = 'Máximo 150 caracteres'
  if (!f.idTriaje) e.idTriaje = 'Seleccione el nivel de triaje'
  return e
}

/** Estado derivado del ingreso. */
export function estadoEmergencia(e) {
  if (e.activo === false) return { texto: 'Inactiva', tono: 'gris' }
  if (e.fechaHoraAlta) return { texto: 'Con alta', tono: 'verde' }
  return { texto: 'En atención', tono: 'ambar' }
}

export function nombreHabitacion(h, id) {
  return h ? `Habitación ${h.numero}` : `Habitación #${id}`
}

/** Nombre del paciente registrado o el nombre temporal del paciente no identificado. */
export function etiquetaPacienteEmergencia(e, pacientesPorId) {
  if (e.idPaciente) return nombrePaciente(pacientesPorId[e.idPaciente], e.idPaciente)
  return `${e.nombreTemporal ?? 'Sin identificar'} (no identificado)`
}
