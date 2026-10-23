import Badge from './Badge.jsx'

const TONOS = { pendiente: 'ambar', pagada: 'verde', cancelada: 'gris' }

/** Estado de una factura (por la descripción del catálogo) y, si está anulada (activo=false), la marca "Anulada". */
export default function EstadoFacturaBadge({ estado, activo = true }) {
  const texto = estado?.descripcion
  return (
    <span className="d-inline-flex align-items-center acciones-grupo">
      {texto ? <Badge tono={TONOS[texto.toLowerCase()] ?? 'gris'}>{texto}</Badge> : <span className="text-muted">—</span>}
      {activo === false && <Badge tono="rojo">Anulada</Badge>}
    </span>
  )
}
