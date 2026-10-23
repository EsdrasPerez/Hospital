import { useEffect, useState } from 'react'
import { Alert, Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap'
import { actualizarLinea, agregarLinea } from '../../api/facturas.js'
import { listarCuentas } from '../../api/cuentas.js'
import { registrarPago } from '../../api/pagos.js'
import FormField from '../../components/FormField.jsx'
import { aCentavos, centavosATexto, deCentavos, errorMonto } from '../../utils/dinero.js'
import { normalizarError } from '../../utils/errores.js'
import { formatearMoneda } from '../../utils/formato.js'

/**
 * Agregar (linea = null) o editar una línea de la factura. El importe lo calcula el backend (cantidad x precio, RN-144)
 * y recalcula subtotal y total de la factura (RN-145); aquí solo se muestra una vista previa en centavos.
 */
export function LineaDialog({ idFactura, linea, onCerrar, onListo }) {
  const [form, setForm] = useState({
    concepto: linea?.concepto ?? '',
    cantidad: linea ? String(linea.cantidad) : '1',
    precioUnitario: linea ? centavosATexto(aCentavos(linea.precioUnitario) ?? 0) : '',
  })
  const [errores, setErrores] = useState({})
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  const cambiar = (e) => {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const cantidad = /^\d+$/.test(form.cantidad) ? Number(form.cantidad) : null
  const precioCent = errorMonto(form.precioUnitario, { permitirCero: true }) ? null : aCentavos(form.precioUnitario)
  const importeCent = cantidad && precioCent !== null ? cantidad * precioCent : null

  const confirmar = async () => {
    const e = {}
    if (!form.concepto.trim()) e.concepto = 'El concepto es obligatorio.'
    else if (form.concepto.trim().length > 200) e.concepto = 'El concepto no puede superar los 200 caracteres.'
    if (!cantidad || cantidad < 1 || cantidad > 100000) e.cantidad = 'La cantidad debe ser un entero entre 1 y 100000.'
    const m = errorMonto(form.precioUnitario, { etiqueta: 'El precio unitario', permitirCero: true })
    if (m) e.precioUnitario = m
    setErrores(e)
    if (Object.keys(e).length) return

    setGuardando(true)
    setError('')
    try {
      const datos = { idFactura, concepto: form.concepto.trim(), cantidad, precioUnitario: deCentavos(precioCent) }
      if (linea) await actualizarLinea(linea.idDetalle, datos)
      else await agregarLinea(datos)
      onListo(linea ? 'Línea actualizada; el backend recalculó los totales.' : 'Línea agregada; el backend recalculó los totales.')
    } catch (err) {
      // 400: `detalles` por campo (concepto, cantidad, precioUnitario, idFactura); 404: no existe;
      // 422: factura pagada/cancelada/anulada (RN-146), línea de otra factura, total por debajo de lo pagado, importe fuera de rango
      const { message, detalles } = normalizarError(err)
      setErrores(detalles ?? {})
      setError(Object.keys(detalles ?? {}).length ? 'Revise los campos marcados.' : message)
      setGuardando(false)
    }
  }

  return (
    <Modal isOpen toggle={guardando ? undefined : onCerrar} centered fade={false}>
      <ModalHeader closeAriaLabel="Cerrar" toggle={guardando ? undefined : onCerrar}>
        {linea ? 'Editar línea' : 'Agregar línea'}
      </ModalHeader>
      <ModalBody>
        <div className="row">
          <FormField className="col-12" nombre="concepto" etiqueta="Concepto" valor={form.concepto} onChange={cambiar} error={errores.concepto} requerido maxLength={200} />
          <FormField nombre="cantidad" etiqueta="Cantidad" tipo="number" min="1" step="1" valor={form.cantidad} onChange={cambiar} error={errores.cantidad} requerido />
          <FormField
            nombre="precioUnitario"
            etiqueta="Precio unitario (Q)"
            inputMode="decimal"
            valor={form.precioUnitario}
            onChange={cambiar}
            error={errores.precioUnitario}
            requerido
            placeholder="0.00"
          />
        </div>
        <p className="mb-0">
          Importe de la línea: <strong>{importeCent === null ? '—' : formatearMoneda(deCentavos(importeCent))}</strong>
        </p>
        {error && (
          <Alert color="danger" fade={false} role="alert" className="mt-3 mb-0">
            {error}
          </Alert>
        )}
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" outline onClick={onCerrar} disabled={guardando}>
          Cancelar
        </Button>
        <Button color="primary" onClick={confirmar} disabled={guardando}>
          {guardando ? 'Guardando...' : linea ? 'Guardar línea' : 'Agregar línea'}
        </Button>
      </ModalFooter>
    </Modal>
  )
}

/**
 * Registrar un pago sobre la factura. El backend valida (422) que el monto sea mayor a 0 y no supere el saldo, que la factura
 * esté Pendiente y que método y cuenta sean válidos; si el saldo queda en 0 la factura pasa sola a Pagada. La pantalla
 * adelanta el control del monto (en centavos) solo como ayuda visual.
 */
export function PagoDialog({ factura, saldoCentavos, metodos, onCerrar, onListo }) {
  const [form, setForm] = useState({
    monto: '',
    idMetodoPago: factura.idMetodoPago ? String(factura.idMetodoPago) : '',
    referencia: '',
    idCuenta: '',
  })
  const [cuentas, setCuentas] = useState([])
  const [errores, setErrores] = useState({})
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  // Cuentas activas del paciente (opcionales). Si el rol no las puede leer, el selector queda sin opciones.
  useEffect(() => {
    let cancelado = false
    listarCuentas({ idPaciente: factura.idPaciente })
      .then((l) => {
        if (!cancelado) setCuentas(l.filter((c) => c.activo !== false))
      })
      .catch(() => {})
    return () => {
      cancelado = true
    }
  }, [factura.idPaciente])

  const cambiar = (e) => {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const montoCent = errorMonto(form.monto) ? null : aCentavos(form.monto)
  const restante = montoCent === null ? null : saldoCentavos - montoCent

  const confirmar = async () => {
    const e = {}
    const m = errorMonto(form.monto, { etiqueta: 'El monto' })
    if (m) e.monto = m
    else if (montoCent > saldoCentavos) e.monto = `El monto no puede superar el saldo pendiente (${formatearMoneda(deCentavos(saldoCentavos))}).`
    if (!form.idMetodoPago) e.idMetodoPago = 'Seleccione el método de pago.'
    if (form.referencia.length > 100) e.referencia = 'La referencia no puede superar los 100 caracteres.'
    setErrores(e)
    if (Object.keys(e).length) return

    setGuardando(true)
    setError('')
    try {
      await registrarPago({
        idFactura: factura.idFactura,
        idCuenta: form.idCuenta ? Number(form.idCuenta) : null,
        idMetodoPago: Number(form.idMetodoPago),
        monto: deCentavos(montoCent),
        referencia: form.referencia.trim() || null,
      })
      onListo(
        `Pago de ${formatearMoneda(deCentavos(montoCent))} registrado.` +
          (montoCent === saldoCentavos ? ' La factura quedó pagada.' : ''),
      )
    } catch (err) {
      // 400: `detalles` por campo (idFactura, idMetodoPago, monto, referencia); 403: sin permiso; 422: monto mayor al saldo,
      // factura no Pendiente, método o cuenta inexistentes/inactivos o de otro paciente
      const { message, detalles } = normalizarError(err)
      setErrores(detalles ?? {})
      setError(Object.keys(detalles ?? {}).length ? 'Revise los campos marcados.' : message)
      setGuardando(false)
    }
  }

  return (
    <Modal isOpen toggle={guardando ? undefined : onCerrar} centered fade={false}>
      <ModalHeader closeAriaLabel="Cerrar" toggle={guardando ? undefined : onCerrar}>
        Registrar pago
      </ModalHeader>
      <ModalBody>
        <p>
          Factura #{factura.idFactura} · saldo pendiente <strong>{formatearMoneda(deCentavos(saldoCentavos))}</strong>
        </p>
        <div className="row">
          <div className="col-12">
            <FormField
              className="col-12 px-0"
              nombre="monto"
              etiqueta="Monto (Q)"
              inputMode="decimal"
              valor={form.monto}
              onChange={cambiar}
              error={errores.monto}
              requerido
              placeholder="0.00"
            />
            <button
              type="button"
              className="btn btn-outline-secondary btn-sm btn-round mb-3"
              onClick={() => {
                setForm((f) => ({ ...f, monto: centavosATexto(saldoCentavos) }))
                setErrores((er) => ({ ...er, monto: undefined }))
              }}
            >
              Pagar el saldo completo
            </button>
          </div>
          <FormField
            nombre="idMetodoPago"
            etiqueta="Método de pago"
            tipo="select"
            opciones={metodos.filter((x) => x.activo !== false).map((x) => ({ valor: String(x.idMetodoPago), etiqueta: x.descripcion }))}
            valor={form.idMetodoPago}
            onChange={cambiar}
            error={errores.idMetodoPago}
            requerido
          />
          <FormField
            nombre="idCuenta"
            etiqueta="Cuenta (opcional)"
            tipo="select"
            placeholder="Sin cuenta"
            opciones={cuentas.map((c) => ({ valor: String(c.idCuenta), etiqueta: `#${c.idCuenta} · ${c.estado} · saldo ${formatearMoneda(c.saldo)}` }))}
            valor={form.idCuenta}
            onChange={cambiar}
            error={errores.idCuenta}
          />
          <FormField className="col-12" nombre="referencia" etiqueta="Referencia (opcional)" valor={form.referencia} onChange={cambiar} error={errores.referencia} maxLength={100} />
        </div>
        {restante !== null && (
          <p className={`mb-0${restante < 0 ? ' text-danger' : ''}`}>
            Saldo tras el pago: <strong>{formatearMoneda(deCentavos(Math.max(restante, 0)))}</strong>
            {restante < 0 && ' (el monto supera el saldo)'}
            {restante === 0 && ' (la factura quedará liquidada)'}
          </p>
        )}
        {error && (
          <Alert color="danger" fade={false} role="alert" className="mt-3 mb-0">
            {error}
          </Alert>
        )}
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" outline onClick={onCerrar} disabled={guardando}>
          Cancelar
        </Button>
        <Button color="primary" onClick={confirmar} disabled={guardando}>
          {guardando ? 'Registrando...' : 'Registrar pago'}
        </Button>
      </ModalFooter>
    </Modal>
  )
}
