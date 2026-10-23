import { useSearchParams } from 'react-router-dom'

/**
 * Pestañas accesibles (nav-tabs de Bootstrap 4). La pestaña activa vive en el parámetro `?tab=` de la URL, así que se puede
 * enlazar y sobrevive a recargar. `pestanas`: [{ clave, titulo, contenido }] (contenido es un nodo; solo se monta el de la activa).
 */
export default function Pestanas({ pestanas, etiqueta = 'Secciones' }) {
  const [params, setParams] = useSearchParams()
  const activa = pestanas.find((p) => p.clave === params.get('tab')) ?? pestanas[0]
  if (!activa) return null

  const elegir = (clave) => setParams({ tab: clave }, { replace: true })

  const alTeclear = (e, indice) => {
    const salto = e.key === 'ArrowRight' ? 1 : e.key === 'ArrowLeft' ? -1 : 0
    if (!salto) return
    e.preventDefault()
    const destino = pestanas[(indice + salto + pestanas.length) % pestanas.length]
    elegir(destino.clave)
    document.getElementById(`pestana-${destino.clave}`)?.focus()
  }

  return (
    <div>
      <ul className="nav nav-tabs pestanas-admin" role="tablist" aria-label={etiqueta}>
        {pestanas.map((p, i) => (
          <li className="nav-item" key={p.clave} role="presentation">
            <button
              type="button"
              id={`pestana-${p.clave}`}
              role="tab"
              aria-selected={p.clave === activa.clave}
              aria-controls={`panel-${p.clave}`}
              tabIndex={p.clave === activa.clave ? 0 : -1}
              className={`nav-link${p.clave === activa.clave ? ' active' : ''}`}
              onClick={() => elegir(p.clave)}
              onKeyDown={(e) => alTeclear(e, i)}
            >
              {p.titulo}
            </button>
          </li>
        ))}
      </ul>
      <div id={`panel-${activa.clave}`} role="tabpanel" aria-labelledby={`pestana-${activa.clave}`} className="pt-3" key={activa.clave}>
        {activa.contenido}
      </div>
    </div>
  )
}
