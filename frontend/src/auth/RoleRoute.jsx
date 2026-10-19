import { Outlet } from 'react-router-dom'
import NoAutorizado from '../pages/NoAutorizado.jsx'
import { useAuth } from './useAuth.js'

/** Muestra la página 403 si el rol del usuario no está en `roles`. */
export default function RoleRoute({ roles }) {
  const { tieneRol } = useAuth()
  return tieneRol(roles) ? <Outlet /> : <NoAutorizado />
}
