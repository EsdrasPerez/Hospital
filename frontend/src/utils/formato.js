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
