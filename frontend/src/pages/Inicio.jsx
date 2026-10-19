import { Link } from 'react-router-dom'
import { useAuth } from '../auth/useAuth.js'
import { menuParaRol } from '../config/menu.js'
import { ETIQUETAS_ROL } from '../utils/formato.js'

export default function Inicio() {
  const { usuario, rol } = useAuth()
  const modulos = menuParaRol(rol).flatMap((g) => g.items)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Bienvenido, {usuario?.username}</h1>
        <p className="text-sm text-slate-500">Perfil: {ETIQUETAS_ROL[rol] ?? rol}</p>
      </div>

      {modulos.length > 0 ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {modulos.map((m) => (
            <Link
              key={m.clave}
              to={m.ruta}
              className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-teal-600 hover:shadow"
            >
              <p className="text-xs font-semibold uppercase tracking-wide text-teal-700">{m.grupo}</p>
              <p className="mt-1 text-lg font-semibold text-slate-900">{m.titulo}</p>
              <p className="mt-1 text-sm text-slate-500">Abrir módulo</p>
            </Link>
          ))}
        </div>
      ) : (
        <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-600 shadow-sm">
          Aún no hay módulos disponibles para su perfil en esta versión. Se habilitarán en próximas fases.
        </div>
      )}
    </div>
  )
}
