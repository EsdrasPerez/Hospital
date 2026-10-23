import Badge from './Badge.jsx'

const TONOS = {
  programada: 'azul',
  confirmada: 'teal',
  'en curso': 'ambar',
  atendida: 'verde',
  cancelada: 'rojo',
  'no asistió': 'gris',
  reprogramada: 'ambar',
}

/** Etiqueta de color según la descripción del estado de la cita. */
export default function EstadoCitaBadge({ estado }) {
  if (!estado) return <span className="text-muted">—</span>
  const texto = estado.descripcion || estado.nombre
  return <Badge tono={TONOS[String(texto).toLowerCase()] ?? 'gris'}>{texto}</Badge>
}
