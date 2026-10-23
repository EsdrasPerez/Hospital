import client from './client.js'
import { RUTAS } from './rutas.js'

// tratamiento-medicamento: relación (idMedicamento, idTratamiento). El backend no guarda dosis ni frecuencia.
// GET admite los filtros combinables idTratamiento e idMedicamento (se filtra en el servidor).
export const listarRecetas = ({ idTratamiento, idMedicamento } = {}) => {
  const params = {}
  if (idTratamiento) params.idTratamiento = idTratamiento
  if (idMedicamento) params.idMedicamento = idMedicamento
  return client.get(RUTAS.tratamientoMedicamento, { params }).then((r) => r.data)
}
export const asignarMedicamento = (idMedicamento, idTratamiento) =>
  client.post(RUTAS.tratamientoMedicamento, { idMedicamento, idTratamiento }).then((r) => r.data)
export const quitarMedicamento = (idMedicamento, idTratamiento) =>
  client.delete(`${RUTAS.tratamientoMedicamento}/${idMedicamento}/${idTratamiento}`)
