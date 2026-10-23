import client from './client.js'
import { RUTAS } from './rutas.js'

/** GET /api/medicamentos con filtros combinables en el servidor: criterio (nombre comercial o genérico), bajoStock y soloActivos. */
export const listarMedicamentos = ({ criterio, bajoStock, soloActivos } = {}) => {
  const params = {}
  if (criterio && criterio.trim()) params.criterio = criterio.trim()
  if (bajoStock) params.bajoStock = true
  if (soloActivos) params.soloActivos = true
  return client.get(RUTAS.medicamentos, { params }).then((r) => r.data)
}
export const obtenerMedicamento = (id) => client.get(`${RUTAS.medicamentos}/${id}`).then((r) => r.data)
export const crearMedicamento = (datos) => client.post(RUTAS.medicamentos, datos).then((r) => r.data)
/** PUT: el backend NO modifica el stock (se conserva); el stock solo cambia con ajustarStock. */
export const actualizarMedicamento = (id, datos) => client.put(`${RUTAS.medicamentos}/${id}`, datos).then((r) => r.data)
/** PATCH /api/medicamentos/{id}/stock?cambio=N (N positivo = entrada, negativo = salida; no puede dejar el stock < 0). */
export const ajustarStock = (id, cambio) =>
  client.patch(`${RUTAS.medicamentos}/${id}/stock`, null, { params: { cambio } }).then((r) => r.data)
/** DELETE = borrado lógico; falla si el medicamento está asignado a algún tratamiento. */
export const desactivarMedicamento = (id) => client.delete(`${RUTAS.medicamentos}/${id}`)
