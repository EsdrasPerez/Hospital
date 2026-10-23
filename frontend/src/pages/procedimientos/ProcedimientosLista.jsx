import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { listarEmergencias } from '../../api/emergencias.js'
import { desactivarProcedimiento, listarProcedimientos } from '../../api/procedimientos.js'
import { useAuth } from '../../auth/useAuth.js'
import { BadgeActivo } from '../../components/Badge.jsx'
import ConfirmDialog from '../../components/ConfirmDialog.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Pagination from '../../components/Pagination.jsx'
import Table from '../../components/Table.jsx'
import { puede } from '../../config/menu.js'
import { mapaPor, useApi } from '../../hooks/useApi.js'
import { useDatosEmergencia } from '../../hooks/useDatosEmergencia.js'
import { normalizarError } from '../../utils/errores.js'
import { acortar, formatearFechaHora } from '../../utils/formato.js'
import { nombreMedico } from '../citas/utilsCita.js'
import { etiquetaPacienteEmergencia } from '../emergencias/utilsEmergencia.js'

const TAMANO_PAGINA = 10

export default function ProcedimientosLista() {
  const { rol } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const puedeCrear = puede(rol, 'procedimientos', 'crear')
  const puedeEditar = puede(rol, 'procedimientos', 'editar')
  const puedeDesactivar = puede(rol, 'procedimientos', 'desactivar')

  const [texto, setTexto] = useState('')
  const [incluirInactivos, setIncluirInactivos] = useState(false)
  const [pagina, setPagina] = useState(1)
  const [exito, setExito] = useState(location.state?.mensaje ?? '')
  const [aDesactivar, setADesactivar] = useState(null)
  const [desactivando, setDesactivando] = useState(false)
  const [errorDesactivar, setErrorDesactivar] = useState('')

  const datosEmergencia = useDatosEmergencia()
  const { datos, error, cargando, recargar } = useApi('procedimientos', async () => {
    const [procedimientos, emergencias] = await Promise.all([listarProcedimientos(), listarEmergencias().catch(() => [])])
    return { procedimientos, emergenciasPorId: mapaPor(emergencias, 'idEmergencia') }
  })

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const describirEmergencia = (idEmergencia) => {
    const e = datos?.emergenciasPorId[idEmergencia]
    return e ? etiquetaPacienteEmergencia(e, datosEmergencia.pacientesPorId) : `Emergencia #${idEmergencia}`
  }

  const filas = useMemo(() => {
    const busqueda = texto.trim().toLowerCase()
    return (datos?.procedimientos ?? [])
      .filter((p) => incluirInactivos || p.activo !== false)
      .filter((p) => !busqueda || `${p.descripcion} ${p.observaciones ?? ''}`.toLowerCase().includes(busqueda))
      .sort((a, b) => String(b.fechaHora).localeCompare(String(a.fechaHora)))
  }, [datos, texto, incluirInactivos])

  const desactivar = async () => {
    setDesactivando(true)
    setErrorDesactivar('')
    try {
      await desactivarProcedimiento(aDesactivar.idProcedimiento)
      setADesactivar(null)
      setExito('El procedimiento fue desactivado.')
      recargar()
    } catch (e) {
      setErrorDesactivar(normalizarError(e).message)
    } finally {
      setDesactivando(false)
    }
  }

  const columnas = [
    { clave: 'fechaHora', titulo: 'Fecha y hora', render: (p) => formatearFechaHora(p.fechaHora) },
    { clave: 'descripcion', titulo: 'Procedimiento', render: (p) => acortar(p.descripcion, 50) },
    {
      clave: 'emergencia',
      titulo: 'Emergencia',
      render: (p) => (
        <Link to={`/emergencia/${p.idEmergencia}`}>
          {describirEmergencia(p.idEmergencia)}
        </Link>
      ),
    },
    { clave: 'medico', titulo: 'Médico', render: (p) => nombreMedico(datosEmergencia.medicosPorId[p.idMedico], p.idMedico) },
    { clave: 'activo', titulo: 'Estado', render: (p) => <BadgeActivo activo={p.activo} /> },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      className: 'text-right',
      render: (p) => (
        <div className="tabla-acciones d-flex justify-content-end">
          {puedeEditar && p.activo !== false && (
            <Link to={`/procedimientos/${p.idProcedimiento}/editar`}>
              Editar
            </Link>
          )}
          {puedeDesactivar && p.activo !== false && (
            <button
              type="button"
              onClick={() => {
                setErrorDesactivar('')
                setADesactivar(p)
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
          <h1 className="h3 mb-0">Procedimientos de emergencia</h1>
          <p className="text-muted mb-0">Procedimientos realizados a pacientes en emergencia.</p>
        </div>
        {puedeCrear && (
          <Link
            to="/procedimientos/nuevo"
            className="btn btn-primary btn-round"
          >
            Nuevo procedimiento
          </Link>
        )}
      </div>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />
      <ErrorAlert mensaje={error?.message} />

      <Table
        columnas={columnas}
        filas={filas.slice((pagina - 1) * TAMANO_PAGINA, pagina * TAMANO_PAGINA)}
        claveFila={(p) => p.idProcedimiento}
        cargando={cargando}
        busqueda={texto}
        onBuscar={(v) => {
          setTexto(v)
          setPagina(1)
        }}
        placeholderBusqueda="Buscar por procedimiento u observaciones"
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
        vacio={<EmptyState titulo="No hay procedimientos" descripcion="Los procedimientos se registran sobre un ingreso a emergencia." />}
        pie={<Pagination pagina={pagina} totalElementos={filas.length} tamano={TAMANO_PAGINA} onCambiarPagina={setPagina} />}
      />

      <ConfirmDialog
        abierto={!!aDesactivar}
        titulo="Desactivar procedimiento"
        mensaje="¿Desea desactivar este procedimiento? El registro se conserva en el historial."
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
