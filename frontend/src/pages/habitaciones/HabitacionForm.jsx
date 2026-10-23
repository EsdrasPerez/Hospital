import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { actualizarHabitacion, crearHabitacion, obtenerHabitacion } from '../../api/habitaciones.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Loading from '../../components/Loading.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useDatosHospitalizacion } from '../../hooks/useDatosHospitalizacion.js'
import { normalizarError } from '../../utils/errores.js'

const VACIO = { numero: '', piso: '', capacidad: '1', idTipoHabitacion: '', idEstadoHabitacion: '' }

export default function HabitacionForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()
  const datos = useDatosHospitalizacion()
  const { datos: original, error: errorCarga, cargando } = useApi(`habitacion-form|${id ?? 'nueva'}`, () =>
    esEdicion ? obtenerHabitacion(id) : null,
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
          titulo={errorCarga.status === 404 ? 'Habitación no encontrada' : 'No se pudo cargar el formulario'}
          mensaje={errorCarga.message}
        />
        <Link to="/habitaciones" className="volver-enlace">
          &larr; Volver a habitaciones
        </Link>
      </div>
    )
  }

  const disponible = datos.estadosHab.find((e) => e.nombre === 'Disponible')
  const iniciales = original
    ? {
        numero: original.numero ?? '',
        piso: original.piso != null ? String(original.piso) : '',
        capacidad: original.capacidad != null ? String(original.capacidad) : '',
        idTipoHabitacion: original.idTipoHabitacion != null ? String(original.idTipoHabitacion) : '',
        idEstadoHabitacion: original.idEstadoHabitacion != null ? String(original.idEstadoHabitacion) : '',
      }
    : { ...VACIO, idEstadoHabitacion: disponible ? String(disponible.idEstadoHabitacion) : '' }
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
    if (!form.numero.trim()) locales.numero = 'El número es obligatorio'
    else if (form.numero.trim().length > 10) locales.numero = 'Máximo 10 caracteres'
    if (form.piso === '' || !Number.isInteger(Number(form.piso))) locales.piso = 'El piso es obligatorio (número entero)'
    if (!Number.isInteger(Number(form.capacidad)) || Number(form.capacidad) <= 0) locales.capacidad = 'La capacidad debe ser un entero mayor a cero'
    if (!form.idTipoHabitacion) locales.idTipoHabitacion = 'El tipo de habitación es obligatorio'
    if (!form.idEstadoHabitacion) locales.idEstadoHabitacion = 'Seleccione el estado'
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const payload = {
        numero: form.numero.trim(),
        piso: Number(form.piso),
        capacidad: Number(form.capacidad),
        idTipoHabitacion: Number(form.idTipoHabitacion),
        idEstadoHabitacion: Number(form.idEstadoHabitacion),
      }
      const guardada = esEdicion ? await actualizarHabitacion(id, payload) : await crearHabitacion(payload)
      navigate(`/habitaciones/${guardada.idHabitacion}`, {
        replace: true,
        state: { mensaje: esEdicion ? 'Habitación actualizada.' : 'Habitación registrada.' },
      })
    } catch (err) {
      // 400: errores por campo en `detalles`; 409: número repetido; 422: tipo/estado inexistente o inactivo, estado incoherente con la ocupación
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  const opcionesTipo = datos.tiposHab
    .filter((t) => t.activo !== false || String(t.idTipoHabitacion) === form.idTipoHabitacion)
    .map((t) => ({ valor: String(t.idTipoHabitacion), etiqueta: t.descripcion }))
  const opcionesEstado = datos.estadosHab
    .filter((s) => s.activo !== false || String(s.idEstadoHabitacion) === form.idEstadoHabitacion)
    .map((s) => ({ valor: String(s.idEstadoHabitacion), etiqueta: s.nombre }))

  return (
    <div>
      <Link to={esEdicion ? `/habitaciones/${id}` : '/habitaciones'} className="volver-enlace">
        &larr; {esEdicion ? 'Volver al detalle' : 'Volver a habitaciones'}
      </Link>
      <h1 className="h3 mb-0">{esEdicion ? 'Editar habitación' : 'Nueva habitación'}</h1>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />

        <div className="row">
          <FormField nombre="numero" etiqueta="Número" valor={form.numero} onChange={cambiar} error={errores.numero} maxLength={10} requerido />
          <FormField nombre="piso" etiqueta="Piso" tipo="number" step="1" valor={form.piso} onChange={cambiar} error={errores.piso} requerido />
          <FormField
            nombre="capacidad"
            etiqueta="Capacidad (camas)"
            tipo="number"
            step="1"
            min="1"
            valor={form.capacidad}
            onChange={cambiar}
            error={errores.capacidad}
            requerido
          />
          <FormField
            nombre="idTipoHabitacion"
            etiqueta="Tipo de habitación"
            tipo="select"
            opciones={opcionesTipo}
            valor={form.idTipoHabitacion}
            onChange={cambiar}
            error={errores.idTipoHabitacion}
            requerido
          />
          <FormField
            nombre="idEstadoHabitacion"
            etiqueta="Estado"
            tipo="select"
            opciones={opcionesEstado}
            valor={form.idEstadoHabitacion}
            onChange={cambiar}
            error={errores.idEstadoHabitacion}
            requerido
          />
        </div>

        <div className="acciones-grupo d-flex justify-content-end border-top pt-3">
          <Link
            to={esEdicion ? `/habitaciones/${id}` : '/habitaciones'}
            className="btn btn-secondary btn-round"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={guardando}
            className="btn btn-primary btn-round"
          >
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Registrar habitación'}
          </button>
        </div>
      </form>
    </div>
  )
}
