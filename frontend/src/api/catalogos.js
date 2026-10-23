import client from './client.js'
import { RUTAS } from './rutas.js'

export const listarAlergias = () => client.get(RUTAS.alergias).then((r) => r.data)
export const listarTiposSangre = () => client.get(RUTAS.tiposSangre).then((r) => r.data)

export const listarMedicos = () => client.get(RUTAS.medicos).then((r) => r.data)
export const listarEstadosCita = () => client.get(RUTAS.estadosCita).then((r) => r.data)
export const listarTiposDiagnostico = () => client.get(RUTAS.tiposDiagnostico).then((r) => r.data)

/** GET /api/estado-cita/validar-transicion?origen=&destino= -> { permitida } (nombres = `descripcion`) */
export const validarTransicionCita = (origen, destino) =>
  client
    .get(`${RUTAS.estadosCita}/validar-transicion`, { params: { origen, destino } })
    .then((r) => r.data.permitida)

export const listarHabitaciones = () => client.get(RUTAS.habitaciones).then((r) => r.data)

/** Catálogo con codigo (N1..N5) y color (Rojo, Naranja...): su codigo coincide con triaje.nivelDeEmergencia. */
export const listarNivelesUrgencia = () => client.get(RUTAS.nivelesUrgencia).then((r) => r.data)

export const listarEstadosHabitacion = () => client.get(RUTAS.estadosHabitacion).then((r) => r.data)
export const listarTiposHabitacion = () => client.get(RUTAS.tiposHabitacion).then((r) => r.data)
export const listarEstadosHospitalizacion = () => client.get(RUTAS.estadosHospitalizacion).then((r) => r.data)
export const listarClinicas = () => client.get(RUTAS.clinicas).then((r) => r.data)
export const listarTurnos = () => client.get(RUTAS.turnos).then((r) => r.data)
/** Solo lectura aquí (la administración de enfermeros es de otra fase). */
export const listarEnfermeros = () => client.get(RUTAS.enfermeros).then((r) => r.data)

/** Métodos de pago: solo lectura aquí (ADMIN, CAJERO, RECEPCIONISTA); la administración del catálogo es de otra fase. */
export const listarMetodosPago = () => client.get(RUTAS.metodosPago).then((r) => r.data)
export const listarEstadosFactura = () => client.get(RUTAS.estadosFactura).then((r) => r.data)
