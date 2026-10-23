import client from './client.js'
import { RUTAS } from './rutas.js'

/**
 * GET /api/facturas con filtros COMBINABLES en el servidor: idPaciente, idHospitalizacion, idEstadoFactura,
 * desde y hasta (fecha de emisión, YYYY-MM-DD, inclusivos) y soloActivas.
 */
export const listarFacturas = ({ idPaciente, idHospitalizacion, idEstadoFactura, desde, hasta, soloActivas } = {}) => {
  const params = {}
  if (idPaciente) params.idPaciente = idPaciente
  if (idHospitalizacion) params.idHospitalizacion = idHospitalizacion
  if (idEstadoFactura) params.idEstadoFactura = idEstadoFactura
  if (desde) params.desde = desde
  if (hasta) params.hasta = hasta
  if (soloActivas) params.soloActivas = true
  return client.get(RUTAS.facturas, { params }).then((r) => r.data)
}
export const obtenerFactura = (id) => client.get(`${RUTAS.facturas}/${id}`).then((r) => r.data)
/** GET /api/facturas/{id}/saldo -> { total, totalPagado, saldoPendiente, cantidadPagos, liquidada } (ADMIN, CAJERO, RECEPCIONISTA). */
export const obtenerSaldoFactura = (id) => client.get(`${RUTAS.facturas}/${id}/saldo`).then((r) => r.data)
/** El backend IGNORA subtotal, iva, total, estado y fecha: solo se envían idPaciente, idHospitalizacion e idMetodoPago. */
export const crearFactura = (datos) => client.post(RUTAS.facturas, datos).then((r) => r.data)
export const actualizarFactura = (id, datos) => client.put(`${RUTAS.facturas}/${id}`, datos).then((r) => r.data)
/** PATCH /api/facturas/{id}/estado/{idEstadoFactura} */
export const cambiarEstadoFactura = (id, idEstado) => client.patch(`${RUTAS.facturas}/${id}/estado/${idEstado}`).then((r) => r.data)
/** DELETE = anular (borrado lógico). 409 si tiene pagos activos; 422 si está Pagada o ya anulada. */
export const anularFactura = (id) => client.delete(`${RUTAS.facturas}/${id}`)

// Líneas de la factura (/api/detalle-factura). Cada alta, cambio o baja hace que el backend recalcule subtotal y total.
export const listarLineasFactura = (idFactura) =>
  client.get(RUTAS.detalleFactura, { params: { idFactura } }).then((r) => r.data)
export const agregarLinea = (datos) => client.post(RUTAS.detalleFactura, datos).then((r) => r.data)
export const actualizarLinea = (id, datos) => client.put(`${RUTAS.detalleFactura}/${id}`, datos).then((r) => r.data)
export const quitarLinea = (id) => client.delete(`${RUTAS.detalleFactura}/${id}`)
