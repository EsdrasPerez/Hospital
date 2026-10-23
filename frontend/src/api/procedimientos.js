import client from './client.js'
import { RUTAS } from './rutas.js'

// No existe filtro por emergencia en el backend: se pide la lista completa y se filtra en el cliente.
export const listarProcedimientos = () => client.get(RUTAS.procedimientosEmergencia).then((r) => r.data)
export const obtenerProcedimiento = (id) =>
  client.get(`${RUTAS.procedimientosEmergencia}/${id}`).then((r) => r.data)
export const crearProcedimiento = (datos) =>
  client.post(RUTAS.procedimientosEmergencia, datos).then((r) => r.data)
export const actualizarProcedimiento = (id, datos) =>
  client.put(`${RUTAS.procedimientosEmergencia}/${id}`, datos).then((r) => r.data)
export const desactivarProcedimiento = (id) => client.delete(`${RUTAS.procedimientosEmergencia}/${id}`)
