import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { listarMedicamentos } from '../../api/medicamentos.js'
import { useAuth } from '../../auth/useAuth.js'
import Badge, { BadgeActivo } from '../../components/Badge.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Pagination from '../../components/Pagination.jsx'
import StockBadge from '../../components/StockBadge.jsx'
import Table from '../../components/Table.jsx'
import { puede } from '../../config/menu.js'
import { useApi } from '../../hooks/useApi.js'
import { formatearMoneda } from '../../utils/formato.js'
import { estadoStock } from '../../utils/stock.js'

const TAMANO_PAGINA = 10
const claseSelect = 'form-control'

export default function MedicamentosLista() {
  const { rol } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const puedeCrear = puede(rol, 'medicamentos', 'crear')
  const puedeEditar = puede(rol, 'medicamentos', 'editar')

  const [texto, setTexto] = useState('')
  const [params] = useSearchParams()
  // ?stock=alerta (enlace de "Stock bajo" en Inicio): sin stock o por debajo del mínimo
  const [filtroStock, setFiltroStock] = useState(['sin', 'bajo', 'alerta', 'suficiente'].includes(params.get('stock')) ? params.get('stock') : '')
  const [filtroReceta, setFiltroReceta] = useState('')
  const [incluirInactivos, setIncluirInactivos] = useState(false)
  const [pagina, setPagina] = useState(1)
  const [exito, setExito] = useState(location.state?.mensaje ?? '')

  // El texto se envía al servidor (criterio) con una pequeña espera para no pedir en cada tecla
  const [criterio, setCriterio] = useState('')
  useEffect(() => {
    const t = setTimeout(() => setCriterio(texto.trim()), 300)
    return () => clearTimeout(t)
  }, [texto])

  // Filtros enviados al servidor: criterio, bajoStock (sin stock / bajo) y soloActivos
  const bajoStock = filtroStock === 'sin' || filtroStock === 'bajo' || filtroStock === 'alerta'
  const soloActivos = !incluirInactivos
  const { datos, error, cargando } = useApi(`medicamentos|${criterio}|${bajoStock}|${soloActivos}`, () =>
    listarMedicamentos({ criterio, bajoStock, soloActivos }),
  )
  const { datos: datosBajos } = useApi('medicamentos-bajos', () => listarMedicamentos({ bajoStock: true, soloActivos: true }))

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const filas = useMemo(() => {
    return (datos ?? [])
      .filter((m) => !filtroStock || (filtroStock === 'alerta' ? estadoStock(m).clave !== 'suficiente' : estadoStock(m).clave === filtroStock))
      .filter((m) => !filtroReceta || (filtroReceta === 'si' ? m.requiereReceta : !m.requiereReceta))
      .sort((a, b) => a.nombreComercial.localeCompare(b.nombreComercial, 'es'))
  }, [datos, filtroStock, filtroReceta])

  const bajos = (datosBajos ?? []).length

  const alCambiar = (setter) => (e) => {
    setter(e.target.value)
    setPagina(1)
  }

  const columnas = [
    {
      clave: 'nombre',
      titulo: 'Medicamento',
      render: (m) => (
        <>
          <Link to={`/medicamentos/${m.idMedicamento}`} className="font-weight-bold">
            {m.nombreComercial}
          </Link>
          {m.requiereReceta && (
            <span className="ml-2">
              <Badge tono="azul">Receta</Badge>
            </span>
          )}
          <div className="text-muted small">{[m.nombreGenerico, m.presentacion].filter(Boolean).join(' · ') || '—'}</div>
          {m.laboratorio && <div className="text-muted small">{m.laboratorio}</div>}
        </>
      ),
    },
    {
      clave: 'stock',
      titulo: 'Stock',
      render: (m) => (
        <span className="d-flex align-items-center acciones-grupo">
          <strong>{m.stock}</strong>
          <StockBadge medicamento={m} />
        </span>
      ),
    },
    { clave: 'precio', titulo: 'Precio', render: (m) => formatearMoneda(m.precio) },
    {
      clave: 'estado',
      titulo: 'Estado',
      render: (m) => <BadgeActivo activo={m.activo} />,
    },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      className: 'text-right',
      render: (m) => (
        <div className="tabla-acciones d-flex justify-content-end">
          <Link to={`/medicamentos/${m.idMedicamento}`}>Ver</Link>
          {puedeEditar && m.activo !== false && <Link to={`/medicamentos/${m.idMedicamento}/editar`}>Editar</Link>}
        </div>
      ),
    },
  ]

  return (
    <div>
      <div className="card card-body d-flex flex-column flex-md-row align-items-md-center justify-content-md-between">
        <div>
          <h1 className="h3 mb-0">Medicamentos</h1>
          <p className="text-muted mb-0">
            Inventario de farmacia.
            {bajos > 0 && (
              <>
                {' '}
                <Badge tono="ambar">{bajos} con stock bajo o agotado</Badge>
              </>
            )}
          </p>
        </div>
        {puedeCrear && (
          <Link to="/medicamentos/nuevo" className="btn btn-primary btn-round">
            Nuevo medicamento
          </Link>
        )}
      </div>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />
      <ErrorAlert mensaje={error?.message} />

      <Table
        columnas={columnas}
        filas={filas.slice((pagina - 1) * TAMANO_PAGINA, pagina * TAMANO_PAGINA)}
        claveFila={(m) => m.idMedicamento}
        cargando={cargando}
        busqueda={texto}
        onBuscar={(v) => {
          setTexto(v)
          setPagina(1)
        }}
        placeholderBusqueda="Buscar por nombre, genérico o laboratorio"
        acciones={
          <>
            <select aria-label="Filtrar por stock" className={claseSelect} value={filtroStock} onChange={alCambiar(setFiltroStock)}>
              <option value="">Todo el stock</option>
              <option value="alerta">Stock bajo o agotado</option>
              <option value="sin">Sin stock</option>
              <option value="bajo">Stock bajo</option>
              <option value="suficiente">Stock suficiente</option>
            </select>
            <select aria-label="Filtrar por receta" className={claseSelect} value={filtroReceta} onChange={alCambiar(setFiltroReceta)}>
              <option value="">Con y sin receta</option>
              <option value="si">Requieren receta</option>
              <option value="no">Sin receta</option>
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
        vacio={<EmptyState titulo="No hay medicamentos" descripcion="Ajuste los filtros o registre un medicamento." />}
        pie={<Pagination pagina={pagina} totalElementos={filas.length} tamano={TAMANO_PAGINA} onCambiarPagina={setPagina} />}
      />
    </div>
  )
}
