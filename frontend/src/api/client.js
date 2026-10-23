import axios from 'axios'
import { RUTAS } from './rutas.js'
import { obtenerToken, notificarNoAutorizado } from './sesion.js'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

client.interceptors.request.use((config) => {
  const token = obtenerToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => {
    const url = error.config?.url ?? ''
    // Un 401 en el propio login significa "credenciales incorrectas": lo muestra el
    // formulario. Cualquier otro 401 es token ausente/expirado: cerrar sesión.
    const esLogin = url.endsWith(RUTAS.auth.login)
    if (error.response?.status === 401 && !esLogin) notificarNoAutorizado()
    return Promise.reject(error)
  },
)

export default client
