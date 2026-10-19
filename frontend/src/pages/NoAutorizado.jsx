import { Link } from 'react-router-dom'

export default function NoAutorizado() {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <p className="text-5xl font-bold text-slate-300">403</p>
      <h1 className="mt-3 text-xl font-semibold text-slate-900">Acceso no autorizado</h1>
      <p className="mt-1 max-w-md text-sm text-slate-500">
        Su perfil no tiene permisos para acceder a esta sección.
      </p>
      <Link to="/" className="mt-6 rounded-lg bg-teal-700 px-4 py-2 text-sm font-medium text-white hover:bg-teal-800">
        Volver al inicio
      </Link>
    </div>
  )
}
