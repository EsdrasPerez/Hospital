import { aInputFechaHora, deInputFechaHora } from '../../utils/formato.js'

export const FORM_CONSULTA_VACIO = {
  idCita: '',
  fechaHora: '',
  sintomas: '',
  peso: '',
  talla: '',
  temperatura: '',
  presionArterial: '',
  frecuenciaCardiaca: '',
}

export function consultaAFormulario(c) {
  const texto = (v) => (v != null ? String(v) : '')
  return {
    idCita: texto(c.idCita),
    fechaHora: aInputFechaHora(c.fechaHora),
    sintomas: c.sintomas ?? '',
    peso: texto(c.peso),
    talla: texto(c.talla),
    temperatura: texto(c.temperatura),
    presionArterial: c.presionArterial ?? '',
    frecuenciaCardiaca: texto(c.frecuenciaCardiaca),
  }
}

export function formularioAConsulta(f) {
  const numero = (v) => (v === '' ? null : Number(v))
  const textoONulo = (v) => (v.trim() === '' ? null : v.trim())
  return {
    idCita: Number(f.idCita),
    // Si no se indica, el servidor asigna la fecha y hora actuales (solo al crear).
    fechaHora: deInputFechaHora(f.fechaHora),
    sintomas: textoONulo(f.sintomas),
    peso: numero(f.peso),
    talla: numero(f.talla),
    temperatura: numero(f.temperatura),
    presionArterial: textoONulo(f.presionArterial),
    frecuenciaCardiaca: numero(f.frecuenciaCardiaca),
  }
}

const decimales = (valor) => (String(valor).split('.')[1] ?? '').length

/** Rangos del backend (ConsultasServiceImpl) y límites de columnas (DECIMAL(5,2), (4,1), VARCHAR(10)). */
export function validarConsulta(f) {
  const e = {}
  if (!f.idCita) e.idCita = 'Seleccione la cita'
  const positivo = (campo, etiqueta) => {
    if (f[campo] === '') return
    const n = Number(f[campo])
    if (Number.isNaN(n) || n <= 0) e[campo] = `${etiqueta} debe ser mayor a cero`
    else if (n > 999.99) e[campo] = 'Valor demasiado alto (máximo 999.99)'
    else if (decimales(f[campo]) > 2) e[campo] = 'Máximo 2 decimales'
  }
  positivo('peso', 'El peso')
  positivo('talla', 'La talla')
  if (f.temperatura !== '') {
    const t = Number(f.temperatura)
    if (Number.isNaN(t) || t < 30 || t > 45) e.temperatura = 'La temperatura debe estar entre 30 y 45 °C'
    else if (decimales(f.temperatura) > 1) e.temperatura = 'La temperatura admite como máximo 1 decimal'

  }
  if (f.frecuenciaCardiaca !== '') {
    const fc = Number(f.frecuenciaCardiaca)
    if (!Number.isInteger(fc) || fc < 20 || fc > 250) e.frecuenciaCardiaca = 'Debe ser un entero entre 20 y 250 lpm'
  }
  if (f.presionArterial.trim().length > 10) e.presionArterial = 'Máximo 10 caracteres (ej. 120/80)'
  return e
}
