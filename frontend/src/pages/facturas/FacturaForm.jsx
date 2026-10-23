import { useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { actualizarFactura, crearFactura, obtenerFactura } from '../../api/facturas.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import HospitalizacionSelector from '../../components/HospitalizacionSelector.jsx'
import Loading from '../../components/Loading.jsx'
import PacienteSelector from '../../components/PacienteSelector.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useDatosFacturacion } from '../../hooks/useDatosFacturacion.js'
import { normalizarError } from '../../utils/errores.js'

/**
 * Emitir / editar factura.
 * - Al emitir, el backend fija subtotal, IVA y total en 0, el estado en Pendiente y la fecha en la hora actual; luego los
 *   importes salen SOLO de las líneas (el IVA lo calcula el servidor con su tasa configurada). Por eso no hay campos de importe.
 * - Al editar solo se tocan método de pago y hospitalización (el paciente solo si la factura no tiene pagos).
 * - Errores: 400 con `detalles` (idPaciente, idMetodoPago); las reglas RN-xxx llegan como 422 con `message`.
 */
export default function FacturaForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const datos = useDatosFacturacion()
  const { datos: original, error: errorCarga, cargando } = useApi(`factura-form|${id ?? 'nueva'}`, () => (esEdicion ? obtenerFactura(id) : null))

  const [cambios, setCambios] = useState({})
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [guardando, setGuardando] = useState(false)

  const inicial = original
    ? {
        idPaciente: String(original.idPaciente),
        idHospitalizacion: original.idHospitalizacion ? String(original.idHospitalizacion) : '',
        idMetodoPago: original.idMetodoPago ? String(original.idMetodoPago) : '',
      }
    : { idPaciente: params.get('idPaciente') ?? '', idHospitalizacion: params.get('idHospitalizacion') ?? '', idMetodoPago: '' }
  const form = { ...inicial, ...cambios }

  if (cargando || datos.cargando) return <Loading texto="Cargando..." />

  if (errorCarga) {
    return (
      <div>
        <ErrorAlert titulo={errorCarga.status === 404 ? 'Factura no encontrada' : 'No se pudo cargar la factura'} mensaje={errorCarga.message} />
        <Link to="/facturas" className="volver-enlace">
          &larr; Volver a facturas
        </Link>
      </div>
    )
  }

  const cerrada = esEdicion && original.idEstadoFactura !== datos.idEstado('Pendiente')
  const volver = esEdicion ? `/facturas/${id}` : '/facturas'

  const cambiar = (e) => {
    const { name, value } = e.target
    setCambios((c) => ({ ...c, [name]: value, ...(name === 'idPaciente' ? { idHospitalizacion: '' } : {}) }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  // Para los selectores (PacienteSelector y HospitalizacionSelector): reciben el valor, no un evento
  const cambiarCampo = (name, value) => {
    setCambios((c) => ({ ...c, [name]: value, ...(name === 'idPaciente' ? { idHospitalizacion: '' } : {}) }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const validar = () => {
    const e = {}
    if (!/^\d+$/.test(form.idPaciente)) e.idPaciente = 'Seleccione un paciente (o indique su ID numérico).'
    if (form.idHospitalizacion && !/^\d+$/.test(form.idHospitalizacion)) e.idHospitalizacion = 'El ID de la hospitalización debe ser un número.'
    if (!form.idMetodoPago) e.idMetodoPago = 'Seleccione el método de pago.'
    return e
  }

  const enviar = async (ev) => {
    ev.preventDefault()
    setErrorGeneral('')
    const locales = validar()
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      let guardada
      if (esEdicion) {
        // El backend ignora subtotal, IVA, total, estado y fecha: solo se envían los campos editables
        guardada = await actualizarFactura(id, {
          idPaciente: original.idPaciente,
          idHospitalizacion: form.idHospitalizacion ? Number(form.idHospitalizacion) : null,
          idMetodoPago: Number(form.idMetodoPago),
        })
      } else {
        guardada = await crearFactura({
          idPaciente: Number(form.idPaciente),
          idHospitalizacion: form.idHospitalizacion ? Number(form.idHospitalizacion) : null,
          idMetodoPago: Number(form.idMetodoPago),
        })
      }
      navigate(`/facturas/${guardada.idFactura}`, {
        replace: true,
        state: { mensaje: esEdicion ? 'Factura actualizada.' : 'Factura emitida. Agregue las líneas con los conceptos a cobrar.' },
      })
    } catch (err) {
      // 400: campos obligatorios con `detalles` (idPaciente, idMetodoPago); 404: no existe; 422: paciente, hospitalización
      // o método inexistentes/inactivos, o cambios no permitidos (RN-132..RN-139)
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  const opcionesMetodo = datos.metodos
    .filter((m) => m.activo !== false || String(m.idMetodoPago) === form.idMetodoPago)
    .map((m) => ({ valor: String(m.idMetodoPago), etiqueta: m.descripcion }))
  return (
    <div>
      <Link to={volver} className="volver-enlace">
        &larr; {esEdicion ? 'Volver a la factura' : 'Volver a facturas'}
      </Link>
      <h1 className="h3 mb-0">{esEdicion ? `Editar factura #${id}` : 'Emitir factura'}</h1>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />
        {cerrada && (
          <ErrorAlert
            tipo="aviso"
            mensaje="La factura está pagada o cancelada: el backend solo permite cambiar el método de pago (RN-139). El paciente y la hospitalización no se pueden modificar."
          />
        )}

        <div className="row">
          <PacienteSelector
            valor={form.idPaciente}
            onChange={(v) => cambiarCampo('idPaciente', v)}
            error={errores.idPaciente}
            disabled={esEdicion}
            requerido
            ayuda={esEdicion ? 'El paciente no se puede cambiar desde aquí.' : undefined}
          />
          <HospitalizacionSelector
            idPaciente={form.idPaciente}
            valor={form.idHospitalizacion}
            onChange={(v) => cambiarCampo('idHospitalizacion', v)}
            error={errores.idHospitalizacion}
            disabled={cerrada}
            ayuda="Debe pertenecer al mismo paciente (RN-133)."
          />

          <FormField
            nombre="idMetodoPago"
            etiqueta="Método de pago"
            tipo="select"
            opciones={opcionesMetodo}
            valor={form.idMetodoPago}
            onChange={cambiar}
            error={errores.idMetodoPago}
            requerido
          />

        </div>

        {!esEdicion && (
          <p className="text-muted small">
            La factura se emite en estado Pendiente con importes en Q 0.00. Los conceptos cobrados se agregan como líneas en el detalle y
            el servidor calcula el subtotal, el IVA y el total.
          </p>
        )}

        <div className="acciones-grupo d-flex justify-content-end border-top pt-3">
          <Link to={volver} className="btn btn-secondary btn-round">
            Cancelar
          </Link>
          <button type="submit" disabled={guardando} className="btn btn-primary btn-round">
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Emitir factura'}
          </button>
        </div>
      </form>
    </div>
  )
}
