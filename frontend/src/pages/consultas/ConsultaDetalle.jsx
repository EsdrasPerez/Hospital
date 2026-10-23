import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { obtenerCita } from '../../api/citas.js'
import {
  asociarDiagnostico,
  desactivarConsulta,
  listarDiagnosticosDeConsulta,
  obtenerConsulta,
  quitarDiagnostico,
} from '../../api/consultas.js'
import { listarDiagnosticos } from '../../api/diagnosticos.js'
import { useAuth } from '../../auth/useAuth.js'
import { BadgeActivo } from '../../components/Badge.jsx'
import ConfirmDialog from '../../components/ConfirmDialog.jsx'
import Dato from '../../components/Dato.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Loading from '../../components/Loading.jsx'
import { puede } from '../../config/menu.js'
import { mapaPor, useApi } from '../../hooks/useApi.js'
import { useDatosCita } from '../../hooks/useDatosCita.js'
import { normalizarError } from '../../utils/errores.js'
import { formatearFecha, formatearFechaHora, formatearHora } from '../../utils/formato.js'
import { nombreMedico, nombrePaciente } from '../citas/utilsCita.js'

export default function ConsultaDetalle() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { rol } = useAuth()
  const puedeEditar = puede(rol, 'consultas', 'editar')
  const puedeDesactivar = puede(rol, 'consultas', 'desactivar')
  const puedeAsociar = puede(rol, 'consultas', 'asociarDiagnostico')

  const [exito, setExito] = useState(location.state?.mensaje ?? '')
  const [errorAccion, setErrorAccion] = useState('')
  const [nuevoDiagnostico, setNuevoDiagnostico] = useState('')
  const [asociando, setAsociando] = useState(false)
  const [aQuitar, setAQuitar] = useState(null)
  const [quitando, setQuitando] = useState(false)
  const [errorQuitar, setErrorQuitar] = useState('')
  const [confirmando, setConfirmando] = useState(false)
  const [desactivando, setDesactivando] = useState(false)
  const [errorDesactivar, setErrorDesactivar] = useState('')

  const datosCita = useDatosCita()
  const { datos, error, cargando, recargar } = useApi(`consulta|${id}`, async () => {
    const consulta = await obtenerConsulta(id)
    const [cita, asociaciones, diagnosticos] = await Promise.all([
      obtenerCita(consulta.idCita).catch(() => null),
      listarDiagnosticosDeConsulta(id),
      listarDiagnosticos().catch(() => []),
    ])
    return { consulta, cita, asociaciones, diagnosticos }
  })

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  if (cargando || datosCita.cargando) return <Loading texto="Cargando consulta..." />

  if (error) {
    return (
      <div>
        <ErrorAlert titulo={error.status === 404 ? 'Consulta no encontrada' : 'No se pudo cargar la consulta'} mensaje={error.message} />
        <Link to="/consultas" className="volver-enlace">
          &larr; Volver a consultas
        </Link>
      </div>
    )
  }

  const { consulta, cita, asociaciones, diagnosticos } = datos
  const activa = consulta.activo !== false
  const diagnosticosPorId = mapaPor(diagnosticos, 'idDiagnostico')
  const idsAsociados = new Set(asociaciones.map((a) => a.idDiagnostico))
  const disponibles = diagnosticos.filter((d) => d.activo !== false && !idsAsociados.has(d.idDiagnostico))

  const asociar = async () => {
    setAsociando(true)
    setErrorAccion('')
    try {
      await asociarDiagnostico(Number(nuevoDiagnostico), consulta.idConsulta)
      setNuevoDiagnostico('')
      setExito('Diagnóstico asociado a la consulta.')
      recargar()
    } catch (e) {
      setErrorAccion(normalizarError(e).message)
    } finally {
      setAsociando(false)
    }
  }

  const quitar = async () => {
    setQuitando(true)
    setErrorQuitar('')
    try {
      await quitarDiagnostico(aQuitar.idDiagnostico, consulta.idConsulta)
      setAQuitar(null)
      setExito('Diagnóstico quitado de la consulta.')
      recargar()
    } catch (e) {
      setErrorQuitar(normalizarError(e).message)
    } finally {
      setQuitando(false)
    }
  }

  const desactivar = async () => {
    setDesactivando(true)
    setErrorDesactivar('')
    try {
      await desactivarConsulta(id)
      navigate('/consultas', { replace: true, state: { mensaje: 'La consulta fue desactivada.' } })
    } catch (e) {
      // 409: tiene diagnósticos asociados (hay que quitarlos primero)
      setErrorDesactivar(normalizarError(e).message)
      setDesactivando(false)
    }
  }

  const paciente = cita ? datosCita.pacientesPorId[cita.idPaciente] : null
  const medico = cita ? datosCita.medicosPorId[cita.idMedico] : null

  return (
    <div>
      <Link to="/consultas" className="volver-enlace">
        &larr; Volver a consultas
      </Link>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />

      <div className="card card-body d-flex flex-column flex-md-row align-items-md-center justify-content-md-between">
        <div>
          <h1 className="h3 mb-0">Consulta #{consulta.idConsulta}</h1>
          <p className="d-flex align-items-center acciones-grupo text-muted small mt-1">
            {formatearFechaHora(consulta.fechaHora)} {!activa && <BadgeActivo activo={false} />}
          </p>
        </div>
        <div className="acciones-grupo d-flex">
          {activa && puedeEditar && (
            <Link
              to={`/consultas/${consulta.idConsulta}/editar`}
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
        <h2 className="h5 mb-3">Cita asociada</h2>
        <div className="row">
          <Dato etiqueta="Cita">
            <Link to={`/citas/${consulta.idCita}`}>
              Cita #{consulta.idCita}
            </Link>
          </Dato>
          <Dato etiqueta="Paciente">{cita ? nombrePaciente(paciente, cita.idPaciente) : ''}</Dato>
          <Dato etiqueta="Médico">{cita ? nombreMedico(medico, cita.idMedico) : ''}</Dato>
          <Dato etiqueta="Fecha de la cita">
            {cita ? `${formatearFecha(cita.fecha)} ${formatearHora(cita.hora)}` : ''}
          </Dato>
        </div>
      </div>

      <div className="card card-body">
        <h2 className="h5 mb-3">Datos clínicos</h2>
        <div className="row">
          <Dato etiqueta="Peso">{consulta.peso != null ? `${consulta.peso} kg` : ''}</Dato>
          <Dato etiqueta="Talla">{consulta.talla != null ? `${consulta.talla} cm` : ''}</Dato>
          <Dato etiqueta="Temperatura">{consulta.temperatura != null ? `${consulta.temperatura} °C` : ''}</Dato>
          <Dato etiqueta="Presión arterial">{consulta.presionArterial}</Dato>
          <Dato etiqueta="Frecuencia cardíaca">
            {consulta.frecuenciaCardiaca != null ? `${consulta.frecuenciaCardiaca} lpm` : ''}
          </Dato>
          <Dato etiqueta="Síntomas" className="col-12">
            {consulta.sintomas}
          </Dato>
        </div>
      </div>

      <div className="card card-body">
        <h2 className="h5 mb-3">Diagnósticos de la consulta</h2>
        <ErrorAlert mensaje={errorAccion} onCerrar={() => setErrorAccion('')} />
        {asociaciones.length === 0 ? (
          <p className="text-muted mb-0">Todavía no se asoció ningún diagnóstico.</p>
        ) : (
          <ul className="lista-divisoria">
            {asociaciones.map((a) => {
              const d = diagnosticosPorId[a.idDiagnostico]
              return (
                <li key={a.idDiagnostico} className="d-flex align-items-center justify-content-between">
                  <Link to={`/diagnosticos/${a.idDiagnostico}`} className="font-weight-bold">
                    {d?.descripcion ?? `Diagnóstico #${a.idDiagnostico}`}
                  </Link>
                  {activa && puedeAsociar && (
                    <button
                      type="button"
                      onClick={() => {
                        setErrorQuitar('')
                        setAQuitar(a)
                      }}
                      className="btn btn-link text-danger p-0"
                    >
                      Quitar
                    </button>
                  )}
                </li>
              )
            })}
          </ul>
        )}

        {activa && puedeAsociar && (
          <div className="d-flex flex-wrap align-items-end border-top pt-3 mt-3 acciones-grupo">
            <div className="flex-grow-1">
              <label htmlFor="nuevo-diagnostico" className="d-block font-weight-bold small mb-1">
                Asociar un diagnóstico
              </label>
              <select
                id="nuevo-diagnostico"
                value={nuevoDiagnostico}
                onChange={(e) => setNuevoDiagnostico(e.target.value)}
                className="form-control"
              >
                <option value="">{disponibles.length ? 'Seleccione...' : 'No hay diagnósticos disponibles'}</option>
                {disponibles.map((d) => (
                  <option key={d.idDiagnostico} value={d.idDiagnostico}>
                    {d.descripcion}
                  </option>
                ))}
              </select>
            </div>
            <button
              type="button"
              onClick={asociar}
              disabled={!nuevoDiagnostico || asociando}
              className="btn btn-primary btn-round"
            >
              {asociando ? 'Asociando...' : 'Asociar'}
            </button>
            <Link to="/diagnosticos/nuevo" className="font-weight-bold">
              Crear diagnóstico
            </Link>
          </div>
        )}
      </div>

      <ConfirmDialog
        abierto={!!aQuitar}
        titulo="Quitar diagnóstico"
        mensaje="¿Desea quitar este diagnóstico de la consulta? El diagnóstico no se elimina, solo se desvincula."
        textoConfirmar="Quitar"
        peligro
        cargando={quitando}
        error={errorQuitar}
        onConfirmar={quitar}
        onCancelar={() => setAQuitar(null)}
      />
      <ConfirmDialog
        abierto={confirmando}
        titulo="Desactivar consulta"
        mensaje="¿Desea desactivar esta consulta? Si tiene diagnósticos asociados deberá quitarlos primero."
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
