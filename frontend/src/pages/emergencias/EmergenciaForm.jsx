import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { actualizarEmergencia, crearEmergencia, obtenerEmergencia } from '../../api/emergencias.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Loading from '../../components/Loading.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useDatosEmergencia } from '../../hooks/useDatosEmergencia.js'
import { normalizarError } from '../../utils/errores.js'
import { nombreMedico, nombrePaciente } from '../citas/utilsCita.js'
import {
  FORM_EMERGENCIA_VACIO,
  emergenciaAFormulario,
  formularioAEmergencia,
  validarEmergencia,
} from './utilsEmergencia.js'

export default function EmergenciaForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()
  const datos = useDatosEmergencia()
  const { datos: original, error: errorCarga, cargando } = useApi(`emergencia-form|${id ?? 'nueva'}`, () =>
    esEdicion ? obtenerEmergencia(id) : null,
  )

  const [cambios, setCambios] = useState({})
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [guardando, setGuardando] = useState(false)

  if (cargando || datos.cargando) return <Loading texto="Cargando..." />

  if (errorCarga) {
    return (
      <div>
        <ErrorAlert
          titulo={errorCarga.status === 404 ? 'Ingreso no encontrado' : 'No se pudo cargar el formulario'}
          mensaje={errorCarga.message}
        />
        <Link to="/emergencia" className="volver-enlace">
          &larr; Volver a emergencias
        </Link>
      </div>
    )
  }

  const form = { ...(original ? emergenciaAFormulario(original) : FORM_EMERGENCIA_VACIO), ...cambios }

  const cambiar = (e) => {
    const { name, value } = e.target
    setCambios((c) => ({ ...c, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const enviar = async (e) => {
    e.preventDefault()
    setErrorGeneral('')
    const locales = validarEmergencia(form)
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const payload = formularioAEmergencia(form)
      const guardada = esEdicion ? await actualizarEmergencia(id, payload) : await crearEmergencia(payload)
      navigate(`/emergencia/${guardada.idEmergencia}`, {
        replace: true,
        state: { mensaje: esEdicion ? 'Ingreso actualizado.' : 'Ingreso a emergencia registrado.' },
      })
    } catch (err) {
      // 400: errores por campo en `detalles`; 422 (paciente, médico, habitación o triaje inexistente/inactivo), 404, 403: `message`
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  const seleccionable = (lista, claveId, activo) =>
    lista.filter((x) => activo(x) || String(x[claveId]) === form[claveId])
  const opcionesPaciente = datos.pacientes
    .filter((p) => p.activo !== false || String(p.idPaciente) === form.idPaciente)
    .map((p) => ({ valor: String(p.idPaciente), etiqueta: `${nombrePaciente(p)} (DPI ${p.dpiPaciente})` }))
  const opcionesMedico = seleccionable(datos.medicos, 'idMedico', (m) => m.estado !== false).map((m) => ({
    valor: String(m.idMedico),
    etiqueta: nombreMedico(m),
  }))
  const opcionesTriaje = seleccionable(datos.triajes, 'idTriaje', (t) => t.activo !== false).map((t) => ({
    valor: String(t.idTriaje),
    etiqueta: t.nivelDeEmergencia,
  }))
  const opcionesHabitacion = seleccionable(datos.habitaciones, 'idHabitacion', (h) => h.activo !== false).map((h) => ({
    valor: String(h.idHabitacion),
    etiqueta: `Habitación ${h.numero}`,
  }))

  return (
    <div>
      <Link to={esEdicion ? `/emergencia/${id}` : '/emergencia'} className="volver-enlace">
        &larr; {esEdicion ? 'Volver al detalle' : 'Volver a emergencias'}
      </Link>
      <h1 className="h3 mb-0">{esEdicion ? 'Editar ingreso a emergencia' : 'Registrar ingreso a emergencia'}</h1>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />

        <div className="row">
          <FormField
            nombre="idPaciente"
            etiqueta="Paciente registrado"
            tipo="select"
            placeholder="Paciente no identificado"
            opciones={opcionesPaciente}
            valor={form.idPaciente}
            onChange={cambiar}
            error={errores.idPaciente}
            ayuda="Si el paciente no está registrado, deje vacío e indique un nombre temporal."
          />
          <FormField
            nombre="nombreTemporal"
            etiqueta="Nombre temporal"
            valor={form.nombreTemporal}
            onChange={cambiar}
            error={errores.nombreTemporal}
            maxLength={150}
            placeholder="Ej. Hombre adulto sin identificar"
          />
          <FormField
            nombre="idTriaje"
            etiqueta="Nivel de triaje"
            tipo="select"
            opciones={opcionesTriaje}
            valor={form.idTriaje}
            onChange={cambiar}
            error={errores.idTriaje}
            requerido
          />
          <FormField
            nombre="fechaHoraIngreso"
            etiqueta="Fecha y hora de ingreso"
            tipo="datetime-local"
            valor={form.fechaHoraIngreso}
            onChange={cambiar}
            error={errores.fechaHoraIngreso}
            ayuda="Si se deja vacía se usa la fecha y hora actuales."
          />
          <FormField
            nombre="idMedico"
            etiqueta="Médico asignado"
            tipo="select"
            placeholder="Sin asignar"
            opciones={opcionesMedico}
            valor={form.idMedico}
            onChange={cambiar}
            error={errores.idMedico}
          />
          <FormField
            nombre="idHabitacion"
            etiqueta="Habitación / box"
            tipo="select"
            placeholder="Sin asignar"
            opciones={opcionesHabitacion}
            valor={form.idHabitacion}
            onChange={cambiar}
            error={errores.idHabitacion}
          />
          <FormField
            className="col-12"
            nombre="motivo"
            etiqueta="Motivo de atención"
            tipo="textarea"
            valor={form.motivo}
            onChange={cambiar}
            error={errores.motivo}
            requerido
          />
          <FormField
            className="col-12"
            nombre="signosVitales"
            etiqueta="Signos vitales"
            tipo="textarea"
            valor={form.signosVitales}
            onChange={cambiar}
            error={errores.signosVitales}
            placeholder="PA, FC, FR, temperatura, saturación..."
          />
        </div>

        <div className="acciones-grupo d-flex justify-content-end border-top pt-3">
          <Link
            to={esEdicion ? `/emergencia/${id}` : '/emergencia'}
            className="btn btn-secondary btn-round"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={guardando}
            className="btn btn-primary btn-round"
          >
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Registrar ingreso'}
          </button>
        </div>
      </form>
    </div>
  )
}
