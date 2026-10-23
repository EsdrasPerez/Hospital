import client from './client.js'
import { RUTAS } from './rutas.js'

// /api/triaje es el CATÁLOGO de niveles de triaje (Rojo, Naranja, ...) que se asigna a cada emergencia.
export const listarTriajes = () => client.get(RUTAS.triaje).then((r) => r.data)
export const obtenerTriaje = (id) => client.get(`${RUTAS.triaje}/${id}`).then((r) => r.data)
export const crearTriaje = (datos) => client.post(RUTAS.triaje, datos).then((r) => r.data)
export const actualizarTriaje = (id, datos) => client.put(`${RUTAS.triaje}/${id}`, datos).then((r) => r.data)
export const desactivarTriaje = (id) => client.delete(`${RUTAS.triaje}/${id}`)
