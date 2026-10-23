export default function EmptyState({ titulo = 'Sin resultados', descripcion, accion }) {
  return (
    <div className="text-center py-5 px-3">
      <i className="now-ui-icons files_box text-muted" style={{ fontSize: '2.2rem' }} aria-hidden="true" />
      <h5 className="mt-2 mb-1">{titulo}</h5>
      {descripcion && <p className="text-muted mb-0">{descripcion}</p>}
      {accion && <div className="mt-3">{accion}</div>}
    </div>
  )
}
