import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { listarMedicamentos } from '../../api/medicamentos.js'
import { listarRecetas, quitarMedicamento } from '../../api/recetas.js'
import { listarTratamientos } from '../../api/tratamientos.js'
import { useAuth } from '../../auth/useAuth.js'
import Badge from '../../components/Badge.jsx'
import ConfirmDialog from '../../components/ConfirmDialog.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Pagination from '../../components/Pagination.jsx'
import StockBadge from '../../components/StockBadge.jsx'
import Table from '../../components/Table.jsx'
import { puede } from '../../config/menu.js'
import { mapaPor, useApi } from '../../hooks/useApi.js'
import { normalizarError } from '../../utils/errores.js'
import { acortar } from '../../utils/formato.js'

const TAMANO_PAGINA = 10
const claseSelect = 'form-control'

/** Lista general de medicamentos recetados (tratamiento–medicamento), útil para farmacia al dispensar. */
export default function RecetasLista() {
  const { rol } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const puedeQuitar = puede(rol, 'recetas', 'desactivar')

  const [texto, setTexto] = useState('')
  const [idMedicamento, setIdMedicamento] = useState('')
  const [soloRequierenReceta, setSoloRequierenReceta] = useState(false)
  const [pagina, setPagina] = useState(1)
  const [exito, setExito] = useState(location.state?.mensaje ?? '')
  const [aQuitar, setAQuitar] = useState(null)
  const [quitando, setQuitando] = useState(false)
  const [errorQuitar, setErrorQuitar] = useState('')

  // idMedicamento se filtra en el servidor
  const { datos, error, cargando, recargar } = useApi(`recetas|${idMedicamento}`, async () => {
    const [recetas, medicamentos, tratamientos] = await Promise.all([
      listarRecetas({ idMedicamento }),
      listarMedicamentos().catch(() => []),
      listarTratamientos().catch(() => []),
    ])
    return { recetas, medicamentos, medicamentosPorId: mapaPor(medicamentos, 'idMedicamento'), tratamientosPorId: mapaPor(tratamientos, 'idTratamiento') }
  })

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const filas = useMemo(() => {
    if (!datos) return []
    const busqueda = texto.trim().toLowerCase()
    return datos.recetas
      .map((r) => ({ ...r, medicamento: datos.medicamentosPorId[r.idMedicamento], tratamiento: datos.tratamientosPorId[r.idTratamiento] }))
      .filter((r) => !soloRequierenReceta || r.medicamento?.requiereReceta)
      .filter((r) => !busqueda || `${r.medicamento?.nombreComercial ?? ''} ${r.medicamento?.nombreGenerico ?? ''} ${r.tratamiento?.descripcion ?? ''}`.toLowerCase().includes(busqueda))
  }, [datos, texto, soloRequierenReceta])

  const quitar = async () => {
    setQuitando(true)
    setErrorQuitar('')
    try {
      await quitarMedicamento(aQuitar.idMedicamento, aQuitar.idTratamiento)
      setAQuitar(null)
      setExito('El medicamento fue quitado del tratamiento.')
      recargar()
    } catch (e) {
      setErrorQuitar(normalizarError(e).message)
    } finally {
      setQuitando(false)
    }
  }

  const todasLasColumnas = [
    {
      clave: 'medicamento',
      titulo: 'Medicamento',
      render: (r) =>
        r.medicamento ? (
          <>
            <Link to={`/medicamentos/${r.idMedicamento}`} className="font-weight-bold">
              {r.medicamento.nombreComercial}
            </Link>
            {r.medicamento.nombreGenerico && <div className="text-muted small">{r.medicamento.nombreGenerico}</div>}
          </>
        ) : (
          `Medicamento #${r.idMedicamento}`
        ),
    },
    {
      clave: 'tratamiento',
      titulo: 'Tratamiento',
      render: (r) => (
        <Link to={`/tratamientos/${r.idTratamiento}`}>{r.tratamiento ? acortar(r.tratamiento.descripcion, 55) : `Tratamiento #${r.idTratamiento}`}</Link>
      ),
    },
    {
      clave: 'stock',
      titulo: 'Stock',
      render: (r) =>
        r.medicamento ? (
          <span className="d-flex align-items-center acciones-grupo">
            <strong>{r.medicamento.stock}</strong>
            <StockBadge medicamento={r.medicamento} />
          </span>
        ) : (
          '—'
        ),
    },
    { clave: 'receta', titulo: 'Receta', render: (r) => (r.medicamento?.requiereReceta ? <Badge tono="azul">Requiere receta</Badge> : <span className="text-muted">No</span>) },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      className: 'text-right',
      render: (r) => (
        <div className="tabla-acciones d-flex justify-content-end">
          {puedeQuitar && (
            <button
              type="button"
              className="btn btn-link text-danger p-0"
              onClick={() => {
                setErrorQuitar('')
                setAQuitar(r)
              }}
            >
              Quitar
            </button>
          )}
        </div>
      ),
    },
  ]

  // La columna de acciones solo aparece para quienes pueden quitar medicamentos (ADMIN, MEDICO)
  const columnas = todasLasColumnas.filter((c) => c.clave !== 'acciones' || puedeQuitar)

  return (
    <div>
      <div className="card card-body">
        <h1 className="h3 mb-0">Medicamentos recetados</h1>
        <p className="text-muted mb-0">Medicamentos asignados a cada tratamiento. Para asignar uno nuevo, abra el detalle del tratamiento.</p>
      </div>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />
      <ErrorAlert mensaje={error?.message} />

      <Table
        columnas={columnas}
        filas={filas.slice((pagina - 1) * TAMANO_PAGINA, pagina * TAMANO_PAGINA)}
        claveFila={(r) => `${r.idMedicamento}-${r.idTratamiento}`}
        cargando={cargando}
        busqueda={texto}
        onBuscar={(v) => {
          setTexto(v)
          setPagina(1)
        }}
        placeholderBusqueda="Buscar por medicamento o tratamiento"
        acciones={
          <>
            <select
              aria-label="Filtrar por medicamento"
              className={claseSelect}
              value={idMedicamento}
              onChange={(e) => {
                setIdMedicamento(e.target.value)
                setPagina(1)
              }}
            >
              <option value="">Todos los medicamentos</option>
              {(datos?.medicamentos ?? []).map((m) => (
                <option key={m.idMedicamento} value={m.idMedicamento}>
                  {m.nombreComercial}
                </option>
              ))}
            </select>
            <label className="d-flex align-items-center small mb-0">
              <input
                type="checkbox"
                checked={soloRequierenReceta}
                onChange={(e) => {
                  setSoloRequierenReceta(e.target.checked)
                  setPagina(1)
                }}
                className="mr-2"
              />
              Solo con receta
            </label>
          </>
        }
        vacio={<EmptyState titulo="No hay medicamentos recetados" descripcion="Aún no se asignó ningún medicamento a un tratamiento." />}
        pie={<Pagination pagina={pagina} totalElementos={filas.length} tamano={TAMANO_PAGINA} onCambiarPagina={setPagina} />}
      />

      <ConfirmDialog
        abierto={!!aQuitar}
        titulo="Quitar medicamento"
        mensaje="¿Desea quitar este medicamento del tratamiento? El medicamento no se elimina."
        textoConfirmar="Quitar"
        peligro
        cargando={quitando}
        error={errorQuitar}
        onConfirmar={quitar}
        onCancelar={() => setAQuitar(null)}
      />
    </div>
  )
}
