import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { Alert, Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap'
import { darAltaEmergencia, desactivarEmergencia, obtenerEmergencia } from '../../api/emergencias.js'
import { desactivarProcedimiento, listarProcedimientos } from '../../api/procedimientos.js'
import { useAuth } from '../../auth/useAuth.js'
import Badge, { BadgeActivo } from '../../components/Badge.jsx'
import ConfirmDialog from '../../components/ConfirmDialog.jsx'
import Dato from '../../components/Dato.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Loading from '../../components/Loading.jsx'
import TriajeBadge from '../../components/TriajeBadge.jsx'
import { puede } from '../../config/menu.js'
import { useApi } from '../../hooks/useApi.js'
import { useDatosEmergencia } from '../../hooks/useDatosEmergencia.js'
import { normalizarError } from '../../utils/errores.js'
import { acortar, deInputFechaHora, formatearFechaHora } from '../../utils/formato.js'
import { nombreMedico } from '../citas/utilsCita.js'
import { estadoEmergencia, etiquetaPacienteEmergencia, nombreHabitacion } from './utilsEmergencia.js'

/** Diálogo para dar de alta: la fecha y hora son opcionales (el servidor usa la hora actual). */
function DarAltaDialog({ emergencia, onCerrar, onListo }) {
  const [fechaHora, setFechaHora] = useState('')
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  const confirmar = async () => {
    setGuardando(true)
    setError('')
    try {
      await darAltaEmergencia(emergencia.idEmergencia, deInputFechaHora(fechaHora))
      onListo()
    } catch (e) {
      // 409: el alta ya estaba registrada; 422: emergencia inactiva o alta anterior al ingreso
      setError(normalizarError(e).message)
      setGuardando(false)
    }
  }

  return (
    <Modal isOpen toggle={guardando ? undefined : onCerrar} centered fade={false}>
      <ModalHeader closeAriaLabel="Cerrar" toggle={guardando ? undefined : onCerrar}>Dar de alta</ModalHeader>
      <ModalBody>
        <p>Ingreso: {formatearFechaHora(emergencia.fechaHoraIngreso)}. Si no indica fecha y hora, se usa el momento actual.</p>
        <label htmlFor="fecha-alta" className="d-block font-weight-bold small mb-1">
          Fecha y hora de alta (opcional)
        </label>
        <input
          id="fecha-alta"
          type="datetime-local"
          value={fechaHora}
          min={emergencia.fechaHoraIngreso?.slice(0, 16)}
          onChange={(e) => setFechaHora(e.target.value)}
          className="form-control"
        />
        {error && (
          <Alert color="danger" fade={false} role="alert" className="mt-3 mb-0">
            {error}
          </Alert>
        )}
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" outline onClick={onCerrar} disabled={guardando}>
          Cancelar
        </Button>
        <Button color="primary" onClick={confirmar} disabled={guardando}>
          {guardando ? 'Guardando...' : 'Confirmar alta'}
        </Button>
      </ModalFooter>
    </Modal>
  )
}

export default function EmergenciaDetalle() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { rol } = useAuth()
  const puedeEditar = puede(rol, 'emergencia', 'editar')
  const puedeDesactivar = puede(rol, 'emergencia', 'desactivar')
  const puedeVerProcedimientos = puede(rol, 'procedimientos', 'ver')
  const puedeCrearProcedimiento = puede(rol, 'procedimientos', 'crear')
  const puedeEditarProcedimiento = puede(rol, 'procedimientos', 'editar')
  const puedeDesactivarProcedimiento = puede(rol, 'procedimientos', 'desactivar')

  const [exito, setExito] = useState(location.state?.mensaje ?? '')
  const [dandoAlta, setDandoAlta] = useState(false)
  const [confirmando, setConfirmando] = useState(false)
  const [desactivando, setDesactivando] = useState(false)
  const [errorDesactivar, setErrorDesactivar] = useState('')
  const [aQuitar, setAQuitar] = useState(null)
  const [quitando, setQuitando] = useState(false)
  const [errorQuitar, setErrorQuitar] = useState('')

  const datos = useDatosEmergencia()
  const { datos: emergencia, error, cargando, recargar } = useApi(`emergencia|${id}`, () => obtenerEmergencia(id))
  // No hay filtro por emergencia en el backend: se filtra la lista completa (solo roles con lectura).
  const { datos: procedimientos, recargar: recargarProcedimientos } = useApi(`procedimientos-de|${id}|${puedeVerProcedimientos}`, async () => {
    if (!puedeVerProcedimientos) return []
    const todos = await listarProcedimientos()
    return todos.filter((p) => String(p.idEmergencia) === String(id))
  })

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const desactivar = async () => {
    setDesactivando(true)
    setErrorDesactivar('')
    try {
      await desactivarEmergencia(id)
      navigate('/emergencia', { replace: true, state: { mensaje: 'El ingreso fue desactivado.' } })
    } catch (e) {
      // 409: tiene procedimientos activos asociados
      setErrorDesactivar(normalizarError(e).message)
      setDesactivando(false)
    }
  }

  const quitarProcedimiento = async () => {
    setQuitando(true)
    setErrorQuitar('')
    try {
      await desactivarProcedimiento(aQuitar.idProcedimiento)
      setAQuitar(null)
      setExito('El procedimiento fue desactivado.')
      recargarProcedimientos()
    } catch (e) {
      setErrorQuitar(normalizarError(e).message)
    } finally {
      setQuitando(false)
    }
  }

  if (cargando || datos.cargando) return <Loading texto="Cargando ingreso..." />

  if (error) {
    return (
      <div>
        <ErrorAlert titulo={error.status === 404 ? 'Ingreso no encontrado' : 'No se pudo cargar el ingreso'} mensaje={error.message} />
        <Link to="/emergencia" className="volver-enlace">
          &larr; Volver a emergencias
        </Link>
      </div>
    )
  }

  const e = emergencia
  const activa = e.activo !== false
  const abierta = activa && !e.fechaHoraAlta
  const est = estadoEmergencia(e)

  return (
    <div>
      <Link to="/emergencia" className="volver-enlace">
        &larr; Volver a emergencias
      </Link>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />

      <div className="card card-body d-flex flex-column flex-md-row align-items-md-center justify-content-md-between">
        <div>
          <h1 className="h3 mb-0">{etiquetaPacienteEmergencia(e, datos.pacientesPorId)}</h1>
          <p className="d-flex align-items-center acciones-grupo text-muted small mt-1">
            <Badge tono={est.tono}>{est.texto}</Badge>
            <TriajeBadge triaje={datos.triajesPorId[e.idTriaje]} niveles={datos.niveles} fallback={`Triaje #${e.idTriaje}`} />
            {!activa && <BadgeActivo activo={false} />}
          </p>
        </div>
        <div className="acciones-grupo d-flex flex-wrap">
          {abierta && puedeEditar && (
            <button
              type="button"
              onClick={() => setDandoAlta(true)}
              className="btn btn-primary btn-round"
            >
              Dar de alta
            </button>
          )}
          {activa && puedeEditar && (
            <Link
              to={`/emergencia/${e.idEmergencia}/editar`}
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
          <Dato etiqueta="Paciente">
            {e.idPaciente ? (
              <Link to={`/pacientes/${e.idPaciente}`}>
                {etiquetaPacienteEmergencia(e, datos.pacientesPorId)}
              </Link>
            ) : (
              etiquetaPacienteEmergencia(e, datos.pacientesPorId)
            )}
          </Dato>
          <Dato etiqueta="Nivel de triaje">{datos.triajesPorId[e.idTriaje]?.nivelDeEmergencia ?? `#${e.idTriaje}`}</Dato>
          <Dato etiqueta="Médico">{e.idMedico ? nombreMedico(datos.medicosPorId[e.idMedico], e.idMedico) : ''}</Dato>
          <Dato etiqueta="Ingreso">{formatearFechaHora(e.fechaHoraIngreso)}</Dato>
          <Dato etiqueta="Alta">{e.fechaHoraAlta ? formatearFechaHora(e.fechaHoraAlta) : ''}</Dato>
          <Dato etiqueta="Habitación / box">
            {e.idHabitacion ? nombreHabitacion(datos.habitacionesPorId[e.idHabitacion], e.idHabitacion) : ''}
          </Dato>
          <Dato etiqueta="Motivo" className="col-12">
            {e.motivo}
          </Dato>
          <Dato etiqueta="Signos vitales" className="col-12">
            {e.signosVitales}
          </Dato>
        </div>
      </div>

      {puedeVerProcedimientos && (
        <div className="card card-body">
          <div className="d-flex align-items-center justify-content-between mb-3">
            <h2 className="h5">Procedimientos realizados</h2>
            {puedeCrearProcedimiento &&
              (abierta ? (
                <Link to={`/procedimientos/nuevo?idEmergencia=${e.idEmergencia}`} className="font-weight-bold">
                  Agregar procedimiento
                </Link>
              ) : (
                <span className="text-muted small">No se pueden agregar procedimientos a un ingreso cerrado.</span>
              ))}
          </div>
          {(procedimientos ?? []).length === 0 ? (
            <p className="text-muted mb-0">No hay procedimientos registrados.</p>
          ) : (
            <ul className="lista-divisoria">
              {procedimientos.map((p) => (
                <li key={p.idProcedimiento} className="d-flex flex-column flex-sm-row align-items-sm-center justify-content-sm-between">
                  <div>
                    <p className="font-weight-bold">
                      {p.descripcion} {p.activo === false && <BadgeActivo activo={false} />}
                    </p>
                    <p className="text-muted small">
                      {formatearFechaHora(p.fechaHora)} · {nombreMedico(datos.medicosPorId[p.idMedico], p.idMedico)}
                      {p.observaciones ? ` · ${acortar(p.observaciones, 80)}` : ''}
                    </p>
                  </div>
                  {p.activo !== false && (
                    <div className="acciones-grupo d-flex">
                      {puedeEditarProcedimiento && (
                        <Link to={`/procedimientos/${p.idProcedimiento}/editar`}>
                          Editar
                        </Link>
                      )}
                      {puedeDesactivarProcedimiento && (
                        <button
                          type="button"
                          onClick={() => {
                            setErrorQuitar('')
                            setAQuitar(p)
                          }}
                          className="btn btn-link text-danger p-0"
                        >
                          Desactivar
                        </button>
                      )}
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {dandoAlta && (
        <DarAltaDialog
          emergencia={e}
          onCerrar={() => setDandoAlta(false)}
          onListo={() => {
            setDandoAlta(false)
            setExito('El paciente fue dado de alta.')
            recargar()
          }}
        />
      )}

      <ConfirmDialog
        abierto={!!aQuitar}
        titulo="Desactivar procedimiento"
        mensaje="¿Desea desactivar este procedimiento? El registro se conserva en el historial."
        textoConfirmar="Desactivar"
        peligro
        cargando={quitando}
        error={errorQuitar}
        onConfirmar={quitarProcedimiento}
        onCancelar={() => setAQuitar(null)}
      />
      <ConfirmDialog
        abierto={confirmando}
        titulo="Desactivar ingreso"
        mensaje="¿Desea desactivar este ingreso a emergencia? No se permite si tiene procedimientos activos."
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
