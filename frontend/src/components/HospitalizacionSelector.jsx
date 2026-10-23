import { listarResumenHospitalizacionesDePaciente } from '../api/hospitalizaciones.js'
import { useApi } from '../hooks/useApi.js'
import { formatearFechaHora } from '../utils/formato.js'
import FormField from './FormField.jsx'

/**
 * Selector (opcional) de hospitalización del paciente elegido, con GET /api/hospitalizaciones/paciente/{id}/resumen:
 * muestra N.º, fecha de ingreso, alta o "en curso" y habitación (sin motivo ni diagnóstico).
 * `onChange(idHospitalizacion)` recibe '' cuando no se elige ninguna.
 */
export default function HospitalizacionSelector({ idPaciente, valor, onChange, error, disabled = false, nombre = 'idHospitalizacion', etiqueta = 'Hospitalización (opcional)', ayuda }) {
  const idValido = /^\d+$/.test(String(idPaciente ?? '')) ? String(idPaciente) : ''
  const { datos, cargando, error: errorCarga } = useApi(`hosp-resumen|${idValido}`, () => (idValido ? listarResumenHospitalizacionesDePaciente(idValido) : []))

  const opciones = (datos ?? []).map((h) => ({
    valor: String(h.idHospitalizacion),
    etiqueta: `#${h.idHospitalizacion} · ingreso ${formatearFechaHora(h.fechaIngreso)} · ${h.fechaAlta ? `alta ${formatearFechaHora(h.fechaAlta)}` : 'en curso'}${h.numeroHabitacion ? ` · Hab. ${h.numeroHabitacion}` : ''}`,
  }))
  // Si el valor actual no está en la lista (p. ej. se está cargando) se conserva como opción para no perderlo
  if (valor && !opciones.some((o) => o.valor === String(valor))) opciones.push({ valor: String(valor), etiqueta: `#${valor}` })

  return (
    <FormField
      nombre={nombre}
      etiqueta={etiqueta}
      tipo="select"
      placeholder="Sin hospitalización"
      opciones={opciones}
      valor={valor}
      onChange={(e) => onChange(e.target.value)}
      error={error}
      disabled={disabled || !idValido || cargando}
      ayuda={
        ayuda ??
        (!idValido
          ? 'Elija primero el paciente.'
          : errorCarga
            ? 'No se pudieron cargar las hospitalizaciones del paciente.'
            : 'Solo se listan las del paciente elegido.')
      }
    />
  )
}
