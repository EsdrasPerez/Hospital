import EmptyState from './EmptyState.jsx'
import Loading from './Loading.jsx'

/**
 * Tabla genérica con barra de búsqueda opcional.
 * - columnas: [{ clave, titulo, render?(fila), className? }]
 * - Si se pasa `onBuscar`, muestra el cuadro de búsqueda (controlado con `busqueda`).
 * - `acciones` es un nodo para la derecha de la barra (filtros, botones).
 * - `pie` se renderiza bajo la tabla (p. ej. <Pagination />).
 */
export default function Table({
  columnas,
  filas,
  claveFila = (f) => f.id,
  cargando = false,
  busqueda,
  onBuscar,
  placeholderBusqueda = 'Buscar...',
  acciones,
  vacio,
  alClickFila,
  pie,
}) {
  return (
    <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
      {(onBuscar || acciones) && (
        <div className="flex flex-col gap-3 border-b border-slate-200 p-4 sm:flex-row sm:items-center sm:justify-between">
          {onBuscar ? (
            <div className="relative w-full sm:max-w-sm">
              <svg
                viewBox="0 0 24 24"
                className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="11" cy="11" r="7" />
                <path d="m20 20-3.5-3.5" />
              </svg>
              <input
                type="search"
                value={busqueda ?? ''}
                onChange={(e) => onBuscar(e.target.value)}
                placeholder={placeholderBusqueda}
                aria-label={placeholderBusqueda}
                className="block w-full rounded-lg border border-slate-300 bg-white py-2 pl-9 pr-3 text-sm placeholder:text-slate-400 focus:border-teal-600 focus:outline-none focus:ring-2 focus:ring-teal-600/30"
              />
            </div>
          ) : (
            <span />
          )}
          {acciones && <div className="flex items-center gap-3">{acciones}</div>}
        </div>
      )}

      {cargando ? (
        <Loading />
      ) : filas.length === 0 ? (
        vacio ?? <EmptyState />
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50">
              <tr>
                {columnas.map((c) => (
                  <th
                    key={c.clave}
                    scope="col"
                    className={`px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 ${c.className ?? ''}`}
                  >
                    {c.titulo}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filas.map((fila) => (
                <tr
                  key={claveFila(fila)}
                  onClick={alClickFila ? () => alClickFila(fila) : undefined}
                  className={alClickFila ? 'cursor-pointer hover:bg-slate-50' : ''}
                >
                  {columnas.map((c) => (
                    <td key={c.clave} className={`px-4 py-3 text-slate-700 ${c.className ?? ''}`}>
                      {c.render ? c.render(fila) : (fila[c.clave] ?? '—')}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {pie}
    </div>
  )
}
