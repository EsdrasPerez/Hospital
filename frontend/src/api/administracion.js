import client from './client.js'
import { RUTAS } from './rutas.js'

/** CRUD genérico de un catálogo: GET lista, POST, PUT /{id} y DELETE /{id} (desactivación lógica). */
export const crud = (ruta) => ({
  listar: () => client.get(ruta).then((r) => r.data),
  crear: (datos) => client.post(ruta, datos).then((r) => r.data),
  actualizar: (id, datos) => client.put(`${ruta}/${id}`, datos).then((r) => r.data),
  eliminar: (id) => client.delete(`${ruta}/${id}`),
})

// ---- Médicos ----
/** GET /api/medico?criterio=&soloActivos= */
export const listarMedicos = ({ criterio, soloActivos } = {}) => {
  const params = {}
  if (criterio && criterio.trim()) params.criterio = criterio.trim()
  if (soloActivos) params.soloActivos = true
  return client.get(RUTAS.medicos, { params }).then((r) => r.data)
}
export const obtenerMedico = (id) => client.get(`${RUTAS.medicos}/${id}`).then((r) => r.data)
export const crearMedico = (datos) => client.post(RUTAS.medicos, datos).then((r) => r.data)
export const actualizarMedico = (id, datos) => client.put(`${RUTAS.medicos}/${id}`, datos).then((r) => r.data)
/** PATCH /api/medico/{id}/estado?nuevoEstado=true|false (422 si tiene citas, hospitalizaciones o emergencias activas). */
export const cambiarEstadoMedico = (id, nuevoEstado) =>
  client.patch(`${RUTAS.medicos}/${id}/estado`, null, { params: { nuevoEstado } }).then((r) => r.data)

// ---- Especialidades de un médico (/api/medico-especialidades) ----
export const listarEspecialidadesDeMedico = (idMedico) => client.get(`${RUTAS.medicoEspecialidades}/medico/${idMedico}`).then((r) => r.data)
export const listarAsignacionesEspecialidad = () => client.get(RUTAS.medicoEspecialidades).then((r) => r.data)
export const asignarEspecialidad = (idEspecialidad, idMedico) =>
  client.post(RUTAS.medicoEspecialidades, { idEspecialidad, idMedico }).then((r) => r.data)
export const quitarEspecialidad = (idEspecialidad, idMedico) => client.delete(`${RUTAS.medicoEspecialidades}/${idEspecialidad}/${idMedico}`)

// ---- Usuarios y roles ----
export const listarUsuarios = () => client.get(RUTAS.usuarios).then((r) => r.data)
/** POST /api/auth/register { username, password, rol (nombre) }: 409 si el usuario existe, 422 si el rol no existe o está inactivo. */
export const registrarUsuario = (username, password, rol) => client.post(RUTAS.auth.register, { username, password, rol })
/** PUT /api/usuarios/{id}: solo cambia lo que se envía (nombreUsuario, password, estado, idRol). */
export const actualizarUsuario = (id, datos) => client.put(`${RUTAS.usuarios}/${id}`, datos).then((r) => r.data)
export const listarRoles = () => client.get(RUTAS.roles).then((r) => r.data)

// ---- Estados de cita: transiciones ----
export const transicionCitaPermitida = (origen, destino) =>
  client
    .get(`${RUTAS.estadosCita}/validar-transicion`, { params: { origen, destino } })
    .then((r) => r.data.permitida)
