// Rutas del backend centralizadas. Pacientes usa el prefijo /api/v1 mientras el resto
// usa /api: si el backend unifica el prefijo, basta con cambiar PREFIJO_PACIENTES.
const PREFIJO = '/api'
const PREFIJO_PACIENTES = '/api/v1'

export const RUTAS = {
  auth: {
    login: `${PREFIJO}/auth/login`,
    me: `${PREFIJO}/auth/me`,
  },
  pacientes: `${PREFIJO_PACIENTES}/paciente`,
  alergias: `${PREFIJO}/alergias`,
  tiposSangre: `${PREFIJO}/tipo-sangre`,
}
