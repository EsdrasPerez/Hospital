/** 'YYYY-MM-DD' -> 'DD/MM/YYYY' (sin conversión de zona horaria). */
export function formatearFecha(valor) {
  if (!valor) return '—'
  const [anio, mes, dia] = String(valor).slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

/** 'YYYY-MM-DDTHH:mm:ss' -> 'DD/MM/YYYY HH:mm' */
export function formatearFechaHora(valor) {
  if (!valor) return '—'
  const texto = String(valor)
  const hora = texto.length >= 16 ? texto.slice(11, 16) : ''
  return `${formatearFecha(texto)} ${hora}`.trim()
}

export function calcularEdad(fechaNacimiento) {
  if (!fechaNacimiento) return null
  const [anio, mes, dia] = String(fechaNacimiento).slice(0, 10).split('-').map(Number)
  const hoy = new Date()
  let edad = hoy.getFullYear() - anio
  if (hoy.getMonth() + 1 < mes || (hoy.getMonth() + 1 === mes && hoy.getDate() < dia)) edad -= 1
  return edad
}

/** Fecha de ayer en formato YYYY-MM-DD (máximo permitido para fecha de nacimiento). */
export function ayerISO() {
  const d = new Date()
  d.setDate(d.getDate() - 1)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export const ETIQUETAS_ROL = {
  ADMIN: 'Administrador',
  MEDICO: 'Médico',
  ENFERMERO: 'Enfermero',
  RECEPCIONISTA: 'Recepcionista',
  FARMACEUTICO: 'Farmacéutico',
  CAJERO: 'Cajero',
}

const pad2 = (n) => String(n).padStart(2, '0')

/** Fecha de hoy (hora local) en formato YYYY-MM-DD. */
export function hoyISO() {
  const d = new Date()
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}

/** 'HH:mm:ss' -> 'HH:mm' (valor para <input type="time"> y visualización). */
export function formatearHora(valor) {
  return valor ? String(valor).slice(0, 5) : '—'
}

/**
 * LocalDateTime del backend ('YYYY-MM-DDTHH:mm:ss[.SSS]', sin zona) -> valor de
 * <input type="datetime-local"> ('YYYY-MM-DDTHH:mm'). Se corta el texto: no se usa Date
 * para evitar desfases de zona horaria.
 */
export function aInputFechaHora(valor) {
  return valor ? String(valor).slice(0, 16) : ''
}

/** Valor de <input type="datetime-local"> -> LocalDateTime del backend. */
export function deInputFechaHora(valor) {
  return valor ? `${valor}:00` : null
}

/** Texto acortado con puntos suspensivos. */
export function acortar(texto, max = 60) {
  if (!texto) return '—'
  return texto.length > max ? `${texto.slice(0, max - 1)}…` : texto
}

/** Quetzales con separador de miles y 2 decimales (el backend guarda DECIMAL(10,2)). */
export function formatearMoneda(valor) {
  if (valor === null || valor === undefined || valor === '') return '—'
  return `Q ${Number(valor).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}
