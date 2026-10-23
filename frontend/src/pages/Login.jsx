import { useState } from 'react'
import { Button } from 'reactstrap'
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
      // 401 = credenciales incorrectas o usuario inactivo.
      setErrorGeneral(status === 401 ? 'Usuario o contraseña incorrectos.' : message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form onSubmit={enviar} noValidate>
      <div className="mb-3 text-center">
        <h4 className="mb-1">Iniciar sesión</h4>
        <p className="text-muted mb-0">Ingrese sus credenciales para continuar.</p>
      </div>

      {sesionExpirada && !errorGeneral && (
        <ErrorAlert tipo="aviso" mensaje="Su sesión expiró. Inicie sesión nuevamente." />
      )}
      <ErrorAlert mensaje={errorGeneral} />

      <FormField
        className="col-12"
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
        className="col-12"
        nombre="password"
        etiqueta="Contraseña"
        tipo="password"
        valor={password}
        onChange={(e) => setPassword(e.target.value)}
        error={errores.password}
        autoComplete="current-password"
        requerido
      />

      <Button type="submit" color="primary" className="btn-round" block disabled={enviando}>
        {enviando ? 'Ingresando...' : 'Ingresar'}
      </Button>
    </form>
  )
}
