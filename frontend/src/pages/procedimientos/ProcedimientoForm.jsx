import { useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { listarEmergencias } from '../../api/emergencias.js'
import { actualizarProcedimiento, crearProcedimiento, obtenerProcedimiento } from '../../api/procedimientos.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Loading from '../../components/Loading.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useDatosEmergencia } from '../../hooks/useDatosEmergencia.js'
import { normalizarError } from '../../utils/errores.js'
import { aInputFechaHora, deInputFechaHora, formatearFechaHora } from '../../utils/formato.js'
import { nombreMedico } from '../citas/utilsCita.js'
import { etiquetaPacienteEmergencia } from '../emergencias/utilsEmergencia.js'

export default function ProcedimientoForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const datosEmergencia = useDatosEmergencia()

  const { datos, error: errorCarga, cargando } = useApi(`procedimiento-form|${id ?? 'nuevo'}`, async () => {
    const [procedimiento, emergencias] = await Promise.all([esEdicion ? obtenerProcedimiento(id) : null, listarEmergencias()])
    return { procedimiento, emergencias }
  })

  const [cambios, setCambios] = useState({})
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [guardando, setGuardando] = useState(false)

  if (cargando || datosEmergencia.cargando) return <Loading texto="Cargando..." />

  if (errorCarga) {
    return (
      <div>
        <ErrorAlert
          titulo={errorCarga.status === 404 ? 'Procedimiento no encontrado' : 'No se pudo cargar el formulario'}
          mensaje={errorCarga.message}
        />
        <Link to="/procedimientos" className="volver-enlace">
          &larr; Volver a procedimientos
        </Link>
      </div>
    )
  }

  const { procedimiento, emergencias } = datos
  const iniciales = procedimiento
    ? {
        idEmergencia: String(procedimiento.idEmergencia),
        descripcion: procedimiento.descripcion ?? '',
        fechaHora: aInputFechaHora(procedimiento.fechaHora),
        idMedico: procedimiento.idMedico != null ? String(procedimiento.idMedico) : '',
        observaciones: procedimiento.observaciones ?? '',
      }
    : { idEmergencia: params.get('idEmergencia') ?? '', descripcion: '', fechaHora: '', idMedico: '', observaciones: '' }
  const form = { ...iniciales, ...cambios }

  const cambiar = (e) => {
    const { name, value } = e.target
    setCambios((c) => ({ ...c, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const enviar = async (e) => {
    e.preventDefault()
    setErrorGeneral('')
    const locales = {}
    if (!form.idEmergencia) locales.idEmergencia = 'Seleccione la emergencia'
    if (!form.descripcion.trim()) locales.descripcion = 'La descripción es obligatoria'
    else if (form.descripcion.trim().length > 200) locales.descripcion = 'Máximo 200 caracteres'
    if (!form.idMedico) locales.idMedico = 'Seleccione el médico'
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const payload = {
        idEmergencia: Number(form.idEmergencia),
        descripcion: form.descripcion.trim(),
        // Si no se indica al crear, el servidor usa la fecha y hora actuales.
        fechaHora: deInputFechaHora(form.fechaHora),
        idMedico: Number(form.idMedico),
        observaciones: form.observaciones.trim() === '' ? null : form.observaciones.trim(),
      }
      if (esEdicion) await actualizarProcedimiento(id, payload)
      else await crearProcedimiento(payload)
      navigate(`/emergencia/${payload.idEmergencia}`, {
        replace: true,
        state: { mensaje: esEdicion ? 'Procedimiento actualizado.' : 'Procedimiento registrado.' },
      })
    } catch (err) {
      // 422 (emergencia cerrada, fecha futura o anterior al ingreso), 404 (médico/emergencia inexistente), 403
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  // Al crear solo se ofrecen ingresos activos y sin alta; al editar queda la emergencia original.
  const opcionesEmergencia = emergencias
    .filter((em) => (em.activo !== false && !em.fechaHoraAlta) || String(em.idEmergencia) === form.idEmergencia)
    .map((em) => ({
      valor: String(em.idEmergencia),
      etiqueta: `#${em.idEmergencia} · ${etiquetaPacienteEmergencia(em, datosEmergencia.pacientesPorId)} · ${formatearFechaHora(em.fechaHoraIngreso)}`,
    }))
  const opcionesMedico = datosEmergencia.medicos
    .filter((m) => m.estado !== false || String(m.idMedico) === form.idMedico)
    .map((m) => ({ valor: String(m.idMedico), etiqueta: nombreMedico(m) }))

  const volver = form.idEmergencia ? `/emergencia/${form.idEmergencia}` : '/procedimientos'

  return (
    <div>
      <Link to={volver} className="volver-enlace">
        &larr; Volver
      </Link>
      <h1 className="h3 mb-0">{esEdicion ? 'Editar procedimiento' : 'Nuevo procedimiento'}</h1>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />

        <div className="row">
          <FormField
            className="col-12"
            nombre="idEmergencia"
            etiqueta="Ingreso a emergencia"
            tipo="select"
            opciones={opcionesEmergencia}
            valor={form.idEmergencia}
            onChange={cambiar}
            error={errores.idEmergencia}
            disabled={esEdicion}
            requerido
            ayuda={!esEdicion ? 'Solo aparecen ingresos activos que todavía no tienen alta.' : undefined}
          />
          <FormField
            className="col-12"
            nombre="descripcion"
            etiqueta="Procedimiento realizado"
            valor={form.descripcion}
            onChange={cambiar}
            error={errores.descripcion}
            maxLength={200}
            requerido
          />
          <FormField
            nombre="idMedico"
            etiqueta="Médico que lo realizó"
            tipo="select"
            opciones={opcionesMedico}
            valor={form.idMedico}
            onChange={cambiar}
            error={errores.idMedico}
            requerido
          />
          <FormField
            nombre="fechaHora"
            etiqueta="Fecha y hora"
            tipo="datetime-local"
            valor={form.fechaHora}
            onChange={cambiar}
            error={errores.fechaHora}
            ayuda="Si se deja vacía se usa la fecha y hora actuales. No puede ser futura ni anterior al ingreso."
          />
          <FormField
            className="col-12"
            nombre="observaciones"
            etiqueta="Observaciones"
            tipo="textarea"
            valor={form.observaciones}
            onChange={cambiar}
            error={errores.observaciones}
          />
        </div>

        <div className="acciones-grupo d-flex justify-content-end border-top pt-3">
          <Link
            to={volver}
            className="btn btn-secondary btn-round"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={guardando}
            className="btn btn-primary btn-round"
          >
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Registrar procedimiento'}
          </button>
        </div>
      </form>
    </div>
  )
}
