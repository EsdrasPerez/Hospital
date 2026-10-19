import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/useAuth.js'
import { menuParaRol } from '../config/menu.js'
import { ETIQUETAS_ROL } from '../utils/formato.js'

const claseEnlace = ({ isActive }) =>
  `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-teal-700 text-white' : 'text-slate-300 hover:bg-slate-800 hover:text-white'
  }`

export default function MainLayout() {
  const { usuario, rol, logout } = useAuth()
  const [menuAbierto, setMenuAbierto] = useState(false)
  const grupos = menuParaRol(rol)

  const sidebar = (
    <nav
      className="flex h-full flex-col bg-slate-900 text-slate-200"
      aria-label="Menú principal"
      onClick={() => setMenuAbierto(false)} // cierra el sidebar (tablet) al elegir una opción
    >
      <div className="flex h-16 shrink-0 items-center gap-3 px-5">
        <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-teal-700 text-white">
          <svg viewBox="0 0 24 24" className="h-5 w-5" fill="currentColor" aria-hidden="true">
            <path d="M10 3h4v7h7v4h-7v7h-4v-7H3v-4h7z" />
          </svg>
        </span>
        <span className="text-lg font-semibold text-white">Hospital</span>
      </div>
      <div className="flex-1 space-y-6 overflow-y-auto px-3 py-4">
        <NavLink to="/" end className={claseEnlace}>
          Inicio
        </NavLink>
        {grupos.map((grupo) => (
          <div key={grupo.titulo}>
            <p className="mb-2 px-3 text-xs font-semibold uppercase tracking-wider text-slate-500">{grupo.titulo}</p>
            <div className="space-y-1">
              {grupo.items.map((m) => (
                <NavLink key={m.clave} to={m.ruta} className={claseEnlace}>
                  {m.titulo}
                </NavLink>
              ))}
            </div>
          </div>
        ))}
      </div>
    </nav>
  )

  return (
    <div className="min-h-screen lg:flex">
      {/* Sidebar fijo en pantallas grandes */}
      <aside className="hidden w-64 shrink-0 lg:block">
        <div className="fixed inset-y-0 w-64">{sidebar}</div>
      </aside>

      {/* Sidebar colapsable (tablet / móvil) */}
      {menuAbierto && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div className="absolute inset-0 bg-slate-900/50" onClick={() => setMenuAbierto(false)} />
          <aside className="relative h-full w-64 shadow-xl">{sidebar}</aside>
        </div>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 sm:px-6">
          <button
            type="button"
            onClick={() => setMenuAbierto(true)}
            className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
            aria-label="Abrir menú"
          >
            <svg viewBox="0 0 24 24" className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <div className="ml-auto flex items-center gap-4">
            <div className="text-right leading-tight">
              <p className="text-sm font-semibold text-slate-800">{usuario?.username}</p>
              <p className="text-xs text-slate-500">{ETIQUETAS_ROL[rol] ?? rol}</p>
            </div>
            <button
              type="button"
              onClick={logout}
              className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Cerrar sesión
            </button>
          </div>
        </header>
        <main className="flex-1 p-4 sm:p-6">
          <div className="mx-auto max-w-6xl">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}
