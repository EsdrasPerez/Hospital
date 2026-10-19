import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth.js'
import ErrorAlert from '../components/ErrorAlert.jsx'
import FormField from '../components/FormField.jsx'
import { normalizarError } from '../utils/errores.js'

export default function Login() {
  const { usuario, cargando, login, sesionExpirada } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [enviando, setEnviando] = useState(false)

  if (!cargando && usuario) return <Navigate to={location.state?.desde ?? '/'} replace />

  const enviar = async (e) => {
    e.preventDefault()
    const nuevos = {}
    if (!username.trim()) nuevos.username = 'El usuario es obligatorio'
    if (!password) nuevos.password = 'La contraseña es obligatoria'
    setErrores(nuevos)
    setErrorGeneral('')
    if (Object.keys(nuevos).length) return

    setEnviando(true)
    try {
      await login(username.trim(), password)
      navigate(location.state?.desde ?? '/', { replace: true })
    } catch (error) {
      const { status, message, detalles } = normalizarError(error)
      setErrores(detalles)
      // El backend responde 401 con texto plano cuando las credenciales son incorrectas.
      setErrorGeneral(status === 401 ? 'Usuario o contraseña incorrectos.' : message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form onSubmit={enviar} noValidate className="space-y-4">
      <div>
        <h1 className="text-lg font-semibold text-slate-900">Iniciar sesión</h1>
        <p className="text-sm text-slate-500">Ingrese sus credenciales para continuar.</p>
      </div>

      {sesionExpirada && !errorGeneral && (
        <ErrorAlert tipo="aviso" mensaje="Su sesión expiró. Inicie sesión nuevamente." />
      )}
      <ErrorAlert mensaje={errorGeneral} />

      <FormField
        nombre="username"
        etiqueta="Usuario"
        valor={username}
        onChange={(e) => setUsername(e.target.value)}
        error={errores.username}
        autoComplete="username"
        autoFocus
        requerido
      />
      <FormField
        nombre="password"
        etiqueta="Contraseña"
        tipo="password"
        valor={password}
        onChange={(e) => setPassword(e.target.value)}
        error={errores.password}
        autoComplete="current-password"
        requerido
      />

      <button
        type="submit"
        disabled={enviando}
        className="w-full rounded-lg bg-teal-700 px-4 py-2.5 text-sm font-semibold text-white hover:bg-teal-800 disabled:opacity-60"
      >
        {enviando ? 'Ingresando...' : 'Ingresar'}
      </button>
    </form>
  )
}
