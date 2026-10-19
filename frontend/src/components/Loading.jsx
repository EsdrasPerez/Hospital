export default function Loading({ texto = 'Cargando...', pantallaCompleta = false }) {
  const contenedor = pantallaCompleta
    ? 'flex min-h-screen items-center justify-center'
    : 'flex items-center justify-center py-12'
  return (
    <div className={contenedor} role="status" aria-live="polite">
      <div className="flex items-center gap-3 text-slate-600">
        <span
          className="h-5 w-5 animate-spin rounded-full border-2 border-slate-300 border-t-teal-700"
          aria-hidden="true"
        />
        <span className="text-sm">{texto}</span>
      </div>
    </div>
  )
}
