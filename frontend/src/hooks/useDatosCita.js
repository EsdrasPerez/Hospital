import { listarEstadosCita, listarMedicos } from '../api/catalogos.js'
import { listarPacientes } from '../api/pacientes.js'
import { mapaPor, useApi } from './useApi.js'

const tolerante = (promesa) => promesa.catch(() => [])

/**
 * Pacientes, médicos y estados de cita para resolver nombres y llenar selects.
 * Si el rol no puede leer un catálogo (p. ej. FARMACEUTICO/CAJERO no leen pacientes ni
 * médicos) esa lista queda vacía y las pantallas muestran "#id" en su lugar.
 */
export function useDatosCita() {
  const { datos, cargando } = useApi('datos-cita', () =>
    Promise.all([
      tolerante(listarPacientes({ todos: true })),
      tolerante(listarMedicos()),
      tolerante(listarEstadosCita()),
    ]),
  )
  const [pacientes, medicos, estados] = datos ?? [[], [], []]
  return {
    cargando,
    pacientes,
    medicos,
    estados,
    pacientesPorId: mapaPor(pacientes, 'idPaciente'),
    medicosPorId: mapaPor(medicos, 'idMedico'),
    estadosPorId: mapaPor(estados, 'idEstado'),
  }
}
