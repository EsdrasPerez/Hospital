// Montos en quetzales con 2 decimales. Para no arrastrar errores de punto flotante, las sumas y
// comparaciones se hacen en CENTAVOS enteros; el texto del usuario se valida con una expresión regular.

const MONTO = /^\d+(\.\d{1,2})?$/

/** Número o texto con a lo sumo 2 decimales -> centavos enteros (null si no es un monto válido). */
export function aCentavos(valor) {
  if (valor === null || valor === undefined || valor === '') return null
  const texto = typeof valor === 'number' ? valor.toFixed(2) : String(valor).trim().replace(',', '.')
  const negativo = texto.startsWith('-')
  const sinSigno = negativo ? texto.slice(1) : texto
  if (!MONTO.test(sinSigno)) return null
  const [enteros, decimales = ''] = sinSigno.split('.')
  const centavos = Number(enteros) * 100 + Number(decimales.padEnd(2, '0'))
  return negativo ? -centavos : centavos
}

/** Centavos enteros -> número con 2 decimales para enviar al backend. */
export const deCentavos = (centavos) => centavos / 100

/** Centavos -> texto 'ddd.cc' (valor de un <input>). */
export function centavosATexto(centavos) {
  const abs = Math.abs(centavos)
  return `${centavos < 0 ? '-' : ''}${Math.floor(abs / 100)}.${String(abs % 100).padStart(2, '0')}`
}

/** Valida un monto escrito por el usuario. Devuelve el mensaje de error o ''. */
export function errorMonto(texto, { etiqueta = 'El monto', permitirCero = false } = {}) {
  if (texto === '' || texto === null || texto === undefined) return `${etiqueta} es obligatorio.`
  const c = aCentavos(texto)
  if (c === null || c < 0) return `${etiqueta} debe ser un número positivo con hasta 2 decimales.`
  if (c === 0 && !permitirCero) return `${etiqueta} debe ser mayor a cero.`
  if (c > 9999999999) return `${etiqueta} es demasiado grande.`
  return ''
}
