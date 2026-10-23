import { useCallback, useEffect, useState } from 'react'
import { normalizarError } from '../utils/errores.js'

/**
 * Carga datos de la API. `clave` identifica la consulta (p. ej. incluye los filtros):
 * al cambiar, se vuelve a pedir y `cargando` es true hasta que llegue la respuesta.
 * `recargar()` fuerza una nueva petición.
 */
export function useApi(clave, funcion) {
  const [recarga, setRecarga] = useState(0)
  const claveCompleta = `${clave}#${recarga}`
  const [resultado, setResultado] = useState({ clave: null, datos: null, error: null })

  useEffect(() => {
    let cancelado = false
    Promise.resolve()
      .then(funcion)
      .then((datos) => {
        if (!cancelado) setResultado({ clave: claveCompleta, datos, error: null })
      })
      .catch((e) => {
        if (!cancelado) setResultado({ clave: claveCompleta, datos: null, error: normalizarError(e) })
      })
    return () => {
      cancelado = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [claveCompleta])

  const recargar = useCallback(() => setRecarga((n) => n + 1), [])
  const listo = resultado.clave === claveCompleta
  return {
    datos: listo ? resultado.datos : null,
    error: listo ? resultado.error : null,
    cargando: !listo,
    recargar,
  }
}

/** Convierte una lista en mapa por id. */
export const mapaPor = (lista, campoId) =>
  Object.fromEntries((lista ?? []).map((x) => [x[campoId], x]))
