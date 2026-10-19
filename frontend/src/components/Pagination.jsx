/**
 * Paginación en el cliente (el backend actual devuelve listas completas, sin paginar).
 * pagina es base 1.
 */
export default function Pagination({ pagina, totalElementos, tamano, onCambiarPagina }) {
  const totalPaginas = Math.max(1, Math.ceil(totalElementos / tamano))
  if (totalElementos <= tamano) return null

  const desde = (pagina - 1) * tamano + 1
  const hasta = Math.min(pagina * tamano, totalElementos)
  const boton =
    'rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40'

  return (
    <nav
      className="flex flex-col items-center justify-between gap-2 border-t border-slate-200 px-4 py-3 sm:flex-row"
      aria-label="Paginación"
    >
      <p className="text-sm text-slate-600">
        Mostrando {desde}–{hasta} de {totalElementos}
      </p>
      <div className="flex items-center gap-2">
        <button type="button" className={boton} disabled={pagina <= 1} onClick={() => onCambiarPagina(pagina - 1)}>
          Anterior
        </button>
        <span className="text-sm text-slate-600">
          Página {pagina} de {totalPaginas}
        </span>
        <button
          type="button"
          className={boton}
          disabled={pagina >= totalPaginas}
          onClick={() => onCambiarPagina(pagina + 1)}
        >
          Siguiente
        </button>
      </div>
    </nav>
  )
}
