import client from './client.js'
import { RUTAS } from './rutas.js'

/** POST /api/auth/login -> { token, type } */
export const iniciarSesion = (username, password) =>
  client.post(RUTAS.auth.login, { username, password }).then((r) => r.data)

/** GET /api/auth/me -> { username, roles: ['ROLE_ADMIN'] } */
export const obtenerUsuarioActual = () => client.get(RUTAS.auth.me).then((r) => r.data)
