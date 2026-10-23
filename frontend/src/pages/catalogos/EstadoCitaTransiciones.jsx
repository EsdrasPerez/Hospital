import { useState } from 'react'
import { Button } from 'reactstrap'
import { transicionCitaPermitida } from '../../api/administracion.js'
import Badge from '../../components/Badge.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import { normalizarError } from '../../utils/errores.js'

/**
 * Cuadro de solo lectura con las transiciones que permite el sistema entre estados de cita (RN-EC-02). Se calcula preguntando
 * a GET /api/estado-cita/validar-transicion por cada par de estados activos (las reglas están fijadas en el backend).
 */
export default function EstadoCitaTransiciones({ estados }) {
  const nombres = estados.filter((e) => e.activo !== false).map((e) => e.descripcion)
  const [matriz, setMatriz] = useState(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState('')

  const calcular = async () => {
    setCargando(true)
    setError('')
    try {
      const pares = nombres.flatMap((o) => nombres.filter((d) => d !== o).map((d) => [o, d]))
      const respuestas = await Promise.all(pares.map(([o, d]) => transicionCitaPermitida(o, d)))
      const resultado = {}
      pares.forEach(([o, d], i) => {
        if (respuestas[i]) resultado[o] = [...(resultado[o] ?? []), d]
      })
      setMatriz(resultado)
    } catch (e) {
      setError(normalizarError(e).message)
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="card card-body mt-3">
      <div className="d-flex flex-wrap align-items-center justify-content-between">
        <div>
          <h3 className="h5 mb-1">Transiciones permitidas entre estados</h3>
          <p className="text-muted small mb-0">Fijadas por el sistema (no editables): una cita solo puede pasar de un estado a los que se indican.</p>
        </div>
        <Button color="primary" outline className="btn-round" onClick={calcular} disabled={cargando || nombres.length < 2}>
          {cargando ? 'Consultando...' : matriz ? 'Actualizar' : 'Ver transiciones'}
        </Button>
      </div>
      <ErrorAlert mensaje={error} />
      {matriz && (
        <div className="table-responsive mt-3">
          <table className="table mb-0">
            <thead className="text-primary">
              <tr>
                <th scope="col">Desde</th>
                <th scope="col">Puede pasar a</th>
              </tr>
            </thead>
            <tbody>
              {nombres.map((o) => (
                <tr key={o}>
                  <th scope="row">{o}</th>
                  <td>
                    {matriz[o]?.length ? (
                      <span className="d-flex flex-wrap acciones-filtros">
                        {matriz[o].map((d) => (
                          <Badge key={d} tono="azul">
                            {d}
                          </Badge>
                        ))}
                      </span>
                    ) : (
                      <span className="text-muted">Estado final (sin transiciones)</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
