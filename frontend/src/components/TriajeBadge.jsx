import Badge from './Badge.jsx'

const TONOS = {
  rojo: 'rojo',
  naranja: 'naranja',
  amarillo: 'amarillo',
  verde: 'verde',
  azul: 'azul',
}

// Color por código si el catálogo de niveles de urgencia no se pudo leer (N1 más grave ... N5 menos grave).
const COLOR_POR_CODIGO = { n1: 'rojo', n2: 'naranja', n3: 'amarillo', n4: 'verde', n5: 'azul' }

function tonoTriaje(triaje, niveles) {
  const nombre = String(triaje.nivelDeEmergencia ?? '').trim().toLowerCase()
  // El código del nivel de urgencia (N1..N5) coincide con el nombre del triaje y trae el color.
  const nivel = (niveles ?? []).find((n) => String(n.codigo).trim().toLowerCase() === nombre)
  const color = String(nivel?.color ?? '').trim().toLowerCase()
  return TONOS[color] ?? TONOS[nombre.split(/\s+/)[0]] ?? COLOR_POR_CODIGO[nombre] ?? 'gris'
}

/** Etiqueta con el nivel de triaje (`triaje` = { nivelDeEmergencia }); `niveles` = catálogo niveles-urgencia (opcional). */
export default function TriajeBadge({ triaje, niveles, fallback = '—' }) {
  if (!triaje) return <span className="text-muted">{fallback}</span>
  return <Badge tono={tonoTriaje(triaje, niveles)}>{triaje.nivelDeEmergencia}</Badge>
}
