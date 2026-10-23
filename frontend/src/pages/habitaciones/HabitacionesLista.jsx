import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { listarHabitaciones } from '../../api/habitaciones.js'
import { listarHospitalizacionesActivas } from '../../api/hospitalizaciones.js'
import { useAuth } from '../../auth/useAuth.js'
import { BadgeActivo } from '../../components/Badge.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import { EstadoHabitacionBadge } from '../../components/EstadosBadge.jsx'
import Loading from '../../components/Loading.jsx'
import Pagination from '../../components/Pagination.jsx'
import Table from '../../components/Table.jsx'
import { puede } from '../../config/menu.js'
import { useApi } from '../../hooks/useApi.js'
import { useDatosHospitalizacion } from '../../hooks/useDatosHospitalizacion.js'

const TAMANO_PAGINA = 10
const claseSelect = 'form-control'

export default function HabitacionesLista() {
  const { rol } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const puedeCrear = puede(rol, 'habitaciones', 'crear')
  const puedeEditar = puede(rol, 'habitaciones', 'editar')

  const [vista, setVista] = useState('tabla')
  const [params] = useSearchParams()
  // ?idEstado=<idEstadoHabitacion> (enlace de "Habitaciones disponibles" en Inicio)
  const [idEstado, setIdEstado] = useState(/^\d+$/.test(params.get('idEstado') ?? '') ? params.get('idEstado') : '')
  const [idTipo, setIdTipo] = useState('')
  const [piso, setPiso] = useState('')
  const [texto, setTexto] = useState('')
  const [incluirInactivas, setIncluirInactivas] = useState(false)
  const [pagina, setPagina] = useState(1)
  const [exito, setExito] = useState(location.state?.mensaje ?? '')

  const datos = useDatosHospitalizacion()
  // Estado, tipo, piso y soloActivas se envían al servidor (se aplican juntos); el número se busca en el cliente.
  const { datos: habitaciones, error: errorLista, cargando: cargandoLista } = useApi(
    `habitaciones|${idEstado}|${idTipo}|${piso}|${incluirInactivas}`,
    () => listarHabitaciones({ piso, idTipoHabitacion: idTipo, idEstadoHabitacion: idEstado, soloActivas: !incluirInactivas }),
  )
  // Ocupación = hospitalizaciones activas por habitación
  const { datos: activas } = useApi('hospitalizaciones-activas', () => listarHospitalizacionesActivas().catch(() => []))

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const ocupacionPorHabitacion = useMemo(() => {
    const mapa = {}
    for (const h of activas ?? []) mapa[h.idHabitacion] = (mapa[h.idHabitacion] ?? 0) + 1
    return mapa
  }, [activas])

  const pisos = useMemo(() => [...new Set(datos.habitaciones.map((h) => h.piso))].sort((a, b) => a - b), [datos.habitaciones])

  const filas = useMemo(() => {
    const busqueda = texto.trim().toLowerCase()
    return (habitaciones ?? [])
      .filter((h) => !busqueda || String(h.numero).toLowerCase().includes(busqueda))
      .sort((a, b) => a.piso - b.piso || String(a.numero).localeCompare(String(b.numero), 'es', { numeric: true }))
  }, [habitaciones, texto])

  const alCambiar = (setter) => (e) => {
    setter(e.target.value)
    setPagina(1)
  }

  const ocupacion = (h) => `${ocupacionPorHabitacion[h.idHabitacion] ?? 0} / ${h.capacidad}`

  const columnas = [
    {
      clave: 'numero',
      titulo: 'Número',
      render: (h) => (
        <Link to={`/habitaciones/${h.idHabitacion}`} className="font-weight-bold">
          {h.numero}
        </Link>
      ),
    },
    { clave: 'piso', titulo: 'Piso' },
    { clave: 'tipo', titulo: 'Tipo', render: (h) => datos.tiposHabPorId[h.idTipoHabitacion]?.descripcion ?? (h.idTipoHabitacion ? `#${h.idTipoHabitacion}` : '—') },
    { clave: 'ocupacion', titulo: 'Ocupación', render: ocupacion },
    {
      clave: 'estado',
      titulo: 'Estado',
      render: (h) => (
        <span className="d-flex align-items-center">
          <EstadoHabitacionBadge estado={datos.estadosHabPorId[h.idEstadoHabitacion]} fallback={`#${h.idEstadoHabitacion}`} />
          {h.activo === false && <BadgeActivo activo={false} />}
        </span>
      ),
    },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      className: 'text-right',
      render: (h) => (
        <div className="tabla-acciones d-flex justify-content-end">
          <Link to={`/habitaciones/${h.idHabitacion}`}>
            Ver
          </Link>
          {puedeEditar && h.activo !== false && (
            <Link to={`/habitaciones/${h.idHabitacion}/editar`}>
              Editar
            </Link>
          )}
        </div>
      ),
    },
  ]

  const filtros = (
    <>
      <select aria-label="Filtrar por estado" className={claseSelect} value={idEstado} onChange={alCambiar(setIdEstado)}>
        <option value="">Todos los estados</option>
        {datos.estadosHab.map((e) => (
          <option key={e.idEstadoHabitacion} value={e.idEstadoHabitacion}>
            {e.nombre}
          </option>
        ))}
      </select>
      <select aria-label="Filtrar por tipo" className={claseSelect} value={idTipo} onChange={alCambiar(setIdTipo)}>
        <option value="">Todos los tipos</option>
        {datos.tiposHab.map((t) => (
          <option key={t.idTipoHabitacion} value={t.idTipoHabitacion}>
            {t.descripcion}
          </option>
        ))}
      </select>
      <select aria-label="Filtrar por piso" className={claseSelect} value={piso} onChange={alCambiar(setPiso)}>
        <option value="">Todos los pisos</option>
        {pisos.map((p) => (
          <option key={p} value={p}>
            Piso {p}
          </option>
        ))}
      </select>
      <label className="d-flex align-items-center small mb-0">
        <input
          type="checkbox"
          checked={incluirInactivas}
          onChange={(e) => {
            setIncluirInactivas(e.target.checked)
            setPagina(1)
          }}
          className="mr-2"
        />
        Mostrar inactivas
      </label>
    </>
  )

  const botonVista = (valor, etiqueta) => (
    <button
      type="button"
      onClick={() => setVista(valor)}
      aria-pressed={vista === valor}
      className={`btn btn-sm mb-0 ${vista === valor ? 'btn-primary' : 'btn-outline-primary'}`}
    >
      {etiqueta}
    </button>
  )

  // Vista de ocupación: tarjetas agrupadas por piso
  const porPiso = useMemo(() => {
    const grupos = {}
    for (const h of filas) (grupos[h.piso] ??= []).push(h)
    return Object.entries(grupos).sort(([a], [b]) => Number(a) - Number(b))
  }, [filas])

  return (
    <div>
      <div className="card card-body d-flex flex-column flex-md-row align-items-md-center justify-content-md-between">
        <div>
          <h1 className="h3 mb-0">Habitaciones</h1>
          <p className="text-muted mb-0">Habitaciones y camas del hospital.</p>
        </div>
        <div className="d-flex align-items-center acciones-grupo">
          <div className="btn-group" role="group" aria-label="Vista">
            {botonVista('tabla', 'Tabla')}
            {botonVista('ocupacion', 'Ocupación')}
          </div>
          {puedeCrear && (
            <Link
              to="/habitaciones/nueva"
              className="btn btn-primary btn-round"
            >
              Nueva habitación
            </Link>
          )}
        </div>
      </div>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />
      <ErrorAlert mensaje={errorLista?.message} />

      {vista === 'tabla' ? (
        <Table
          columnas={columnas}
          filas={filas.slice((pagina - 1) * TAMANO_PAGINA, pagina * TAMANO_PAGINA)}
          claveFila={(h) => h.idHabitacion}
          cargando={datos.cargando || cargandoLista}
          busqueda={texto}
          onBuscar={(v) => {
            setTexto(v)
            setPagina(1)
          }}
          placeholderBusqueda="Buscar por número"
          acciones={filtros}
          vacio={<EmptyState titulo="No hay habitaciones" descripcion="Ajuste los filtros o registre una habitación." />}
          pie={<Pagination pagina={pagina} totalElementos={filas.length} tamano={TAMANO_PAGINA} onCambiarPagina={setPagina} />}
        />
      ) : (
        <div>
          <div className="card card-body d-flex flex-row flex-wrap align-items-center acciones-filtros">{filtros}</div>
          {datos.cargando || cargandoLista ? (
            <Loading />
          ) : porPiso.length === 0 ? (
            <div className="card">
              <EmptyState titulo="No hay habitaciones" descripcion="Ajuste los filtros o registre una habitación." />
            </div>
          ) : (
            porPiso.map(([numeroPiso, habitaciones]) => (
              <section key={numeroPiso} className="card card-body">
                <h2 className="text-muted small font-weight-bold text-uppercase mb-3">Piso {numeroPiso}</h2>
                <div className="row">
                  {habitaciones.map((h) => (
                    <div key={h.idHabitacion} className="col-sm-6 col-lg-3 mb-3">
                    <Link to={`/habitaciones/${h.idHabitacion}`} className="ocupacion-habitacion">
                      <div className="d-flex align-items-center justify-content-between">
                        <span className="h5">{h.numero}</span>
                        <EstadoHabitacionBadge estado={datos.estadosHabPorId[h.idEstadoHabitacion]} fallback={`#${h.idEstadoHabitacion}`} />
                      </div>
                      <p className="text-muted small">
                        {datos.tiposHabPorId[h.idTipoHabitacion]?.descripcion ?? 'Sin tipo'} · Ocupación {ocupacion(h)}
                        {h.activo === false ? ' · Inactiva' : ''}
                      </p>
                    </Link>
                    </div>
                  ))}
                </div>
              </section>
            ))
          )}
        </div>
      )}
    </div>
  )
}
