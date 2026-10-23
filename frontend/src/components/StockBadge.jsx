import { estadoStock } from '../utils/stock.js'
import Badge from './Badge.jsx'

/** Etiqueta del inventario de un medicamento (Sin stock / Stock bajo / Suficiente). */
export default function StockBadge({ medicamento }) {
  const e = estadoStock(medicamento)
  return <Badge tono={e.tono}>{e.texto}</Badge>
}
