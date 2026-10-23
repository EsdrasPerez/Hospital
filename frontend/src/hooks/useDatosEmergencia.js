import { listarHabitaciones, listarMedicos, listarNivelesUrgencia } from '../api/catalogos.js'
import { listarPacientes } from '../api/pacientes.js'
import { listarTriajes } from '../api/triaje.js'
import { mapaPor, useApi } from './useApi.js'

const tolerante = (promesa) => promesa.catch(() => [])

/**
 * Pacientes, médicos, niveles de triaje y habitaciones para resolver nombres y llenar selects.
 * Si el rol no puede leer un catálogo (p. ej. FARMACEUTICO/CAJERO) esa lista queda vacía y las
 * pantallas muestran "#id".
 */
export function useDatosEmergencia() {
  const { datos, cargando } = useApi('datos-emergencia', () =>
    Promise.all([
      tolerante(listarPacientes({ todos: true })),
      tolerante(listarMedicos()),
      tolerante(listarTriajes()),
      tolerante(listarHabitaciones()),
      tolerante(listarNivelesUrgencia()),
    ]),
  )
  const [pacientes, medicos, triajes, habitaciones, niveles] = datos ?? [[], [], [], [], []]
  return {
    cargando,
    pacientes,
    medicos,
    triajes,
    habitaciones,
    niveles,
    pacientesPorId: mapaPor(pacientes, 'idPaciente'),
    medicosPorId: mapaPor(medicos, 'idMedico'),
    triajesPorId: mapaPor(triajes, 'idTriaje'),
    habitacionesPorId: mapaPor(habitaciones, 'idHabitacion'),
  }
}
