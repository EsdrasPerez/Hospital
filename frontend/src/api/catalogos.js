import client from './client.js'
import { RUTAS } from './rutas.js'

export const listarAlergias = () => client.get(RUTAS.alergias).then((r) => r.data)
export const listarTiposSangre = () => client.get(RUTAS.tiposSangre).then((r) => r.data)
