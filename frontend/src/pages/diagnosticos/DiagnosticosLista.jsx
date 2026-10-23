import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { listarTiposDiagnostico } from '../../api/catalogos.js'
import { listarDiagnosticos } from '../../api/diagnosticos.js'
import { useAuth } from '../../auth/useAuth.js'
import { BadgeActivo } from '../../components/Badge.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Pagination from '../../components/Pagination.jsx'
import Table from '../../components/Table.jsx'
import { puede } from '../../config/menu.js'
import { mapaPor, useApi } from '../../hooks/useApi.js'
import { acortar, formatearFecha } from '../../utils/formato.js'

const TAMANO_PAGINA = 10
const claseSelect = 'form-control'

export default function DiagnosticosLista() {
  const { rol } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const puedeCrear = puede(rol, 'diagnosticos', 'crear')
  const puedeEditar = puede(rol, 'diagnosticos', 'editar')

  const [idTipoDiagnostico, setIdTipo] = useState('')
  const [fecha, setFecha] = useState('')
  const [texto, setTexto] = useState('')
  const [incluirInactivos, setIncluirInactivos] = useState(false)
  const [pagina, setPagina] = useState(1)
  const [exito, setExito] = useState(location.state?.mensaje ?? '')

  const { datos: tipos } = useApi('tipos-diagnostico', () => listarTiposDiagnostico().catch(() => []))
  const tiposPorId = mapaPor(tipos, 'idTipoDiagnostico')
  // El backend combina los filtros de tipo y fecha.
  const { datos, error, cargando } = useApi(`diagnosticos|${idTipoDiagnostico}|${fecha}`, () =>
    listarDiagnosticos({ idTipoDiagnostico, fecha }),
  )

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const filas = useMemo(() => {
    const busqueda = texto.trim().toLowerCase()
    return (datos ?? [])
      .filter((d) => incluirInactivos || d.activo !== false)
      .filter((d) => !idTipoDiagnostico || String(d.idTipoDiagnostico) === idTipoDiagnostico)
      .filter((d) => !fecha || d.fecha === fecha)
      .filter((d) => !busqueda || `${d.descripcion ?? ''} ${d.observaciones ?? ''}`.toLowerCase().includes(busqueda))
      .sort((a, b) => b.idDiagnostico - a.idDiagnostico)
  }, [datos, texto, incluirInactivos, idTipoDiagnostico, fecha])

  const columnas = [
    {
      clave: 'descripcion',
      titulo: 'Diagnóstico',
      render: (d) => (
        <Link to={`/diagnosticos/${d.idDiagnostico}`} className="font-weight-bold">
          {acortar(d.descripcion, 70)}
        </Link>
      ),
    },
    { clave: 'tipo', titulo: 'Tipo', render: (d) => tiposPorId[d.idTipoDiagnostico]?.descripcion ?? `#${d.idTipoDiagnostico}` },
    { clave: 'fecha', titulo: 'Fecha', render: (d) => formatearFecha(d.fecha) },
    { clave: 'activo', titulo: 'Estado', render: (d) => <BadgeActivo activo={d.activo} /> },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      className: 'text-right',
      render: (d) => (
        <div className="tabla-acciones d-flex justify-content-end">
          <Link to={`/diagnosticos/${d.idDiagnostico}`}>
            Ver
          </Link>
          {puedeEditar && d.activo !== false && (
            <Link to={`/diagnosticos/${d.idDiagnostico}/editar`}>
              Editar
            </Link>
          )}
        </div>
      ),
    },
  ]

  return (
    <div>
      <div className="card card-body d-flex flex-column flex-md-row align-items-md-center justify-content-md-between">
        <div>
          <h1 className="h3 mb-0">Diagnósticos</h1>
          <p className="text-muted mb-0">Diagnósticos médicos emitidos.</p>
        </div>
        {puedeCrear && (
          <Link
            to="/diagnosticos/nuevo"
            className="btn btn-primary btn-round"
          >
            Nuevo diagnóstico
          </Link>
        )}
      </div>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />
      <ErrorAlert mensaje={error?.message} />

      <Table
        columnas={columnas}
        filas={filas.slice((pagina - 1) * TAMANO_PAGINA, pagina * TAMANO_PAGINA)}
        claveFila={(d) => d.idDiagnostico}
        cargando={cargando}
        busqueda={texto}
        onBuscar={(v) => {
          setTexto(v)
          setPagina(1)
        }}
        placeholderBusqueda="Buscar en descripción u observaciones"
        acciones={
          <>
            <select
              aria-label="Filtrar por tipo"
              className={claseSelect}
              value={idTipoDiagnostico}
              onChange={(e) => {
                setIdTipo(e.target.value)
                setPagina(1)
              }}
            >
              <option value="">Todos los tipos</option>
              {(tipos ?? []).map((t) => (
                <option key={t.idTipoDiagnostico} value={t.idTipoDiagnostico}>
                  {t.descripcion}
                </option>
              ))}
            </select>
            <input
              type="date"
              aria-label="Filtrar por fecha"
              className={claseSelect}
              value={fecha}
              onChange={(e) => {
                setFecha(e.target.value)
                setPagina(1)
              }}
            />
            <label className="d-flex align-items-center small mb-0">
              <input
                type="checkbox"
                checked={incluirInactivos}
                onChange={(e) => {
                  setIncluirInactivos(e.target.checked)
                  setPagina(1)
                }}
                className="mr-2"
              />
              Mostrar inactivos
            </label>
          </>
        }
        vacio={<EmptyState titulo="No hay diagnósticos" descripcion="Ajuste los filtros o registre un diagnóstico." />}
        pie={<Pagination pagina={pagina} totalElementos={filas.length} tamano={TAMANO_PAGINA} onCambiarPagina={setPagina} />}
      />
    </div>
  )
}
