import client from './client.js'
import { RUTAS } from './rutas.js'

/**
 * GET /api/habitacion. Filtros opcionales que el servidor aplica juntos: piso, idTipoHabitacion,
 * idEstadoHabitacion, soloActivas y soloDisponibles.
 */
export const listarHabitaciones = ({ piso, idTipoHabitacion, idEstadoHabitacion, soloActivas, soloDisponibles } = {}) => {
  const params = {}
  if (piso !== undefined && piso !== '') params.piso = piso
  if (idTipoHabitacion) params.idTipoHabitacion = idTipoHabitacion
  if (idEstadoHabitacion) params.idEstadoHabitacion = idEstadoHabitacion
  if (soloActivas) params.soloActivas = true
  if (soloDisponibles) params.soloDisponibles = true
  return client.get(RUTAS.habitaciones, { params }).then((r) => r.data)
}
export const obtenerHabitacion = (id) => client.get(`${RUTAS.habitaciones}/${id}`).then((r) => r.data)
export const crearHabitacion = (datos) => client.post(RUTAS.habitaciones, datos).then((r) => r.data)
export const actualizarHabitacion = (id, datos) => client.put(`${RUTAS.habitaciones}/${id}`, datos).then((r) => r.data)
/** PATCH /api/habitacion/{id}/estado/{idEstado} */
export const cambiarEstadoHabitacion = (id, idEstado) =>
  client.patch(`${RUTAS.habitaciones}/${id}/estado/${idEstado}`).then((r) => r.data)
/** DELETE = borrado lógico; 422 si hay pacientes hospitalizados o ingresos de emergencia activos. */
export const desactivarHabitacion = (id) => client.delete(`${RUTAS.habitaciones}/${id}`)
