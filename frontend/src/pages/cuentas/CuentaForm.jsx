import { useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { actualizarCuenta, crearCuenta, obtenerCuenta } from '../../api/cuentas.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import HospitalizacionSelector from '../../components/HospitalizacionSelector.jsx'
import Loading from '../../components/Loading.jsx'
import PacienteSelector from '../../components/PacienteSelector.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useDatosFacturacion } from '../../hooks/useDatosFacturacion.js'
import { aCentavos, deCentavos, errorMonto } from '../../utils/dinero.js'
import { normalizarError } from '../../utils/errores.js'
import { formatearMoneda } from '../../utils/formato.js'
import { ESTADOS_CUENTA } from './utilsCuenta.js'

/**
 * Abrir / editar cuenta. Al abrir se puede indicar el saldo inicial (>= 0) y la cuenta nace Abierta; al editar el saldo es
 * de solo lectura porque solo se modifica con "Ajustar saldo" (PATCH) y el backend lo ignora en el PUT. Una cuenta solo
 * puede quedar Liquidada con saldo 0.
 */
export default function CuentaForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const datos = useDatosFacturacion()
  const { datos: original, error: errorCarga, cargando } = useApi(`cuenta-form|${id ?? 'nueva'}`, () => (esEdicion ? obtenerCuenta(id) : null))

  const [cambios, setCambios] = useState({})
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [guardando, setGuardando] = useState(false)

  const inicial = original
    ? {
        idPaciente: String(original.idPaciente),
        idHospitalizacion: original.idHospitalizacion ? String(original.idHospitalizacion) : '',
        estado: original.estado ?? 'Abierta',
        saldo: '',
      }
    : { idPaciente: params.get('idPaciente') ?? '', idHospitalizacion: '', estado: 'Abierta', saldo: '0.00' }
  const form = { ...inicial, ...cambios }

  if (cargando || datos.cargando) return <Loading texto="Cargando..." />

  if (errorCarga) {
    return (
      <div>
        <ErrorAlert titulo={errorCarga.status === 404 ? 'Cuenta no encontrada' : 'No se pudo cargar la cuenta'} mensaje={errorCarga.message} />
        <Link to="/cuentas" className="volver-enlace">
          &larr; Volver a cuentas
        </Link>
      </div>
    )
  }

  const volver = esEdicion ? `/cuentas/${id}` : '/cuentas'

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

  const enviar = async (ev) => {
    ev.preventDefault()
    setErrorGeneral('')
    const locales = {}
    if (!/^\d+$/.test(form.idPaciente)) locales.idPaciente = 'Seleccione un paciente (o indique su ID numérico).'
    if (form.idHospitalizacion && !/^\d+$/.test(form.idHospitalizacion)) locales.idHospitalizacion = 'El ID de la hospitalización debe ser un número.'
    if (!esEdicion) {
      const m = errorMonto(form.saldo, { etiqueta: 'El saldo inicial', permitirCero: true })
      if (m) locales.saldo = m
    }
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const idHospitalizacion = form.idHospitalizacion ? Number(form.idHospitalizacion) : null
      const guardada = esEdicion
        ? await actualizarCuenta(id, {
            idPaciente: original.idPaciente,
            idHospitalizacion,
            estado: form.estado,
          })
        : await crearCuenta({ idPaciente: Number(form.idPaciente), idHospitalizacion, saldo: deCentavos(aCentavos(form.saldo)) })
      navigate(`/cuentas/${guardada.idCuenta}`, { replace: true, state: { mensaje: esEdicion ? 'Cuenta actualizada.' : 'Cuenta abierta.' } })
    } catch (err) {
      // 400: `detalles` por campo (idPaciente, estado, saldo); 404: no existe; 409: la hospitalización ya tiene una cuenta activa;
      // 422: paciente u hospitalización inexistentes/inactivos o de otro paciente, Liquidada con saldo distinto de 0
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  return (
    <div>
      <Link to={volver} className="volver-enlace">
        &larr; {esEdicion ? 'Volver a la cuenta' : 'Volver a cuentas'}
      </Link>
      <h1 className="h3 mb-0">{esEdicion ? `Editar cuenta #${id}` : 'Abrir cuenta'}</h1>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />

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
            ayuda="Una hospitalización solo puede tener una cuenta activa."
          />

          {esEdicion ? (
            <>
              <FormField
                nombre="estado"
                etiqueta="Estado"
                tipo="select"
                opciones={[...new Set([...ESTADOS_CUENTA, form.estado])].map((e) => ({ valor: e, etiqueta: e }))}
                valor={form.estado}
                onChange={cambiar}
                error={errores.estado}
              />
              <div className="col-md-6">
                <div className="dato-etiqueta">Saldo actual</div>
                <div className="dato-valor mb-1">{formatearMoneda(original.saldo)}</div>
                <small className="form-text text-muted">El saldo solo se modifica con la acción "Ajustar saldo" del detalle.</small>
              </div>
            </>
          ) : (
            <FormField
              nombre="saldo"
              etiqueta="Saldo inicial (Q)"
              inputMode="decimal"
              valor={form.saldo}
              onChange={cambiar}
              error={errores.saldo}
              ayuda="Opcional; no puede ser negativo (hasta 2 decimales)."
            />
          )}
        </div>

        <div className="acciones-grupo d-flex justify-content-end border-top pt-3">
          <Link to={volver} className="btn btn-secondary btn-round">
            Cancelar
          </Link>
          <button type="submit" disabled={guardando} className="btn btn-primary btn-round">
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Abrir cuenta'}
          </button>
        </div>
      </form>
    </div>
  )
}
