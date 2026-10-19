import { Navigate, Outlet, useLocation } from 'react-router-dom'
import Loading from '../components/Loading.jsx'
import { useAuth } from './useAuth.js'

/** Exige sesión iniciada; si no, redirige a /login. */
export default function ProtectedRoute() {
  const { usuario, cargando, cerroSesion } = useAuth()
  const location = useLocation()

  if (cargando) return <Loading pantallaCompleta texto="Verificando sesión..." />
  if (!usuario) {
    return <Navigate to="/login" replace state={cerroSesion ? null : { desde: location.pathname }} />
  }
  return <Outlet />
}
