/** Muestra el `message` de un error del backend (o cualquier texto). */
export default function ErrorAlert({ mensaje, titulo, onCerrar, tipo = 'error' }) {
  if (!mensaje) return null
  const estilos =
    tipo === 'exito'
      ? 'border-emerald-200 bg-emerald-50 text-emerald-800'
      : tipo === 'aviso'
        ? 'border-amber-200 bg-amber-50 text-amber-800'
        : 'border-red-200 bg-red-50 text-red-800'
  return (
    <div
      role={tipo === 'error' ? 'alert' : 'status'}
      className={`flex items-start justify-between gap-3 rounded-lg border px-4 py-3 text-sm ${estilos}`}
    >
      <div>
        {titulo && <p className="font-semibold">{titulo}</p>}
        <p>{mensaje}</p>
      </div>
      {onCerrar && (
        <button
          type="button"
          onClick={onCerrar}
          className="shrink-0 rounded px-1 text-lg leading-none opacity-70 hover:opacity-100"
          aria-label="Cerrar mensaje"
        >
          &times;
        </button>
      )}
    </div>
  )
}
