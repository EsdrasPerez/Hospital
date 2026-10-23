import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { listarDiagnosticos } from '../../api/diagnosticos.js'
import { listarTratamientos } from '../../api/tratamientos.js'
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

export default function TratamientosLista() {
  const { rol } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const puedeCrear = puede(rol, 'tratamientos', 'crear')
  const puedeEditar = puede(rol, 'tratamientos', 'editar')

  const [idDiagnostico, setIdDiagnostico] = useState('')
  const [texto, setTexto] = useState('')
  const [incluirInactivos, setIncluirInactivos] = useState(false)
  const [pagina, setPagina] = useState(1)
  const [exito, setExito] = useState(location.state?.mensaje ?? '')

  const { datos: diagnosticos } = useApi('diagnosticos-todos', () => listarDiagnosticos().catch(() => []))
  const diagnosticosPorId = mapaPor(diagnosticos, 'idDiagnostico')
  // El filtro por diagnóstico es combinable y devuelve el mismo universo que el listado general.
  const { datos, error, cargando } = useApi(`tratamientos|${idDiagnostico}`, () => listarTratamientos({ idDiagnostico }))

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const filas = useMemo(() => {
    const busqueda = texto.trim().toLowerCase()
    return (datos ?? [])
      .filter((t) => incluirInactivos || t.activo !== false)
      .filter((t) => !busqueda || `${t.descripcion ?? ''} ${t.indicaciones ?? ''}`.toLowerCase().includes(busqueda))
      .sort((a, b) => b.idTratamiento - a.idTratamiento)
  }, [datos, texto, incluirInactivos])

  const columnas = [
    {
      clave: 'descripcion',
      titulo: 'Tratamiento',
      render: (t) => (
        <Link to={`/tratamientos/${t.idTratamiento}`} className="font-weight-bold">
          {acortar(t.descripcion, 60)}
        </Link>
      ),
    },
    {
      clave: 'diagnostico',
      titulo: 'Diagnóstico',
      render: (t) => acortar(diagnosticosPorId[t.idDiagnostico]?.descripcion ?? `#${t.idDiagnostico}`, 50),
    },
    { clave: 'duracionDias', titulo: 'Duración', render: (t) => (t.duracionDias != null ? `${t.duracionDias} días` : '—') },
    { clave: 'fechaInicio', titulo: 'Inicio', render: (t) => formatearFecha(t.fechaInicio) },
    { clave: 'fechaFin', titulo: 'Fin', render: (t) => formatearFecha(t.fechaFin) },
    { clave: 'activo', titulo: 'Estado', render: (t) => <BadgeActivo activo={t.activo} /> },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      className: 'text-right',
      render: (t) => (
        <div className="tabla-acciones d-flex justify-content-end">
          <Link to={`/tratamientos/${t.idTratamiento}`}>
            Ver
          </Link>
          {puedeEditar && t.activo !== false && (
            <Link to={`/tratamientos/${t.idTratamiento}/editar`}>
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
          <h1 className="h3 mb-0">Tratamientos</h1>
          <p className="text-muted mb-0">Tratamientos indicados a partir de un diagnóstico.</p>
        </div>
        {puedeCrear && (
          <Link
            to="/tratamientos/nuevo"
            className="btn btn-primary btn-round"
          >
            Nuevo tratamiento
          </Link>
        )}
      </div>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />
      <ErrorAlert mensaje={error?.message} />

      <Table
        columnas={columnas}
        filas={filas.slice((pagina - 1) * TAMANO_PAGINA, pagina * TAMANO_PAGINA)}
        claveFila={(t) => t.idTratamiento}
        cargando={cargando}
        busqueda={texto}
        onBuscar={(v) => {
          setTexto(v)
          setPagina(1)
        }}
        placeholderBusqueda="Buscar en descripción o indicaciones"
        acciones={
          <>
            <select
              aria-label="Filtrar por diagnóstico"
              className={`${claseSelect}`}
              value={idDiagnostico}
              onChange={(e) => {
                setIdDiagnostico(e.target.value)
                setPagina(1)
              }}
            >
              <option value="">Todos los diagnósticos</option>
              {(diagnosticos ?? []).map((d) => (
                <option key={d.idDiagnostico} value={d.idDiagnostico}>
                  {acortar(d.descripcion, 50)}
                </option>
              ))}
            </select>
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
        vacio={<EmptyState titulo="No hay tratamientos" descripcion="Ajuste los filtros o registre un tratamiento." />}
        pie={<Pagination pagina={pagina} totalElementos={filas.length} tamano={TAMANO_PAGINA} onCambiarPagina={setPagina} />}
      />
    </div>
  )
}
