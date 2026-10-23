import { useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { listarDiagnosticos } from '../../api/diagnosticos.js'
import { actualizarTratamiento, crearTratamiento, obtenerTratamiento } from '../../api/tratamientos.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Loading from '../../components/Loading.jsx'
import { useApi } from '../../hooks/useApi.js'
import { normalizarError } from '../../utils/errores.js'
import { acortar } from '../../utils/formato.js'
import {
  FORM_TRATAMIENTO_VACIO,
  formularioATratamiento,
  tratamientoAFormulario,
  validarTratamiento,
} from './utilsTratamiento.js'

export default function TratamientoForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()
  const [params] = useSearchParams()

  const { datos, error: errorCarga, cargando } = useApi(`tratamiento-form|${id ?? 'nuevo'}`, async () => {
    const [tratamiento, diagnosticos] = await Promise.all([
      esEdicion ? obtenerTratamiento(id) : null,
      listarDiagnosticos(),
    ])
    return { tratamiento, diagnosticos }
  })

  const [cambios, setCambios] = useState({})
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [guardando, setGuardando] = useState(false)

  if (cargando) return <Loading texto="Cargando..." />

  if (errorCarga) {
    return (
      <div>
        <ErrorAlert
          titulo={errorCarga.status === 404 ? 'Tratamiento no encontrado' : 'No se pudo cargar el formulario'}
          mensaje={errorCarga.message}
        />
        <Link to="/tratamientos" className="volver-enlace">
          &larr; Volver a tratamientos
        </Link>
      </div>
    )
  }

  const { tratamiento, diagnosticos } = datos
  const iniciales = tratamiento
    ? tratamientoAFormulario(tratamiento)
    : { ...FORM_TRATAMIENTO_VACIO, idDiagnostico: params.get('idDiagnostico') ?? '' }
  const form = { ...iniciales, ...cambios }

  const cambiar = (e) => {
    const { name, value } = e.target
    setCambios((c) => ({ ...c, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const enviar = async (e) => {
    e.preventDefault()
    setErrorGeneral('')
    const locales = validarTratamiento(form)
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const datosEnvio = formularioATratamiento(form)
      const guardado = esEdicion ? await actualizarTratamiento(id, datosEnvio) : await crearTratamiento(datosEnvio)
      navigate(`/tratamientos/${guardado.idTratamiento}`, {
        replace: true,
        state: { mensaje: esEdicion ? 'Tratamiento actualizado.' : 'Tratamiento registrado.' },
      })
    } catch (err) {
      // 422 (reglas de negocio), 404 (diagnóstico inexistente), etc.: se muestra `message`
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  const opcionesDiagnostico = diagnosticos
    .filter((d) => d.activo !== false || String(d.idDiagnostico) === form.idDiagnostico)
    .map((d) => ({ valor: String(d.idDiagnostico), etiqueta: `#${d.idDiagnostico} · ${acortar(d.descripcion, 70)}` }))

  return (
    <div>
      <Link to={esEdicion ? `/tratamientos/${id}` : '/tratamientos'} className="volver-enlace">
        &larr; {esEdicion ? 'Volver al detalle' : 'Volver a tratamientos'}
      </Link>
      <h1 className="h3 mb-0">{esEdicion ? 'Editar tratamiento' : 'Nuevo tratamiento'}</h1>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />

        <div className="row">
          <FormField
            className="col-12"
            nombre="idDiagnostico"
            etiqueta="Diagnóstico"
            tipo="select"
            opciones={opcionesDiagnostico}
            valor={form.idDiagnostico}
            onChange={cambiar}
            error={errores.idDiagnostico}
            requerido
          />
          <FormField
            className="col-12"
            nombre="descripcion"
            etiqueta="Descripción"
            valor={form.descripcion}
            onChange={cambiar}
            error={errores.descripcion}
            maxLength={255}
            requerido
          />
          <FormField
            nombre="duracionDias"
            etiqueta="Duración (días)"
            tipo="number"
            step="1"
            min="1"
            valor={form.duracionDias}
            onChange={cambiar}
            error={errores.duracionDias}
          />
          <span className="d-none d-sm-block" />
          <FormField
            nombre="fechaInicio"
            etiqueta="Fecha de inicio"
            tipo="date"
            valor={form.fechaInicio}
            onChange={cambiar}
            error={errores.fechaInicio}
            ayuda="Si se deja vacía se usa la fecha de hoy al crear."
          />
          <FormField
            nombre="fechaFin"
            etiqueta="Fecha de fin"
            tipo="date"
            valor={form.fechaFin}
            onChange={cambiar}
            error={errores.fechaFin}
          />
          <FormField
            className="col-12"
            nombre="indicaciones"
            etiqueta="Indicaciones"
            tipo="textarea"
            valor={form.indicaciones}
            onChange={cambiar}
            error={errores.indicaciones}
          />
        </div>

        <div className="acciones-grupo d-flex justify-content-end border-top pt-3">
          <Link
            to={esEdicion ? `/tratamientos/${id}` : '/tratamientos'}
            className="btn btn-secondary btn-round"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={guardando}
            className="btn btn-primary btn-round"
          >
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Registrar tratamiento'}
          </button>
        </div>
      </form>
    </div>
  )
}
