import { Spinner } from 'reactstrap'

export default function Loading({ texto = 'Cargando...', pantallaCompleta = false }) {
  const contenedor = pantallaCompleta ? 'pantalla-centrada' : 'text-center py-5'
  return (
    <div className={contenedor} role="status" aria-live="polite">
      <div className="d-flex align-items-center justify-content-center text-muted">
        <Spinner size="sm" color="primary" className="mr-2">{''}</Spinner>
        <span className="small">{texto}</span>
      </div>
    </div>
  )
}
