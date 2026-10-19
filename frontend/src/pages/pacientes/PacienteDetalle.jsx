import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { listarAlergias, listarTiposSangre } from '../../api/catalogos.js'
import { desactivarPaciente, obtenerPaciente } from '../../api/pacientes.js'
import { useAuth } from '../../auth/useAuth.js'
import ConfirmDialog from '../../components/ConfirmDialog.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Loading from '../../components/Loading.jsx'
import { puede } from '../../config/menu.js'
import { normalizarError } from '../../utils/errores.js'
import { calcularEdad, formatearFecha, formatearFechaHora } from '../../utils/formato.js'
import { nombreCompleto } from './utilsPaciente.js'

function Dato({ etiqueta, children }) {
  return (
    <div>
      <dt className="text-xs font-semibold uppercase tracking-wide text-slate-500">{etiqueta}</dt>
      <dd className="mt-1 text-sm text-slate-900">{children || '—'}</dd>
    </div>
  )
}

export default function PacienteDetalle() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const [exito, setExito] = useState(location.state?.mensaje ?? '')
  const { rol } = useAuth()
  const puedeEditarRol = puede(rol, 'pacientes', 'editar')
  const puedeDesactivarRol = puede(rol, 'pacientes', 'desactivar')

  const [paciente, setPaciente] = useState(null)
  const [alergia, setAlergia] = useState('')
  const [tipoSangre, setTipoSangre] = useState('')
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const [confirmando, setConfirmando] = useState(false)
  const [desactivando, setDesactivando] = useState(false)
  const [errorDesactivar, setErrorDesactivar] = useState('')

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  useEffect(() => {
    let cancelado = false
    setCargando(true)
    setError(null)
    obtenerPaciente(id)
      .then(async (p) => {
        if (cancelado) return
        setPaciente(p)
        // Los catálogos solo sirven para mostrar nombres; si fallan, se muestra el ID.
        const [alergias, tipos] = await Promise.allSettled([listarAlergias(), listarTiposSangre()])
        if (cancelado) return
        if (alergias.status === 'fulfilled') {
          setAlergia(alergias.value.find((a) => a.idAlergias === p.idAlergias)?.nombre ?? '')
        }
        if (tipos.status === 'fulfilled') {
          setTipoSangre(tipos.value.find((t) => t.idTipoDeSangre === p.idTipoDeSangre)?.nombre ?? '')
        }
      })
      .catch((e) => {
        if (!cancelado) setError(normalizarError(e))
      })
      .finally(() => {
        if (!cancelado) setCargando(false)
      })
    return () => {
      cancelado = true
    }
  }, [id])

  const desactivar = async () => {
    setDesactivando(true)
    setErrorDesactivar('')
    try {
      await desactivarPaciente(id)
      navigate('/pacientes', {
        replace: true,
        state: { mensaje: `El paciente ${nombreCompleto(paciente)} fue desactivado.` },
      })
    } catch (e) {
      setErrorDesactivar(normalizarError(e).message)
      setDesactivando(false)
    }
  }

  if (cargando) return <Loading texto="Cargando paciente..." />

  if (error) {
    return (
      <div className="space-y-4">
        <ErrorAlert
          titulo={error.status === 404 ? 'Paciente no encontrado' : 'No se pudo cargar el paciente'}
          mensaje={error.message}
        />
        <Link to="/pacientes" className="text-sm font-medium text-teal-800 hover:underline">
          &larr; Volver a pacientes
        </Link>
      </div>
    )
  }

  const p = paciente
  const estaActivo = p.activo !== false
  const puedeEditar = puedeEditarRol && estaActivo
  const puedeDesactivar = puedeDesactivarRol && estaActivo
  return (
    <div className="space-y-4">
      <Link to="/pacientes" className="text-sm font-medium text-teal-800 hover:underline">
        &larr; Volver a pacientes
      </Link>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">{nombreCompleto(p)}</h1>
          <p className="text-sm text-slate-500">
            DPI {p.dpiPaciente}
            {!estaActivo && (
              <span className="ml-2 rounded-full bg-slate-200 px-2.5 py-0.5 text-xs font-medium text-slate-600">Inactivo</span>
            )}
          </p>
        </div>
        <div className="flex gap-3">
          {puedeEditar && (
            <Link
              to={`/pacientes/${p.idPaciente}/editar`}
              className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Editar
            </Link>
          )}
          {puedeDesactivar && (
            <button
              type="button"
              onClick={() => {
                setErrorDesactivar('')
                setConfirmando(true)
              }}
              className="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
            >
              Desactivar
            </button>
          )}
        </div>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <dl className="grid gap-x-6 gap-y-5 sm:grid-cols-2 lg:grid-cols-3">
          <Dato etiqueta="Nombre">{p.nombre}</Dato>
          <Dato etiqueta="Apellido paterno">{p.apellidoPaterno}</Dato>
          <Dato etiqueta="Apellido materno">{p.apellidoMaterno}</Dato>
          <Dato etiqueta="Fecha de nacimiento">
            {p.fechaNacimiento ? `${formatearFecha(p.fechaNacimiento)} (${calcularEdad(p.fechaNacimiento)} años)` : ''}
          </Dato>
          <Dato etiqueta="Sexo">{p.sexo === 'M' ? 'Masculino' : p.sexo === 'F' ? 'Femenino' : ''}</Dato>
          <Dato etiqueta="Teléfono">{p.telefono}</Dato>
          <Dato etiqueta="Correo electrónico">{p.correoElectronico}</Dato>
          <Dato etiqueta="Dirección">{p.direccion}</Dato>
          <Dato etiqueta="Tipo de sangre">{tipoSangre || (p.idTipoDeSangre ? `ID ${p.idTipoDeSangre}` : '')}</Dato>
          <Dato etiqueta="Alergia">{alergia || (p.idAlergias ? `ID ${p.idAlergias}` : '')}</Dato>
          <Dato etiqueta="Fecha de registro">{formatearFechaHora(p.fechaRegistro)}</Dato>
          <Dato etiqueta="Estado">{p.activo === false ? 'Inactivo' : 'Activo'}</Dato>
        </dl>
      </div>

      <ConfirmDialog
        abierto={confirmando}
        titulo="Desactivar paciente"
        mensaje={`¿Desea desactivar a ${nombreCompleto(p)}? El registro no se elimina, pero dejará de aparecer en el listado.`}
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
