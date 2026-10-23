import { Alert } from 'reactstrap'

const COLORES = { error: 'danger', exito: 'success', aviso: 'warning' }

/** Muestra el `message` de un error del backend (o cualquier texto) como alerta de Now UI. */
export default function ErrorAlert({ mensaje, titulo, onCerrar, tipo = 'error' }) {
  if (!mensaje) return null
  return (
    <Alert color={COLORES[tipo] ?? 'danger'} fade={false} toggle={onCerrar} closeAriaLabel="Cerrar" role={tipo === 'error' ? 'alert' : 'status'} className="mb-3">
      {titulo && <strong className="d-block">{titulo}</strong>}
      {mensaje}
    </Alert>
  )
}
