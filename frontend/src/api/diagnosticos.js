import client from './client.js'
import { RUTAS } from './rutas.js'

/** Filtros opcionales y combinables: idTipoDiagnostico, fecha. */
export const listarDiagnosticos = ({ idTipoDiagnostico, fecha } = {}) => {
  const params = {}
  if (idTipoDiagnostico) params.idTipoDiagnostico = idTipoDiagnostico
  if (fecha) params.fecha = fecha
  return client.get(RUTAS.diagnosticos, { params }).then((r) => r.data)
}
export const obtenerDiagnostico = (id) => client.get(`${RUTAS.diagnosticos}/${id}`).then((r) => r.data)
export const crearDiagnostico = (datos) => client.post(RUTAS.diagnosticos, datos).then((r) => r.data)
export const actualizarDiagnostico = (id, datos) =>
  client.put(`${RUTAS.diagnosticos}/${id}`, datos).then((r) => r.data)
/** 409 si está vinculado a una consulta o tiene tratamientos. */
export const desactivarDiagnostico = (id) => client.delete(`${RUTAS.diagnosticos}/${id}`)
