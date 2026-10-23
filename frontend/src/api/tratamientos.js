import client from './client.js'
import { RUTAS } from './rutas.js'

/** idDiagnostico y soloActivos son opcionales y combinables (sin soloActivos incluye inactivos). */
export const listarTratamientos = ({ idDiagnostico, soloActivos } = {}) => {
  const params = {}
  if (idDiagnostico) params.idDiagnostico = idDiagnostico
  if (soloActivos) params.soloActivos = true
  return client.get(RUTAS.tratamientos, { params }).then((r) => r.data)
}
export const obtenerTratamiento = (id) => client.get(`${RUTAS.tratamientos}/${id}`).then((r) => r.data)
export const crearTratamiento = (datos) => client.post(RUTAS.tratamientos, datos).then((r) => r.data)
export const actualizarTratamiento = (id, datos) =>
  client.put(`${RUTAS.tratamientos}/${id}`, datos).then((r) => r.data)
/** 422 si tiene medicamentos asociados. */
export const desactivarTratamiento = (id) => client.delete(`${RUTAS.tratamientos}/${id}`)
