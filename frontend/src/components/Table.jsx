import { Card, CardBody, Col, Input, Row, Table as TablaBootstrap } from 'reactstrap'
import EmptyState from './EmptyState.jsx'
import Loading from './Loading.jsx'

/**
 * Tabla genérica en una Card de Now UI, con barra de búsqueda y filtros opcionales.
 * - columnas: [{ clave, titulo, render?(fila), className? }]
 * - Si se pasa `onBuscar`, muestra el cuadro de búsqueda (controlado con `busqueda`).
 * - `acciones` es un nodo para la derecha de la barra (filtros, botones).
 * - `pie` se renderiza bajo la tabla (p. ej. <Pagination />).
 */
export default function Table({
  columnas,
  filas,
  claveFila = (f) => f.id,
  cargando = false,
  busqueda,
  onBuscar,
  placeholderBusqueda = 'Buscar...',
  acciones,
  vacio,
  alClickFila,
  pie,
}) {
  return (
    <Card>
      <CardBody>
        {(onBuscar || acciones) && (
          <Row className="mb-3 align-items-center">
            {onBuscar && (
              <Col md="4" className="mb-2 mb-md-0">
                <Input
                  type="search"
                  value={busqueda ?? ''}
                  onChange={(e) => onBuscar(e.target.value)}
                  placeholder={placeholderBusqueda}
                  aria-label={placeholderBusqueda}
                  bsSize="sm"
                />
              </Col>
            )}
            {acciones && (
              <Col md={onBuscar ? 8 : 12} className="d-flex flex-wrap align-items-center justify-content-md-end acciones-filtros">
                {acciones}
              </Col>
            )}
          </Row>
        )}

        {cargando ? (
          <Loading />
        ) : filas.length === 0 ? (
          (vacio ?? <EmptyState />)
        ) : (
          <TablaBootstrap responsive hover className="mb-0">
            <thead className="text-primary">
              <tr>
                {columnas.map((c) => (
                  <th key={c.clave} scope="col" className={c.className ?? ''}>
                    {c.titulo}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filas.map((fila) => (
                <tr
                  key={claveFila(fila)}
                  onClick={alClickFila ? () => alClickFila(fila) : undefined}
                  style={alClickFila ? { cursor: 'pointer' } : undefined}
                >
                  {columnas.map((c) => (
                    <td key={c.clave} className={c.className ?? ''}>
                      {c.render ? c.render(fila) : (fila[c.clave] ?? '—')}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </TablaBootstrap>
        )}
        {pie}
      </CardBody>
    </Card>
  )
}
