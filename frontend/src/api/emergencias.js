import client from './client.js'
import { RUTAS } from './rutas.js'

/** GET /api/emergencia. Filtros opcionales y combinables: idPaciente y soloActivas. */
export const listarEmergencias = ({ idPaciente, soloActivas } = {}) => {
  const params = {}
  if (idPaciente) params.idPaciente = idPaciente
  if (soloActivas) params.soloActivas = true
  return client.get(RUTAS.emergencias, { params }).then((r) => r.data)
}
export const obtenerEmergencia = (id) => client.get(`${RUTAS.emergencias}/${id}`).then((r) => r.data)
export const crearEmergencia = (datos) => client.post(RUTAS.emergencias, datos).then((r) => r.data)
/** El PUT no modifica la fecha de alta (se conserva); el alta se registra solo con dar-alta. */
export const actualizarEmergencia = (id, datos) =>
  client.put(`${RUTAS.emergencias}/${id}`, datos).then((r) => r.data)

/** PATCH /api/emergencia/{id}/dar-alta?fechaHoraAlta= (ISO local; si se omite, el servidor usa la hora actual). */
export const darAltaEmergencia = (id, fechaHoraAlta) =>
  client
    .patch(`${RUTAS.emergencias}/${id}/dar-alta`, null, { params: fechaHoraAlta ? { fechaHoraAlta } : {} })
    .then((r) => r.data)

/** DELETE = borrado lógico. 409 si tiene procedimientos activos. */
export const desactivarEmergencia = (id) => client.delete(`${RUTAS.emergencias}/${id}`)
