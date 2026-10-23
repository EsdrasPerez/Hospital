import client from './client.js'
import { RUTAS } from './rutas.js'

// /api/pagos: lectura y registro solo ADMIN y CAJERO; anular (DELETE) y corregir (PUT) solo ADMIN.
export const listarPagos = () => client.get(RUTAS.pagos).then((r) => r.data)
/** Por defecto incluye también los pagos anulados (activo = false); con soloActivos solo los aplicados. */
export const listarPagosDeFactura = (idFactura, { soloActivos } = {}) =>
  client.get(`${RUTAS.pagos}/factura/${idFactura}`, { params: soloActivos ? { soloActivos: true } : {} }).then((r) => r.data)
export const listarPagosDeCuenta = (idCuenta, { soloActivos } = {}) =>
  client.get(`${RUTAS.pagos}/cuenta/${idCuenta}`, { params: soloActivos ? { soloActivos: true } : {} }).then((r) => r.data)
export const registrarPago = (datos) => client.post(RUTAS.pagos, datos).then((r) => r.data)
/** DELETE = anular (borrado lógico). Permitido con la factura Pendiente o Pagada (una Pagada vuelve a Pendiente). */
export const anularPago = (id) => client.delete(`${RUTAS.pagos}/${id}`)
