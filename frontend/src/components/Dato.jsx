import { Col } from 'reactstrap'

/**
 * Par etiqueta/valor para pantallas de detalle. Se usa dentro de un <Row>; `ancho` define las
 * columnas (por defecto un tercio en escritorio) y `completo` ocupa toda la fila.
 */
export default function Dato({ etiqueta, children, completo = false, className = '' }) {
  // `className` se conserva por compatibilidad con las pantallas que pedían ancho completo
  const ancho = completo || /col-12/.test(className) ? 12 : undefined
  return (
    <Col xs="12" sm={ancho ?? 6} lg={ancho ?? 4}>
      <div className="dato-etiqueta">{etiqueta}</div>
      <div className="dato-valor">{children || '—'}</div>
    </Col>
  )
}
