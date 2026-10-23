import { useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { actualizarCita, crearCita, obtenerCita } from '../../api/citas.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Loading from '../../components/Loading.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useDatosCita } from '../../hooks/useDatosCita.js'
import { normalizarError } from '../../utils/errores.js'
import { hoyISO } from '../../utils/formato.js'
import {
  FORM_CITA_VACIO,
  citaAFormulario,
  formularioACita,
  nombreMedico,
  nombrePaciente,
  validarCita,
} from './utilsCita.js'

const ESTADOS_INICIALES = ['Programada', 'Confirmada']

export default function CitaForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const datosCita = useDatosCita()
  const { datos: cita, error: errorCarga, cargando } = useApi(`cita-form|${id ?? 'nueva'}`, () =>
    esEdicion ? obtenerCita(id) : null,
  )

  // Valores editados por el usuario sobre los iniciales (derivados de la cita cargada o de la URL).
  const [cambios, setCambios] = useState({})
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [guardando, setGuardando] = useState(false)

  if (cargando || datosCita.cargando) return <Loading texto="Cargando..." />

  if (errorCarga) {
    return (
      <div>
        <ErrorAlert
          titulo={errorCarga.status === 404 ? 'Cita no encontrada' : 'No se pudo cargar el formulario'}
          mensaje={errorCarga.message}
        />
        <Link to="/citas" className="volver-enlace">
          &larr; Volver a citas
        </Link>
      </div>
    )
  }

  const estadoProgramada = datosCita.estados.find((e) => e.descripcion === 'Programada')
  const iniciales = cita
    ? citaAFormulario(cita)
    : {
        ...FORM_CITA_VACIO,
        idPaciente: params.get('idPaciente') ?? '',
        idEstado: estadoProgramada ? String(estadoProgramada.idEstado) : '',
      }
  const form = { ...iniciales, ...cambios }

  const cambiar = (e) => {
    const { name, value } = e.target
    setCambios((c) => ({ ...c, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const enviar = async (e) => {
    e.preventDefault()
    setErrorGeneral('')
    const locales = validarCita(form, esEdicion ? iniciales : null)
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const datos = formularioACita(form)
      const guardada = esEdicion ? await actualizarCita(id, datos) : await crearCita(datos)
      navigate(`/citas/${guardada.idCita}`, {
        replace: true,
        state: { mensaje: esEdicion ? 'Cita actualizada.' : 'Cita programada.' },
      })
    } catch (err) {
      // El backend responde 400 (reglas de negocio de citas, sin detalles por campo), 404, etc.
      // 400: errores por campo en `detalles`; 404/409/422/403: se muestra `message`
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  const opcionesPaciente = datosCita.pacientes
    .filter((p) => p.activo !== false || String(p.idPaciente) === form.idPaciente)
    .map((p) => ({ valor: String(p.idPaciente), etiqueta: `${nombrePaciente(p)} (DPI ${p.dpiPaciente})` }))
  const opcionesMedico = datosCita.medicos
    .filter((m) => m.estado !== false || String(m.idMedico) === form.idMedico)
    .map((m) => ({ valor: String(m.idMedico), etiqueta: nombreMedico(m) }))
  // Una cita nueva solo puede nacer Programada o Confirmada; los demás estados se alcanzan
  // con "Cambiar estado" (el backend acepta cualquier estado activo al crear).
  const opcionesEstado = datosCita.estados
    .filter((s) => s.activo !== false && ESTADOS_INICIALES.includes(s.descripcion))
    .map((s) => ({ valor: String(s.idEstado), etiqueta: s.descripcion }))
  const estadoActual = datosCita.estadosPorId[form.idEstado]

  return (
    <div>
      <Link to={esEdicion ? `/citas/${id}` : '/citas'} className="volver-enlace">
        &larr; {esEdicion ? 'Volver al detalle' : 'Volver a citas'}
      </Link>
      <h1 className="h3 mb-0">{esEdicion ? 'Editar cita' : 'Nueva cita'}</h1>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />

        <div className="row">
          <FormField
            nombre="idPaciente"
            etiqueta="Paciente"
            tipo="select"
            opciones={opcionesPaciente}
            valor={form.idPaciente}
            onChange={cambiar}
            error={errores.idPaciente}
            requerido
          />
          <FormField
            nombre="idMedico"
            etiqueta="Médico"
            tipo="select"
            opciones={opcionesMedico}
            valor={form.idMedico}
            onChange={cambiar}
            error={errores.idMedico}
            requerido
          />
          <FormField
            nombre="fecha"
            etiqueta="Fecha"
            tipo="date"
            valor={form.fecha}
            onChange={cambiar}
            error={errores.fecha}
            min={esEdicion ? undefined : hoyISO()}
            requerido
          />
          <FormField
            nombre="hora"
            etiqueta="Hora"
            tipo="time"
            valor={form.hora}
            onChange={cambiar}
            error={errores.hora}
            requerido
          />
          {esEdicion ? (
            <div>
              <p className="font-weight-bold small mb-1">Estado</p>
              <p className="form-control-plaintext border rounded px-2 bg-light">
                {estadoActual?.descripcion ?? `#${form.idEstado}`}
              </p>
              <p className="text-muted small">Para cambiarlo use «Cambiar estado» en el detalle de la cita.</p>
            </div>
          ) : (
            <FormField
              nombre="idEstado"
              etiqueta="Estado inicial"
              tipo="select"
              opciones={opcionesEstado}
              valor={form.idEstado}
              onChange={cambiar}
              error={errores.idEstado}
              requerido
            />
          )}
          <FormField
            className="col-12"
            nombre="motivo"
            etiqueta="Motivo de la cita"
            tipo="textarea"
            valor={form.motivo}
            onChange={cambiar}
            error={errores.motivo}
            maxLength={255}
          />
        </div>

        <div className="acciones-grupo d-flex justify-content-end border-top pt-3">
          <Link
            to={esEdicion ? `/citas/${id}` : '/citas'}
            className="btn btn-secondary btn-round"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={guardando}
            className="btn btn-primary btn-round"
          >
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Programar cita'}
          </button>
        </div>
      </form>
    </div>
  )
}
