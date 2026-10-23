import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { listarAsignacionesEspecialidad, listarMedicos } from '../../api/administracion.js'
import client from '../../api/client.js'
import { RUTAS } from '../../api/rutas.js'
import { useAuth } from '../../auth/useAuth.js'
import { BadgeActivo } from '../../components/Badge.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Pagination from '../../components/Pagination.jsx'
import Table from '../../components/Table.jsx'
import { puede } from '../../config/menu.js'
import { mapaPor, useApi } from '../../hooks/useApi.js'
import { nombreMedico } from '../citas/utilsCita.js'

const TAMANO_PAGINA = 10

/**
 * Pestaña "Médicos" de Personal: búsqueda y filtro de activos en el servidor. `criterio` (nombre, apellidos o N.º de colegiado) y
 * `soloActivos` son combinables: con "Mostrar inactivos" la búsqueda también incluye a los médicos inactivos.
 */
export default function MedicosLista() {
  const { rol } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const puedeCrear = puede(rol, 'personal', 'crear')

  const [texto, setTexto] = useState('')
  const [criterio, setCriterio] = useState('')
  const [incluirInactivos, setIncluirInactivos] = useState(false)
  const [pagina, setPagina] = useState(1)
  const [exito, setExito] = useState(location.state?.mensaje ?? '')

  useEffect(() => {
    const t = setTimeout(() => setCriterio(texto.trim()), 300)
    return () => clearTimeout(t)
  }, [texto])

  const { datos, error, cargando } = useApi(`medicos|${criterio}|${incluirInactivos}`, () => listarMedicos({ criterio, soloActivos: !incluirInactivos }))
  // Especialidades de cada médico (lecturas abiertas a cualquier usuario autenticado)
  const { datos: extra } = useApi('medicos-especialidades', async () => {
    const [asignaciones, especialidades] = await Promise.all([
      listarAsignacionesEspecialidad().catch(() => []),
      client.get(RUTAS.especialidadesMedicas).then((r) => r.data).catch(() => []),
    ])
    return { asignaciones, especialidadesPorId: mapaPor(especialidades, 'idEspecialidad') }
  })

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname + location.search, { replace: true, state: null })
  }, [location.state, location.pathname, location.search, navigate])

  const especialidadesDe = (idMedico) =>
    (extra?.asignaciones ?? [])
      .filter((a) => a.idMedico === idMedico)
      .map((a) => extra.especialidadesPorId[a.idEspecialidad]?.nombre ?? `#${a.idEspecialidad}`)
      .join(', ')

  const filas = useMemo(() => [...(datos ?? [])].sort((a, b) => nombreMedico(a).localeCompare(nombreMedico(b), 'es')), [datos])

  const columnas = [
    {
      clave: 'nombre',
      titulo: 'Médico',
      render: (m) => (
        <>
          <Link to={`/personal/medicos/${m.idMedico}`} className="font-weight-bold">
            {nombreMedico(m)}
          </Link>
          <div className="text-muted small">Colegiado {m.noColegiado}</div>
        </>
      ),
    },
    { clave: 'contacto', titulo: 'Contacto', render: (m) => [m.telefono, m.email].filter(Boolean).join(' · ') || '—' },
    { clave: 'especialidades', titulo: 'Especialidades', render: (m) => especialidadesDe(m.idMedico) || '—' },
    { clave: 'estado', titulo: 'Estado', render: (m) => <BadgeActivo activo={m.estado} /> },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      className: 'text-right',
      render: (m) => (
        <div className="tabla-acciones d-flex flex-wrap justify-content-end">
          <Link to={`/personal/medicos/${m.idMedico}`}>Ver</Link>
          {puede(rol, 'personal', 'editar') && <Link to={`/personal/medicos/${m.idMedico}/editar`}>Editar</Link>}
        </div>
      ),
    },
  ]

  return (
    <div>
      <div className="d-flex flex-column flex-md-row align-items-md-center justify-content-md-between mb-3">
        <div>
          <h2 className="h4 mb-1">Médicos</h2>
          <p className="text-muted small mb-0">Búsqueda por nombre, apellidos o número de colegiado.</p>
        </div>
        {puedeCrear && (
          <Link to="/personal/medicos/nuevo" className="btn btn-primary btn-round mt-2 mt-md-0">
            Nuevo médico
          </Link>
        )}
      </div>
      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />
      <ErrorAlert mensaje={error?.message} />
      <Table
        columnas={columnas}
        filas={filas.slice((pagina - 1) * TAMANO_PAGINA, pagina * TAMANO_PAGINA)}
        claveFila={(m) => m.idMedico}
        cargando={cargando}
        busqueda={texto}
        onBuscar={(v) => {
          setTexto(v)
          setPagina(1)
        }}
        placeholderBusqueda="Buscar por nombre, apellidos o colegiado"
        acciones={
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
        }
        vacio={<EmptyState titulo="No hay médicos" descripcion="Ajuste la búsqueda o registre un médico." />}
        pie={<Pagination pagina={pagina} totalElementos={filas.length} tamano={TAMANO_PAGINA} onCambiarPagina={setPagina} />}
      />
    </div>
  )
}
