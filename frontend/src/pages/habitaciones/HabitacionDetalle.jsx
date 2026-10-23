import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { cambiarEstadoHabitacion, desactivarHabitacion, obtenerHabitacion } from '../../api/habitaciones.js'
import { listarHospitalizacionesActivas } from '../../api/hospitalizaciones.js'
import { useAuth } from '../../auth/useAuth.js'
import { BadgeActivo } from '../../components/Badge.jsx'
import ConfirmDialog from '../../components/ConfirmDialog.jsx'
import Dato from '../../components/Dato.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import { EstadoHabitacionBadge } from '../../components/EstadosBadge.jsx'
import Loading from '../../components/Loading.jsx'
import { puede } from '../../config/menu.js'
import { useApi } from '../../hooks/useApi.js'
import { useDatosHospitalizacion } from '../../hooks/useDatosHospitalizacion.js'
import { normalizarError } from '../../utils/errores.js'
import { formatearFechaHora } from '../../utils/formato.js'
import { nombrePaciente } from '../citas/utilsCita.js'

export default function HabitacionDetalle() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { rol } = useAuth()
  const puedeEditar = puede(rol, 'habitaciones', 'editar')
  const puedeDesactivar = puede(rol, 'habitaciones', 'desactivar')
  const puedeCambiarEstado = puede(rol, 'habitaciones', 'cambiarEstado')

  const [exito, setExito] = useState(location.state?.mensaje ?? '')
  const [nuevoEstado, setNuevoEstado] = useState('')
  const [cambiando, setCambiando] = useState(false)
  const [errorEstado, setErrorEstado] = useState('')
  const [confirmando, setConfirmando] = useState(false)
  const [desactivando, setDesactivando] = useState(false)
  const [errorDesactivar, setErrorDesactivar] = useState('')

  const datos = useDatosHospitalizacion()
  const { datos: habitacion, error, cargando, recargar } = useApi(`habitacion|${id}`, () => obtenerHabitacion(id))
  const { datos: activas } = useApi(`hosp-activas-hab|${id}`, () => listarHospitalizacionesActivas().catch(() => []))

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const cambiarEstado = async () => {
    setCambiando(true)
    setErrorEstado('')
    try {
      await cambiarEstadoHabitacion(id, nuevoEstado)
      setNuevoEstado('')
      setExito('Estado de la habitación actualizado.')
      recargar()
    } catch (e) {
      setErrorEstado(normalizarError(e).message)
    } finally {
      setCambiando(false)
    }
  }

  const desactivar = async () => {
    setDesactivando(true)
    setErrorDesactivar('')
    try {
      await desactivarHabitacion(id)
      navigate('/habitaciones', { replace: true, state: { mensaje: 'La habitación fue desactivada.' } })
    } catch (e) {
      // 422: tiene pacientes hospitalizados o ingresos de emergencia activos
      setErrorDesactivar(normalizarError(e).message)
      setDesactivando(false)
    }
  }

  if (cargando || datos.cargando) return <Loading texto="Cargando habitación..." />

  if (error) {
    return (
      <div>
        <ErrorAlert titulo={error.status === 404 ? 'Habitación no encontrada' : 'No se pudo cargar la habitación'} mensaje={error.message} />
        <Link to="/habitaciones" className="volver-enlace">
          &larr; Volver a habitaciones
        </Link>
      </div>
    )
  }

  const h = habitacion
  const activa = h.activo !== false
  const pacientes = (activas ?? []).filter((x) => x.idHabitacion === h.idHabitacion)

  return (
    <div>
      <Link to="/habitaciones" className="volver-enlace">
        &larr; Volver a habitaciones
      </Link>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />

      <div className="card card-body d-flex flex-column flex-md-row align-items-md-center justify-content-md-between">
        <div>
          <h1 className="h3 mb-0">Habitación {h.numero}</h1>
          <p className="d-flex align-items-center acciones-grupo text-muted small mt-1">
            <EstadoHabitacionBadge estado={datos.estadosHabPorId[h.idEstadoHabitacion]} fallback={`#${h.idEstadoHabitacion}`} />
            {!activa && <BadgeActivo activo={false} />}
          </p>
        </div>
        <div className="acciones-grupo d-flex">
          {activa && puedeEditar && (
            <Link
              to={`/habitaciones/${h.idHabitacion}/editar`}
              className="btn btn-secondary btn-round"
            >
              Editar
            </Link>
          )}
          {activa && puedeDesactivar && (
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
          <Dato etiqueta="Número">{h.numero}</Dato>
          <Dato etiqueta="Piso">{String(h.piso)}</Dato>
          <Dato etiqueta="Tipo">{datos.tiposHabPorId[h.idTipoHabitacion]?.descripcion ?? ''}</Dato>
          <Dato etiqueta="Capacidad / ocupación">{`${pacientes.length} de ${h.capacidad}`}</Dato>
        </div>
      </div>

      {activa && puedeCambiarEstado && (
        <div className="card card-body">
          <h2 className="h5 mb-3">Cambiar estado</h2>
          <p className="text-muted small mb-3">
            Al ingresar o dar de alta pacientes el sistema actualiza el estado automáticamente; use esto solo para casos como mantenimiento.
          </p>
          <ErrorAlert mensaje={errorEstado} onCerrar={() => setErrorEstado('')} />
          <div className="d-flex flex-wrap align-items-end acciones-grupo mt-2">
            <div className="flex-grow-1">
              <label htmlFor="estado-hab" className="d-block font-weight-bold small mb-1">
                Nuevo estado
              </label>
              <select
                id="estado-hab"
                value={nuevoEstado}
                onChange={(e) => setNuevoEstado(e.target.value)}
                className="form-control"
              >
                <option value="">Seleccione...</option>
                {datos.estadosHab
                  .filter((e) => e.activo !== false && e.idEstadoHabitacion !== h.idEstadoHabitacion)
                  .map((e) => (
                    <option key={e.idEstadoHabitacion} value={e.idEstadoHabitacion}>
                      {e.nombre}
                    </option>
                  ))}
              </select>
            </div>
            <button
              type="button"
              onClick={cambiarEstado}
              disabled={!nuevoEstado || cambiando}
              className="btn btn-primary btn-round"
            >
              {cambiando ? 'Guardando...' : 'Aplicar'}
            </button>
          </div>
        </div>
      )}

      <div className="card card-body">
        <h2 className="h5 mb-3">Pacientes hospitalizados ahora</h2>
        {pacientes.length === 0 ? (
          <p className="text-muted mb-0">No hay pacientes hospitalizados en esta habitación.</p>
        ) : (
          <ul className="lista-divisoria">
            {pacientes.map((x) => (
              <li key={x.idHospitalizacion} className="d-flex align-items-center justify-content-between">
                <Link to={`/hospitalizaciones/${x.idHospitalizacion}`} className="font-weight-bold">
                  {nombrePaciente(datos.pacientesPorId[x.idPaciente], x.idPaciente)}
                </Link>
                <span className="text-muted small">Ingreso {formatearFechaHora(x.fechaIngreso)}</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      <ConfirmDialog
        abierto={confirmando}
        titulo="Desactivar habitación"
        mensaje="¿Desea desactivar esta habitación? No se permite si tiene pacientes hospitalizados o ingresos de emergencia activos."
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
