import { aInputFechaHora, deInputFechaHora } from '../../utils/formato.js'

// El backend fija el id 1 como "Activa" (HospitalizacionesServiceImpl.ESTADO_HOSP_ACTIVA).
export const ESTADO_HOSP_ACTIVA = 1

export const estaActiva = (h) => h.idEstadoHosp === ESTADO_HOSP_ACTIVA

export const nombreHabitacion = (h, id) => (h ? `Habitación ${h.numero}` : `Habitación #${id}`)

export const FORM_HOSP_VACIO = {
  idPaciente: '',
  idMedicoResponsable: '',
  idHabitacion: '',
  idClinica: '',
  fechaIngreso: '',
  motivoIngreso: '',
  diagnosticoIngreso: '',
}

export function hospitalizacionAFormulario(h) {
  const id = (v) => (v != null ? String(v) : '')
  return {
    idPaciente: id(h.idPaciente),
    idMedicoResponsable: id(h.idMedicoResponsable),
    idHabitacion: id(h.idHabitacion),
    idClinica: id(h.idClinica),
    fechaIngreso: aInputFechaHora(h.fechaIngreso),
    motivoIngreso: h.motivoIngreso ?? '',
    diagnosticoIngreso: h.diagnosticoIngreso ?? '',
  }
}

/**
 * Formulario -> HospitalizacionesDTO. El estado y `activo` los fija el servidor; en edición el
 * backend ignora la fecha de ingreso.
 */
export function formularioAHospitalizacion(f) {
  const numeroONulo = (v) => (v ? Number(v) : null)
  const textoONulo = (v) => (v.trim() === '' ? null : v.trim())
  return {
    idPaciente: numeroONulo(f.idPaciente),
    idMedicoResponsable: numeroONulo(f.idMedicoResponsable),
    idHabitacion: numeroONulo(f.idHabitacion),
    idClinica: numeroONulo(f.idClinica),
    // Si no se indica al ingresar, el servidor usa la fecha y hora actuales.
    fechaIngreso: deInputFechaHora(f.fechaIngreso),
    motivoIngreso: f.motivoIngreso.trim(),
    diagnosticoIngreso: textoONulo(f.diagnosticoIngreso),
  }
}

export function validarHospitalizacion(f) {
  const e = {}
  if (!f.idPaciente) e.idPaciente = 'Seleccione el paciente'
  if (!f.idMedicoResponsable) e.idMedicoResponsable = 'Seleccione el médico responsable'
  if (!f.idHabitacion) e.idHabitacion = 'Toda hospitalización debe tener una habitación'
  if (!f.motivoIngreso.trim()) e.motivoIngreso = 'El motivo de ingreso es obligatorio'
  return e
}
