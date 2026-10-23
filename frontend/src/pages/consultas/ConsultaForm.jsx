import { useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { listarCitas } from '../../api/citas.js'
import { actualizarConsulta, crearConsulta, listarConsultas, obtenerConsulta } from '../../api/consultas.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Loading from '../../components/Loading.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useDatosCita } from '../../hooks/useDatosCita.js'
import { normalizarError } from '../../utils/errores.js'
import { formatearFecha, formatearHora, hoyISO } from '../../utils/formato.js'
import { nombreMedico, nombrePaciente } from '../citas/utilsCita.js'
import {
  FORM_CONSULTA_VACIO,
  consultaAFormulario,
  formularioAConsulta,
  validarConsulta,
} from './utilsConsulta.js'

export default function ConsultaForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const datosCita = useDatosCita()

  const { datos, error: errorCarga, cargando } = useApi(`consulta-form|${id ?? 'nueva'}`, async () => {
    const [consulta, citas, consultas] = await Promise.all([
      esEdicion ? obtenerConsulta(id) : null,
      listarCitas(),
      listarConsultas(),
    ])
    return { consulta, citas, consultas }
  })

  const [cambios, setCambios] = useState({})
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [guardando, setGuardando] = useState(false)

  if (cargando || datosCita.cargando) return <Loading texto="Cargando..." />

  if (errorCarga) {
    return (
      <div>
        <ErrorAlert
          titulo={errorCarga.status === 404 ? 'Consulta no encontrada' : 'No se pudo cargar el formulario'}
          mensaje={errorCarga.message}
        />
        <Link to="/consultas" className="volver-enlace">
          &larr; Volver a consultas
        </Link>
      </div>
    )
  }

  const { consulta, citas, consultas } = datos
  const iniciales = consulta
    ? consultaAFormulario(consulta)
    : { ...FORM_CONSULTA_VACIO, idCita: params.get('idCita') ?? '' }
  const form = { ...iniciales, ...cambios }

  // Una cita solo admite una consulta, y solo si está activa, Confirmada o En curso y su fecha no es
  // futura. Al editar se mantiene siempre la cita actual.
  const citasConConsulta = new Set(consultas.map((c) => c.idCita))
  const atendible = (c) =>
    c.activo !== false &&
    !citasConConsulta.has(c.idCita) &&
    c.fecha <= hoyISO() &&
    ['confirmada', 'en curso'].includes((datosCita.estadosPorId[c.idEstado]?.descripcion ?? '').toLowerCase())
  const citasDisponibles = citas.filter((c) => atendible(c) || String(c.idCita) === form.idCita)
  const opcionesCita = citasDisponibles.map((c) => ({
    valor: String(c.idCita),
    etiqueta: `#${c.idCita} · ${formatearFecha(c.fecha)} ${formatearHora(c.hora)} · ${nombrePaciente(datosCita.pacientesPorId[c.idPaciente], c.idPaciente)} · ${nombreMedico(datosCita.medicosPorId[c.idMedico], c.idMedico)}`,
  }))

  const cambiar = (e) => {
    const { name, value } = e.target
    setCambios((c) => ({ ...c, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const enviar = async (e) => {
    e.preventDefault()
    setErrorGeneral('')
    const locales = validarConsulta(form)
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const datosEnvio = formularioAConsulta(form)
      const guardada = esEdicion ? await actualizarConsulta(id, datosEnvio) : await crearConsulta(datosEnvio)
      navigate(`/consultas/${guardada.idConsulta}`, {
        replace: true,
        state: {
          mensaje: esEdicion
            ? 'Consulta actualizada.'
            : 'Consulta registrada. La cita pasó automáticamente al estado «Atendida».',
        },
      })
    } catch (err) {
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  return (
    <div>
      <Link to={esEdicion ? `/consultas/${id}` : '/consultas'} className="volver-enlace">
        &larr; {esEdicion ? 'Volver al detalle' : 'Volver a consultas'}
      </Link>
      <h1 className="h3 mb-0">{esEdicion ? `Editar consulta #${id}` : 'Nueva consulta'}</h1>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />

        <div className="row">
          <FormField
            className="col-12"
            nombre="idCita"
            etiqueta="Cita"
            tipo="select"
            placeholder="Seleccione la cita atendida"
            opciones={opcionesCita}
            valor={form.idCita}
            onChange={cambiar}
            error={errores.idCita}
            disabled={esEdicion}
            requerido
            ayuda={!esEdicion ? 'Solo aparecen citas Confirmadas o En curso, con fecha de hoy o anterior, que aún no tienen consulta.' : undefined}
          />
          <FormField
            nombre="fechaHora"
            etiqueta="Fecha y hora de atención"
            tipo="datetime-local"
            valor={form.fechaHora}
            onChange={cambiar}
            error={errores.fechaHora}
            ayuda="Si se deja vacía se usa la fecha y hora actuales."
          />
          <FormField
            nombre="presionArterial"
            etiqueta="Presión arterial"
            valor={form.presionArterial}
            onChange={cambiar}
            error={errores.presionArterial}
            placeholder="120/80"
            maxLength={10}
          />
          <FormField
            nombre="peso"
            etiqueta="Peso (kg)"
            tipo="number"
            step="0.01"
            min="0"
            valor={form.peso}
            onChange={cambiar}
            error={errores.peso}
          />
          <FormField
            nombre="talla"
            etiqueta="Talla (cm)"
            tipo="number"
            step="0.01"
            min="0"
            valor={form.talla}
            onChange={cambiar}
            error={errores.talla}
          />
          <FormField
            nombre="temperatura"
            etiqueta="Temperatura (°C)"
            tipo="number"
            step="0.1"
            min="30"
            max="45"
            valor={form.temperatura}
            onChange={cambiar}
            error={errores.temperatura}
          />
          <FormField
            nombre="frecuenciaCardiaca"
            etiqueta="Frecuencia cardíaca (lpm)"
            tipo="number"
            step="1"
            min="20"
            max="250"
            valor={form.frecuenciaCardiaca}
            onChange={cambiar}
            error={errores.frecuenciaCardiaca}
          />
          <FormField
            className="col-12"
            nombre="sintomas"
            etiqueta="Síntomas"
            tipo="textarea"
            valor={form.sintomas}
            onChange={cambiar}
            error={errores.sintomas}
          />
        </div>

        <div className="acciones-grupo d-flex justify-content-end border-top pt-3">
          <Link
            to={esEdicion ? `/consultas/${id}` : '/consultas'}
            className="btn btn-secondary btn-round"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={guardando}
            className="btn btn-primary btn-round"
          >
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Registrar consulta'}
          </button>
        </div>
      </form>
    </div>
  )
}
