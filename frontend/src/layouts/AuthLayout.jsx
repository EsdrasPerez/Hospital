import { Outlet } from 'react-router-dom'

export default function AuthLayout() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-slate-100 px-4 py-10">
      <div className="mb-6 flex items-center gap-3">
        <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-teal-700 text-white">
          <svg viewBox="0 0 24 24" className="h-6 w-6" fill="currentColor" aria-hidden="true">
            <path d="M10 3h4v7h7v4h-7v7h-4v-7H3v-4h7z" />
          </svg>
        </span>
        <div>
          <p className="text-xl font-semibold leading-tight text-slate-900">Hospital</p>
          <p className="text-xs text-slate-500">Sistema de gestión hospitalaria</p>
        </div>
      </div>
      <div className="w-full max-w-sm rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <Outlet />
      </div>
    </div>
  )
}
