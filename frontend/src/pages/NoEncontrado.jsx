import { Link } from 'react-router-dom'

export default function NoEncontrado() {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <p className="text-5xl font-bold text-slate-300">404</p>
      <h1 className="mt-3 text-xl font-semibold text-slate-900">Página no encontrada</h1>
      <p className="mt-1 max-w-md text-sm text-slate-500">La dirección que busca no existe o fue movida.</p>
      <Link to="/" className="mt-6 rounded-lg bg-teal-700 px-4 py-2 text-sm font-medium text-white hover:bg-teal-800">
        Volver al inicio
      </Link>
    </div>
  )
}
