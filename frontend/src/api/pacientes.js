import client from './client.js'
import { RUTAS } from './rutas.js'

/** GET /api/v1/paciente?q=&todos= (sin paginación en el backend) */
export const listarPacientes = ({ q, todos } = {}) => {
  const params = {}
  if (q && q.trim()) params.q = q.trim()
  if (todos) params.todos = true
  return client.get(RUTAS.pacientes, { params }).then((r) => r.data)
}

export const obtenerPaciente = (id) => client.get(`${RUTAS.pacientes}/${id}`).then((r) => r.data)

export const obtenerPacientePorDpi = (dpi) =>
  client.get(`${RUTAS.pacientes}/dpi/${dpi}`).then((r) => r.data)

export const crearPaciente = (datos) => client.post(RUTAS.pacientes, datos).then((r) => r.data)

export const actualizarPaciente = (id, datos) =>
  client.put(`${RUTAS.pacientes}/${id}`, datos).then((r) => r.data)

/** DELETE = borrado lógico (activo = false). Responde 204. */
export const desactivarPaciente = (id) => client.delete(`${RUTAS.pacientes}/${id}`)
