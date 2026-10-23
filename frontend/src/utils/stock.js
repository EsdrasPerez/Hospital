/** Estado del inventario: sin stock (<= 0), bajo (<= stock mínimo) o suficiente. */
export function estadoStock(m) {
  const stock = m?.stock ?? 0
  const minimo = m?.stockMinimo ?? 0
  if (stock <= 0) return { clave: 'sin', texto: 'Sin stock', tono: 'rojo' }
  if (stock <= minimo) return { clave: 'bajo', texto: 'Stock bajo', tono: 'ambar' }
  return { clave: 'suficiente', texto: 'Suficiente', tono: 'verde' }
}
