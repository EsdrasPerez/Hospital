import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { listarTiposDiagnostico } from '../../api/catalogos.js'
import { listarConsultasDeDiagnostico } from '../../api/consultas.js'
import { desactivarDiagnostico, obtenerDiagnostico } from '../../api/diagnosticos.js'
import { listarTratamientos } from '../../api/tratamientos.js'
import { useAuth } from '../../auth/useAuth.js'
import { BadgeActivo } from '../../components/Badge.jsx'
import ConfirmDialog from '../../components/ConfirmDialog.jsx'
import Dato from '../../components/Dato.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Loading from '../../components/Loading.jsx'
import { puede } from '../../config/menu.js'
import { useApi } from '../../hooks/useApi.js'
import { normalizarError } from '../../utils/errores.js'
import { formatearFecha } from '../../utils/formato.js'

export default function DiagnosticoDetalle() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { rol } = useAuth()
  const puedeEditar = puede(rol, 'diagnosticos', 'editar')
  const puedeDesactivar = puede(rol, 'diagnosticos', 'desactivar')
  const puedeCrearTratamiento = puede(rol, 'tratamientos', 'crear')

  const [exito, setExito] = useState(location.state?.mensaje ?? '')
  const [confirmando, setConfirmando] = useState(false)
  const [desactivando, setDesactivando] = useState(false)
  const [errorDesactivar, setErrorDesactivar] = useState('')

  const { datos, error, cargando } = useApi(`diagnostico|${id}`, async () => {
    const diagnostico = await obtenerDiagnostico(id)
    const [tipos, tratamientos, consultas] = await Promise.all([
      listarTiposDiagnostico().catch(() => []),
      listarTratamientos({ idDiagnostico: id, soloActivos: true }).catch(() => []),
      listarConsultasDeDiagnostico(id).catch(() => []),
    ])
    return { diagnostico, tipos, tratamientos, consultas }
  })

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const desactivar = async () => {
    setDesactivando(true)
    setErrorDesactivar('')
    try {
      await desactivarDiagnostico(id)
      navigate('/diagnosticos', { replace: true, state: { mensaje: 'El diagnóstico fue desactivado.' } })
    } catch (e) {
      // 409: vinculado a una consulta o con tratamientos registrados
      setErrorDesactivar(normalizarError(e).message)
      setDesactivando(false)
    }
  }

  if (cargando) return <Loading texto="Cargando diagnóstico..." />

  if (error) {
    return (
      <div>
        <ErrorAlert titulo={error.status === 404 ? 'Diagnóstico no encontrado' : 'No se pudo cargar el diagnóstico'} mensaje={error.message} />
        <Link to="/diagnosticos" className="volver-enlace">
          &larr; Volver a diagnósticos
        </Link>
      </div>
    )
  }

  const { diagnostico: d, tipos, tratamientos, consultas } = datos
  const activo = d.activo !== false
  const tipo = tipos.find((t) => t.idTipoDiagnostico === d.idTipoDiagnostico)

  return (
    <div>
      <Link to="/diagnosticos" className="volver-enlace">
        &larr; Volver a diagnósticos
      </Link>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />

      <div className="card card-body d-flex flex-column flex-md-row align-items-md-center justify-content-md-between">
        <div>
          <h1 className="h3 mb-0">{d.descripcion}</h1>
          <p className="text-muted small">
            <BadgeActivo activo={d.activo} />
          </p>
        </div>
        <div className="acciones-grupo d-flex">
          {activo && puedeEditar && (
            <Link
              to={`/diagnosticos/${d.idDiagnostico}/editar`}
              className="btn btn-secondary btn-round"
            >
              Editar
            </Link>
          )}
          {activo && puedeDesactivar && (
            <button
              type="button"
              onClick={() => {
                setErrorDesactivar('')
                setConfirmando(true)
              }}
              className="btn btn-danger btn-round"
            >
              Desactivar
            </button>
          )}
        </div>
      </div>

      <div className="card card-body">
        <div className="row">
          <Dato etiqueta="Tipo">{tipo?.descripcion ?? `#${d.idTipoDiagnostico}`}</Dato>
          <Dato etiqueta="Fecha">{formatearFecha(d.fecha)}</Dato>
          <Dato etiqueta="Observaciones" className="col-12">
            {d.observaciones}
          </Dato>
        </div>
      </div>

      <div className="card card-body">
        <div className="d-flex align-items-center justify-content-between mb-3">
          <h2 className="h5">Tratamientos activos</h2>
          {activo && puedeCrearTratamiento && (
            <Link to={`/tratamientos/nuevo?idDiagnostico=${d.idDiagnostico}`} className="font-weight-bold">
              Agregar tratamiento
            </Link>
          )}
        </div>
        {tratamientos.length === 0 ? (
          <p className="text-muted mb-0">Este diagnóstico no tiene tratamientos activos.</p>
        ) : (
          <ul className="lista-divisoria">
            {tratamientos.map((t) => (
              <li key={t.idTratamiento}>
                <Link to={`/tratamientos/${t.idTratamiento}`} className="font-weight-bold">
                  {t.descripcion}
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="card card-body">
        <h2 className="h5 mb-3">Consultas donde se emitió</h2>
        {consultas.length === 0 ? (
          <p className="text-muted mb-0">Todavía no está asociado a ninguna consulta.</p>
        ) : (
          <ul className="lista-divisoria">
            {consultas.map((c) => (
              <li key={c.idConsulta}>
                <Link to={`/consultas/${c.idConsulta}`} className="font-weight-bold">
                  Consulta #{c.idConsulta}
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>

      <ConfirmDialog
        abierto={confirmando}
        titulo="Desactivar diagnóstico"
        mensaje="¿Desea desactivar este diagnóstico? No se permite si está vinculado a una consulta o tiene tratamientos."
        textoConfirmar="Desactivar"
        peligro
        cargando={desactivando}
        error={errorDesactivar}
        onConfirmar={desactivar}
        onCancelar={() => setConfirmando(false)}
      />
    </div>
  )
}
