import { Button } from 'reactstrap'

/**
 * Paginación en el cliente (el backend actual devuelve listas completas, sin paginar).
 * pagina es base 1.
 */
export default function Pagination({ pagina, totalElementos, tamano, onCambiarPagina }) {
  const totalPaginas = Math.max(1, Math.ceil(totalElementos / tamano))
  if (totalElementos <= tamano) return null

  const desde = (pagina - 1) * tamano + 1
  const hasta = Math.min(pagina * tamano, totalElementos)

  return (
    <nav className="d-flex flex-column flex-sm-row align-items-center justify-content-between border-top pt-3 mt-2" aria-label="Paginación">
      <p className="small text-muted mb-2 mb-sm-0">
        Mostrando {desde}–{hasta} de {totalElementos}
      </p>
      <div className="d-flex align-items-center acciones-grupo">
        <Button color="primary" outline size="sm" className="btn-round" disabled={pagina <= 1} onClick={() => onCambiarPagina(pagina - 1)}>
          Anterior
        </Button>
        <span className="small text-muted">
          Página {pagina} de {totalPaginas}
        </span>
        <Button color="primary" outline size="sm" className="btn-round" disabled={pagina >= totalPaginas} onClick={() => onCambiarPagina(pagina + 1)}>
          Siguiente
        </Button>
      </div>
    </nav>
  )
}
