import client from './client.js'
import { RUTAS } from './rutas.js'

/** GET /api/cuentas con filtros COMBINABLES en el servidor: idPaciente, idHospitalizacion, estado y soloActivas. */
export const listarCuentas = ({ idPaciente, idHospitalizacion, estado, soloActivas } = {}) => {
  const params = {}
  if (idPaciente) params.idPaciente = idPaciente
  if (idHospitalizacion) params.idHospitalizacion = idHospitalizacion
  if (estado) params.estado = estado
  if (soloActivas) params.soloActivas = true
  return client.get(RUTAS.cuentas, { params }).then((r) => r.data)
}
export const obtenerCuenta = (id) => client.get(`${RUTAS.cuentas}/${id}`).then((r) => r.data)
export const crearCuenta = (datos) => client.post(RUTAS.cuentas, datos).then((r) => r.data)
export const actualizarCuenta = (id, datos) => client.put(`${RUTAS.cuentas}/${id}`, datos).then((r) => r.data)
/** PATCH /api/cuentas/{id}/ajustar-saldo?monto=N (positivo suma, negativo resta; el saldo no puede quedar < 0). */
export const ajustarSaldoCuenta = (id, monto) =>
  client.patch(`${RUTAS.cuentas}/${id}/ajustar-saldo`, null, { params: { monto } }).then((r) => r.data)
/** DELETE = desactivar (borrado lógico); 409 si la cuenta tiene pagos. */
export const desactivarCuenta = (id) => client.delete(`${RUTAS.cuentas}/${id}`)
