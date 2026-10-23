import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { actualizarTriaje, crearTriaje, obtenerTriaje } from '../../api/triaje.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Loading from '../../components/Loading.jsx'
import { useApi } from '../../hooks/useApi.js'
import { normalizarError } from '../../utils/errores.js'

export default function TriajeForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()
  const { datos: original, error: errorCarga, cargando } = useApi(`triaje-form|${id ?? 'nuevo'}`, () =>
    esEdicion ? obtenerTriaje(id) : null,
  )

  const [cambios, setCambios] = useState({})
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [guardando, setGuardando] = useState(false)

  if (cargando) return <Loading texto="Cargando..." />

  if (errorCarga) {
    return (
      <div>
        <ErrorAlert
          titulo={errorCarga.status === 404 ? 'Nivel de triaje no encontrado' : 'No se pudo cargar el formulario'}
          mensaje={errorCarga.message}
        />
        <Link to="/triaje" className="volver-enlace">
          &larr; Volver a niveles de triaje
        </Link>
      </div>
    )
  }

  const form = {
    nivelDeEmergencia: original?.nivelDeEmergencia ?? '',
    descripcion: original?.descripcion ?? '',
    ...cambios,
  }

  const cambiar = (e) => {
    const { name, value } = e.target
    setCambios((c) => ({ ...c, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const enviar = async (e) => {
    e.preventDefault()
    setErrorGeneral('')
    const locales = {}
    if (!form.nivelDeEmergencia.trim()) locales.nivelDeEmergencia = 'El nivel de emergencia es obligatorio'
    else if (form.nivelDeEmergencia.trim().length > 50) locales.nivelDeEmergencia = 'Máximo 50 caracteres'
    if (form.descripcion.trim().length > 255) locales.descripcion = 'Máximo 255 caracteres'
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const payload = {
        nivelDeEmergencia: form.nivelDeEmergencia.trim(),
        descripcion: form.descripcion.trim() === '' ? null : form.descripcion.trim(),
      }
      if (esEdicion) await actualizarTriaje(id, payload)
      else await crearTriaje(payload)
      navigate('/triaje', { replace: true, state: { mensaje: esEdicion ? 'Nivel de triaje actualizado.' : 'Nivel de triaje registrado.' } })
    } catch (err) {
      // 409: ya existe un nivel con ese nombre; 422/404/403: se muestra `message`
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  return (
    <div>
      <Link to="/triaje" className="volver-enlace">
        &larr; Volver a niveles de triaje
      </Link>
      <h1 className="h3 mb-0">{esEdicion ? 'Editar nivel de triaje' : 'Nuevo nivel de triaje'}</h1>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />
        <div className="row">
        <FormField
          className="col-12"
          nombre="nivelDeEmergencia"
          etiqueta="Nivel de emergencia"
          valor={form.nivelDeEmergencia}
          onChange={cambiar}
          error={errores.nivelDeEmergencia}
          maxLength={50}
          placeholder="N1, N2, N3, N4 o N5"
          ayuda="El color de la etiqueta se toma del catálogo de niveles de urgencia, que usa el mismo código (N1 rojo ... N5 azul)."
          requerido
        />
        <FormField
          className="col-12"
          nombre="descripcion"
          etiqueta="Descripción"
          tipo="textarea"
          valor={form.descripcion}
          onChange={cambiar}
          error={errores.descripcion}
          maxLength={255}
        />
        </div>
        <div className="acciones-grupo d-flex justify-content-end border-top pt-3">
          <Link
            to="/triaje"
            className="btn btn-secondary btn-round"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={guardando}
            className="btn btn-primary btn-round"
          >
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Registrar nivel'}
          </button>
        </div>
      </form>
    </div>
  )
}
