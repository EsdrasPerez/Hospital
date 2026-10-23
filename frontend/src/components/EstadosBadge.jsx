import Badge from './Badge.jsx'

const TONO_HABITACION = { disponible: 'verde', ocupada: 'rojo', mantenimiento: 'ambar' }
const TONO_HOSPITALIZACION = { activa: 'teal', alta: 'verde', traslado: 'azul', fallecimiento: 'gris' }

const texto = (estado) => String(estado?.nombre ?? estado?.descripcion ?? '').trim()

/** Etiqueta del estado de una habitación (estado = { nombre }). */
export function EstadoHabitacionBadge({ estado, fallback = '—' }) {
  if (!estado) return <span className="text-muted">{fallback}</span>
  return <Badge tono={TONO_HABITACION[texto(estado).toLowerCase()] ?? 'gris'}>{texto(estado)}</Badge>
}

/** Etiqueta del estado de una hospitalización (estado = { descripcion }). */
export function EstadoHospBadge({ estado, fallback = '—' }) {
  if (!estado) return <span className="text-muted">{fallback}</span>
  return <Badge tono={TONO_HOSPITALIZACION[texto(estado).toLowerCase()] ?? 'gris'}>{texto(estado)}</Badge>
}
