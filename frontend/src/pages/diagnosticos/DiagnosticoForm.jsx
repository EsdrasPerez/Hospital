import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { listarTiposDiagnostico } from '../../api/catalogos.js'
import { actualizarDiagnostico, crearDiagnostico, obtenerDiagnostico } from '../../api/diagnosticos.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Loading from '../../components/Loading.jsx'
import { useApi } from '../../hooks/useApi.js'
import { normalizarError } from '../../utils/errores.js'
import { hoyISO } from '../../utils/formato.js'
import {
  FORM_DIAGNOSTICO_VACIO,
  diagnosticoAFormulario,
  formularioADiagnostico,
  validarDiagnostico,
} from './utilsDiagnostico.js'

export default function DiagnosticoForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()

  const { datos, error: errorCarga, cargando } = useApi(`diagnostico-form|${id ?? 'nuevo'}`, async () => {
    const [diagnostico, tipos] = await Promise.all([esEdicion ? obtenerDiagnostico(id) : null, listarTiposDiagnostico()])
    return { diagnostico, tipos }
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
          titulo={errorCarga.status === 404 ? 'Diagnóstico no encontrado' : 'No se pudo cargar el formulario'}
          mensaje={errorCarga.message}
        />
        <Link to="/diagnosticos" className="volver-enlace">
          &larr; Volver a diagnósticos
        </Link>
      </div>
    )
  }

  const { diagnostico, tipos } = datos
  const form = { ...(diagnostico ? diagnosticoAFormulario(diagnostico) : FORM_DIAGNOSTICO_VACIO), ...cambios }

  const cambiar = (e) => {
    const { name, value } = e.target
    setCambios((c) => ({ ...c, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const enviar = async (e) => {
    e.preventDefault()
    setErrorGeneral('')
    const locales = validarDiagnostico(form)
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const datosEnvio = formularioADiagnostico(form)
      const guardado = esEdicion ? await actualizarDiagnostico(id, datosEnvio) : await crearDiagnostico(datosEnvio)
      navigate(`/diagnosticos/${guardado.idDiagnostico}`, {
        replace: true,
        state: { mensaje: esEdicion ? 'Diagnóstico actualizado.' : 'Diagnóstico registrado.' },
      })
    } catch (err) {
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  const opcionesTipo = tipos
    .filter((t) => t.activo !== false || String(t.idTipoDiagnostico) === form.idTipoDiagnostico)
    .map((t) => ({ valor: String(t.idTipoDiagnostico), etiqueta: t.descripcion }))

  return (
    <div>
      <Link to={esEdicion ? `/diagnosticos/${id}` : '/diagnosticos'} className="volver-enlace">
        &larr; {esEdicion ? 'Volver al detalle' : 'Volver a diagnósticos'}
      </Link>
      <h1 className="h3 mb-0">{esEdicion ? 'Editar diagnóstico' : 'Nuevo diagnóstico'}</h1>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />

        <div className="row">
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
            nombre="idTipoDiagnostico"
            etiqueta="Tipo de diagnóstico"
            tipo="select"
            opciones={opcionesTipo}
            valor={form.idTipoDiagnostico}
            onChange={cambiar}
            error={errores.idTipoDiagnostico}
            requerido
          />
          <FormField
            nombre="fecha"
            etiqueta="Fecha"
            tipo="date"
            valor={form.fecha}
            onChange={cambiar}
            error={errores.fecha}
            max={hoyISO()}
            ayuda="Si se deja vacía se usa la fecha de hoy."
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
            to={esEdicion ? `/diagnosticos/${id}` : '/diagnosticos'}
            className="btn btn-secondary btn-round"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={guardando}
            className="btn btn-primary btn-round"
          >
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Registrar diagnóstico'}
          </button>
        </div>
      </form>
    </div>
  )
}
