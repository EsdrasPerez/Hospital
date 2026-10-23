import client from './client.js'
import { RUTAS } from './rutas.js'

export const listarHospitalizaciones = () => client.get(RUTAS.hospitalizaciones).then((r) => r.data)
export const listarHospitalizacionesActivas = () => client.get(`${RUTAS.hospitalizaciones}/activas`).then((r) => r.data)
export const listarHospitalizacionesDePaciente = (idPaciente) =>
  client.get(`${RUTAS.hospitalizaciones}/paciente/${idPaciente}`).then((r) => r.data)
export const obtenerHospitalizacion = (id) => client.get(`${RUTAS.hospitalizaciones}/${id}`).then((r) => r.data)
export const registrarHospitalizacion = (datos) => client.post(RUTAS.hospitalizaciones, datos).then((r) => r.data)
export const actualizarHospitalizacion = (id, datos) =>
  client.put(`${RUTAS.hospitalizaciones}/${id}`, datos).then((r) => r.data)
/** POST /api/hospitalizaciones/{id}/alta?fechaAlta= (ISO local; si se omite usa la hora actual). */
export const darAltaHospitalizacion = (id, fechaAlta) =>
  client.post(`${RUTAS.hospitalizaciones}/${id}/alta`, null, { params: fechaAlta ? { fechaAlta } : {} }).then((r) => r.data)
export const desactivarHospitalizacion = (id) => client.delete(`${RUTAS.hospitalizaciones}/${id}`)

/** GET /api/hospitalizaciones/paciente/{id}/resumen (ADMIN, CAJERO, RECEPCIONISTA): ingreso, alta, estado y habitación; sin motivo ni diagnóstico. */
export const listarResumenHospitalizacionesDePaciente = (idPaciente) =>
  client.get(`${RUTAS.hospitalizaciones}/paciente/${idPaciente}/resumen`).then((r) => r.data)
