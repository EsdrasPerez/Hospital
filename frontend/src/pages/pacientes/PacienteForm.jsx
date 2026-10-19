import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { listarAlergias, listarTiposSangre } from '../../api/catalogos.js'
import { actualizarPaciente, crearPaciente, obtenerPaciente } from '../../api/pacientes.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Loading from '../../components/Loading.jsx'
import { normalizarError } from '../../utils/errores.js'
import { ayerISO } from '../../utils/formato.js'
import {
  FORM_VACIO,
  SEXOS,
  formularioAPeticion,
  nombreCompleto,
  pacienteAFormulario,
  validarPaciente,
} from './utilsPaciente.js'

export default function PacienteForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState(FORM_VACIO)
  const [alergias, setAlergias] = useState([])
  const [tiposSangre, setTiposSangre] = useState([])
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [errorCarga, setErrorCarga] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    let cancelado = false
    setCargando(true)
    setErrorCarga(null)
    const catalogos = Promise.allSettled([listarAlergias(), listarTiposSangre()])
    const paciente = esEdicion ? obtenerPaciente(id) : Promise.resolve(null)
    Promise.all([paciente, catalogos])
      .then(([p, [a, t]]) => {
        if (cancelado) return
        if (p) setForm(pacienteAFormulario(p))
        if (a.status === 'fulfilled') setAlergias(a.value)
        if (t.status === 'fulfilled') setTiposSangre(t.value)
      })
      .catch((e) => {
        if (!cancelado) setErrorCarga(normalizarError(e))
      })
      .finally(() => {
        if (!cancelado) setCargando(false)
      })
    return () => {
      cancelado = true
    }
  }, [id, esEdicion])

  const cambiar = (e) => {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const enviar = async (e) => {
    e.preventDefault()
    setErrorGeneral('')
    const locales = validarPaciente(form)
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const datos = formularioAPeticion(form)
      const guardado = esEdicion ? await actualizarPaciente(id, datos) : await crearPaciente(datos)
      navigate(`/pacientes/${guardado.idPaciente}`, {
        replace: true,
        state: { mensaje: esEdicion ? 'Paciente actualizado.' : 'Paciente registrado.' },
      })
    } catch (err) {
      const { status, message, detalles } = normalizarError(err)
      setErrores(detalles) // 400: detalles campo -> mensaje
      // 400 muestra los errores bajo cada campo; 404, 409, 422, 403 y otros muestran `message`.
      setErrorGeneral(status === 400 && Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  if (cargando) return <Loading texto="Cargando..." />

  if (errorCarga) {
    return (
      <div className="space-y-4">
        <ErrorAlert
          titulo={errorCarga.status === 404 ? 'Paciente no encontrado' : 'No se pudo cargar el formulario'}
          mensaje={errorCarga.message}
        />
        <Link to="/pacientes" className="text-sm font-medium text-teal-800 hover:underline">
          &larr; Volver a pacientes
        </Link>
      </div>
    )
  }

  const opcionesCatalogo = (lista, clave) =>
    lista.filter((x) => x.activo !== false).map((x) => ({ valor: String(x[clave]), etiqueta: x.nombre }))

  return (
    <div className="space-y-4">
      <Link to={esEdicion ? `/pacientes/${id}` : '/pacientes'} className="text-sm font-medium text-teal-800 hover:underline">
        &larr; {esEdicion ? 'Volver al detalle' : 'Volver a pacientes'}
      </Link>
      <h1 className="text-2xl font-semibold text-slate-900">
        {esEdicion ? `Editar paciente: ${nombreCompleto(form)}` : 'Nuevo paciente'}
      </h1>

      <form onSubmit={enviar} noValidate className="space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <ErrorAlert mensaje={errorGeneral} />

        <div className="grid gap-4 sm:grid-cols-2">
          <FormField
            nombre="dpiPaciente"
            etiqueta="DPI"
            valor={form.dpiPaciente}
            onChange={cambiar}
            error={errores.dpiPaciente}
            requerido
            inputMode="numeric"
            maxLength={13}
            placeholder="13 dígitos"
          />
          <FormField
            nombre="sexo"
            etiqueta="Sexo"
            tipo="select"
            opciones={SEXOS}
            valor={form.sexo}
            onChange={cambiar}
            error={errores.sexo}
            requerido
          />
          <FormField
            nombre="nombre"
            etiqueta="Nombre"
            valor={form.nombre}
            onChange={cambiar}
            error={errores.nombre}
            requerido
            maxLength={100}
          />
          <FormField
            nombre="fechaNacimiento"
            etiqueta="Fecha de nacimiento"
            tipo="date"
            valor={form.fechaNacimiento}
            onChange={cambiar}
            error={errores.fechaNacimiento}
            max={ayerISO()}
          />
          <FormField
            nombre="apellidoPaterno"
            etiqueta="Apellido paterno"
            valor={form.apellidoPaterno}
            onChange={cambiar}
            error={errores.apellidoPaterno}
            requerido
            maxLength={100}
          />
          <FormField
            nombre="apellidoMaterno"
            etiqueta="Apellido materno"
            valor={form.apellidoMaterno}
            onChange={cambiar}
            error={errores.apellidoMaterno}
            requerido
            maxLength={100}
          />
          <FormField
            nombre="telefono"
            etiqueta="Teléfono"
            tipo="tel"
            valor={form.telefono}
            onChange={cambiar}
            error={errores.telefono}
            requerido
            maxLength={20}
          />
          <FormField
            nombre="correoElectronico"
            etiqueta="Correo electrónico"
            tipo="email"
            valor={form.correoElectronico}
            onChange={cambiar}
            error={errores.correoElectronico}
            maxLength={150}
          />
          <FormField
            nombre="idTipoDeSangre"
            etiqueta="Tipo de sangre"
            tipo="select"
            placeholder="Sin especificar"
            opciones={opcionesCatalogo(tiposSangre, 'idTipoDeSangre')}
            valor={form.idTipoDeSangre}
            onChange={cambiar}
            error={errores.idTipoDeSangre}
          />
          <FormField
            nombre="idAlergias"
            etiqueta="Alergia"
            tipo="select"
            placeholder="Ninguna"
            opciones={opcionesCatalogo(alergias, 'idAlergias')}
            valor={form.idAlergias}
            onChange={cambiar}
            error={errores.idAlergias}
          />
          <FormField
            className="sm:col-span-2"
            nombre="direccion"
            etiqueta="Dirección"
            tipo="textarea"
            valor={form.direccion}
            onChange={cambiar}
            error={errores.direccion}
            maxLength={255}
          />
        </div>

        <div className="flex justify-end gap-3 border-t border-slate-100 pt-4">
          <Link
            to={esEdicion ? `/pacientes/${id}` : '/pacientes'}
            className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={guardando}
            className="rounded-lg bg-teal-700 px-5 py-2 text-sm font-semibold text-white hover:bg-teal-800 disabled:opacity-60"
          >
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Registrar paciente'}
          </button>
        </div>
      </form>
    </div>
  )
}
