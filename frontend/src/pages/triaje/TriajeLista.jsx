import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { listarNivelesUrgencia } from '../../api/catalogos.js'
import { desactivarTriaje, listarTriajes } from '../../api/triaje.js'
import { useAuth } from '../../auth/useAuth.js'
import { BadgeActivo } from '../../components/Badge.jsx'
import ConfirmDialog from '../../components/ConfirmDialog.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Table from '../../components/Table.jsx'
import TriajeBadge from '../../components/TriajeBadge.jsx'
import { puede } from '../../config/menu.js'
import { useApi } from '../../hooks/useApi.js'
import { normalizarError } from '../../utils/errores.js'

export default function TriajeLista() {
  const { rol } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const puedeCrear = puede(rol, 'triaje', 'crear')
  const puedeEditar = puede(rol, 'triaje', 'editar')
  const puedeDesactivar = puede(rol, 'triaje', 'desactivar')

  const [texto, setTexto] = useState('')
  const [incluirInactivos, setIncluirInactivos] = useState(false)
  const [exito, setExito] = useState(location.state?.mensaje ?? '')
  const [aDesactivar, setADesactivar] = useState(null)
  const [desactivando, setDesactivando] = useState(false)
  const [errorDesactivar, setErrorDesactivar] = useState('')

  const { datos, error, cargando, recargar } = useApi('triajes', listarTriajes)
  const { datos: niveles } = useApi('niveles-urgencia', () => listarNivelesUrgencia().catch(() => []))

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const filas = useMemo(() => {
    const busqueda = texto.trim().toLowerCase()
    return (datos ?? [])
      .filter((t) => incluirInactivos || t.activo !== false)
      .filter((t) => !busqueda || `${t.nivelDeEmergencia} ${t.descripcion ?? ''}`.toLowerCase().includes(busqueda))
  }, [datos, texto, incluirInactivos])

  const desactivar = async () => {
    setDesactivando(true)
    setErrorDesactivar('')
    try {
      await desactivarTriaje(aDesactivar.idTriaje)
      setADesactivar(null)
      setExito(`El nivel «${aDesactivar.nivelDeEmergencia}» fue desactivado.`)
      recargar()
    } catch (e) {
      setErrorDesactivar(normalizarError(e).message)
    } finally {
      setDesactivando(false)
    }
  }

  const columnas = [
    { clave: 'nivel', titulo: 'Nivel', render: (t) => <TriajeBadge triaje={t} niveles={niveles} /> },
    { clave: 'descripcion', titulo: 'Descripción', render: (t) => t.descripcion || '—' },
    { clave: 'activo', titulo: 'Estado', render: (t) => <BadgeActivo activo={t.activo} /> },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      className: 'text-right',
      render: (t) => (
        <div className="tabla-acciones d-flex justify-content-end">
          {puedeEditar && t.activo !== false && (
            <Link to={`/triaje/${t.idTriaje}/editar`}>
              Editar
            </Link>
          )}
          {puedeDesactivar && t.activo !== false && (
            <button
              type="button"
              onClick={() => {
                setErrorDesactivar('')
                setADesactivar(t)
              }}
              className="btn btn-link text-danger p-0"
            >
              Desactivar
            </button>
          )}
        </div>
      ),
    },
  ]

  return (
    <div>
      <div className="card card-body d-flex flex-column flex-md-row align-items-md-center justify-content-md-between">
        <div>
          <h1 className="h3 mb-0">Niveles de triaje</h1>
          <p className="text-muted mb-0">Catálogo de niveles para clasificar la gravedad al ingresar a emergencia.</p>
        </div>
        {puedeCrear && (
          <Link
            to="/triaje/nuevo"
            className="btn btn-primary btn-round"
          >
            Nuevo nivel
          </Link>
        )}
      </div>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />
      <ErrorAlert mensaje={error?.message} />

      <Table
        columnas={columnas}
        filas={filas}
        claveFila={(t) => t.idTriaje}
        cargando={cargando}
        busqueda={texto}
        onBuscar={setTexto}
        placeholderBusqueda="Buscar por nivel o descripción"
        acciones={
          <label className="d-flex align-items-center small mb-0">
            <input
              type="checkbox"
              checked={incluirInactivos}
              onChange={(e) => setIncluirInactivos(e.target.checked)}
              className="mr-2"
            />
            Mostrar inactivos
          </label>
        }
        vacio={<EmptyState titulo="No hay niveles de triaje" descripcion="Registre los niveles (Rojo, Naranja, Amarillo...) para poder ingresar pacientes." />}
      />

      <ConfirmDialog
        abierto={!!aDesactivar}
        titulo="Desactivar nivel de triaje"
        mensaje="¿Desea desactivar este nivel? Dejará de ofrecerse al registrar ingresos, pero se conserva en el historial."
        textoConfirmar="Desactivar"
        peligro
        cargando={desactivando}
        error={errorDesactivar}
        onConfirmar={desactivar}
        onCancelar={() => setADesactivar(null)}
      />
    </div>
  )
}
