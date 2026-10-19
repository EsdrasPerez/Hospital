// Manejo del token JWT y de la notificación de "sesión expirada".
const CLAVE_TOKEN = 'hospital_token'

export function obtenerToken() {
  try {
    return localStorage.getItem(CLAVE_TOKEN)
  } catch {
    return null
  }
}

export function guardarToken(token) {
  try {
    localStorage.setItem(CLAVE_TOKEN, token)
  } catch {
    /* almacenamiento no disponible */
  }
}

export function borrarToken() {
  try {
    localStorage.removeItem(CLAVE_TOKEN)
  } catch {
    /* almacenamiento no disponible */
  }
}

let manejadorNoAutorizado = null

/** El AuthProvider registra aquí cómo reaccionar ante un 401. */
export function registrarManejadorNoAutorizado(fn) {
  manejadorNoAutorizado = fn
}

export function notificarNoAutorizado() {
  borrarToken()
  if (manejadorNoAutorizado) manejadorNoAutorizado()
  else if (window.location.pathname !== '/login') window.location.assign('/login')
}
