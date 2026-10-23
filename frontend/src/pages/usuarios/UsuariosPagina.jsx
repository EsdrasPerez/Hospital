import { useMemo, useState } from 'react'
import { Alert, Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap'
import { actualizarUsuario, listarRoles, listarUsuarios, registrarUsuario } from '../../api/administracion.js'
import { useAuth } from '../../auth/useAuth.js'
import Badge, { BadgeActivo } from '../../components/Badge.jsx'
import ConfirmDialog from '../../components/ConfirmDialog.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Pagination from '../../components/Pagination.jsx'
import Pestanas from '../../components/Pestanas.jsx'
import Table from '../../components/Table.jsx'
import { mapaPor, useApi } from '../../hooks/useApi.js'
import { normalizarError } from '../../utils/errores.js'
import { ETIQUETAS_ROL, formatearFechaHora } from '../../utils/formato.js'

const TAMANO_PAGINA = 10
// Política del backend: contraseña de al menos 8 caracteres
const LARGO_MINIMO_CLAVE = 8

/** Error del backend: `detalles` por campo (si los hay) o `message`. */
function errorDe(e) {
  const { message, detalles } = normalizarError(e)
  return { detalles: detalles ?? {}, mensaje: Object.keys(detalles ?? {}).length ? 'Revise los campos marcados.' : message }
}

function DialogoBase({ titulo, children, error, guardando, textoGuardar, onCerrar, onGuardar }) {
  return (
    <Modal isOpen toggle={guardando ? undefined : onCerrar} centered fade={false}>
      <form
        onSubmit={(e) => {
          e.preventDefault()
          onGuardar()
        }}
        noValidate
      >
        <ModalHeader closeAriaLabel="Cerrar" toggle={guardando ? undefined : onCerrar}>
          {titulo}
        </ModalHeader>
        <ModalBody>
          <div className="row">{children}</div>
          {error && (
            <Alert color="danger" fade={false} role="alert" className="mt-3 mb-0">
              {error}
            </Alert>
          )}
        </ModalBody>
        <ModalFooter>
          <Button type="button" color="secondary" outline onClick={onCerrar} disabled={guardando}>
            Cancelar
          </Button>
          <Button type="submit" color="primary" disabled={guardando}>
            {guardando ? 'Guardando...' : textoGuardar}
          </Button>
        </ModalFooter>
      </form>
    </Modal>
  )
}

/** Alta de usuario con POST /api/auth/register: usuario, contraseña y rol (por nombre) son obligatorios. */
function CrearUsuarioDialog({ roles, onCerrar, onListo }) {
  const [form, setForm] = useState({ username: '', password: '', confirmar: '', rol: '' })
  const [errores, setErrores] = useState({})
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  const cambiar = (e) => {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const guardar = async () => {
    const e = {}
    if (!form.username.trim()) e.username = 'El usuario es obligatorio.'
    else if (form.username.trim().length > 50) e.username = 'El usuario no puede superar los 50 caracteres.'
    if (!form.password) e.password = 'La contraseña es obligatoria.'
    else if (form.password.length < LARGO_MINIMO_CLAVE) e.password = `La contraseña debe tener al menos ${LARGO_MINIMO_CLAVE} caracteres.`
    if (form.password && form.confirmar !== form.password) e.confirmar = 'Las contraseñas no coinciden.'
    if (!form.rol) e.rol = 'El rol es obligatorio.'
    setErrores(e)
    if (Object.keys(e).length) return
    setGuardando(true)
    setError('')
    try {
      await registrarUsuario(form.username.trim(), form.password, form.rol)
      onListo(`Usuario "${form.username.trim()}" creado.`)
    } catch (err) {
      // 400: `detalles` (username, password, rol); 409: el usuario ya existe; 422: el rol no existe o está inactivo
      const { detalles, mensaje } = errorDe(err)
      setErrores(detalles)
      setError(mensaje)
      setGuardando(false)
    }
  }

  return (
    <DialogoBase titulo="Nuevo usuario" error={error} guardando={guardando} textoGuardar="Crear usuario" onCerrar={onCerrar} onGuardar={guardar}>
      <FormField className="col-12" nombre="username" etiqueta="Usuario" valor={form.username} onChange={cambiar} error={errores.username} requerido maxLength={50} autoComplete="off" />
      <FormField className="col-12" nombre="rol" etiqueta="Rol" tipo="select" opciones={roles.filter((r) => r.activo !== false).map((r) => ({ valor: r.nombre, etiqueta: ETIQUETAS_ROL[r.nombre] ?? r.nombre }))} valor={form.rol} onChange={cambiar} error={errores.rol} requerido />
      <FormField className="col-12" nombre="password" etiqueta="Contraseña" tipo="password" valor={form.password} onChange={cambiar} error={errores.password} requerido autoComplete="new-password" ayuda="Mínimo 8 caracteres. Entréguela al usuario por un canal seguro; el sistema la guarda cifrada." />
      <FormField className="col-12" nombre="confirmar" etiqueta="Confirmar contraseña" tipo="password" valor={form.confirmar} onChange={cambiar} error={errores.confirmar} requerido autoComplete="new-password" />
    </DialogoBase>
  )
}

/** Edición de nombre de usuario y rol con PUT /api/usuarios/{id} (solo se envían estos campos). */
function EditarUsuarioDialog({ usuario, roles, onCerrar, onListo }) {
  const [form, setForm] = useState({ nombreUsuario: usuario.nombreUsuario, idRol: String(usuario.idRol ?? '') })
  const [errores, setErrores] = useState({})
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  const cambiar = (e) => {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const guardar = async () => {
    const e = {}
    if (!form.nombreUsuario.trim()) e.nombreUsuario = 'El usuario es obligatorio.'
    else if (form.nombreUsuario.trim().length > 50) e.nombreUsuario = 'El usuario no puede superar los 50 caracteres.'
    if (!form.idRol) e.idRol = 'El rol es obligatorio.'
    setErrores(e)
    if (Object.keys(e).length) return
    setGuardando(true)
    setError('')
    try {
      await actualizarUsuario(usuario.idUsuario, { nombreUsuario: form.nombreUsuario.trim(), idRol: Number(form.idRol) })
      onListo('Usuario actualizado.')
    } catch (err) {
      // 400: `detalles`; 409: nombre repetido; 422: rol inexistente/inactivo, no quitarse el rol ADMIN ni degradar al último ADMIN
      const { detalles, mensaje } = errorDe(err)
      setErrores(detalles)
      setError(mensaje)
      setGuardando(false)
    }
  }

  return (
    <DialogoBase titulo="Editar usuario" error={error} guardando={guardando} textoGuardar="Guardar cambios" onCerrar={onCerrar} onGuardar={guardar}>
      <FormField className="col-12" nombre="nombreUsuario" etiqueta="Usuario" valor={form.nombreUsuario} onChange={cambiar} error={errores.nombreUsuario} requerido maxLength={50} />
      <FormField className="col-12" nombre="idRol" etiqueta="Rol" tipo="select" opciones={roles.filter((r) => r.activo !== false || String(r.idRol) === form.idRol).map((r) => ({ valor: String(r.idRol), etiqueta: ETIQUETAS_ROL[r.nombre] ?? r.nombre }))} valor={form.idRol} onChange={cambiar} error={errores.idRol} requerido ayuda="El cambio de rol aplica en el siguiente inicio de sesión del usuario." />
    </DialogoBase>
  )
}

/** Cambio de contraseña (PUT /api/usuarios/{id} con `password`): el backend la guarda cifrada. */
function ContrasenaDialog({ usuario, onCerrar, onListo }) {
  const [form, setForm] = useState({ password: '', confirmar: '' })
  const [errores, setErrores] = useState({})
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  const cambiar = (e) => {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const guardar = async () => {
    const e = {}
    if (!form.password) e.password = 'La contraseña es obligatoria.'
    else if (form.password.length < LARGO_MINIMO_CLAVE) e.password = `La contraseña debe tener al menos ${LARGO_MINIMO_CLAVE} caracteres.`
    if (form.password && form.confirmar !== form.password) e.confirmar = 'Las contraseñas no coinciden.'
    setErrores(e)
    if (Object.keys(e).length) return
    setGuardando(true)
    setError('')
    try {
      await actualizarUsuario(usuario.idUsuario, { password: form.password })
      onListo(`Contraseña de "${usuario.nombreUsuario}" actualizada.`)
    } catch (err) {
      const { detalles, mensaje } = errorDe(err)
      setErrores(detalles)
      setError(mensaje)
      setGuardando(false)
    }
  }

  return (
    <DialogoBase titulo={`Cambiar contraseña de ${usuario.nombreUsuario}`} error={error} guardando={guardando} textoGuardar="Cambiar contraseña" onCerrar={onCerrar} onGuardar={guardar}>
      <FormField className="col-12" nombre="password" etiqueta="Nueva contraseña" tipo="password" valor={form.password} onChange={cambiar} error={errores.password} requerido autoComplete="new-password" />
      <FormField className="col-12" nombre="confirmar" etiqueta="Confirmar contraseña" tipo="password" valor={form.confirmar} onChange={cambiar} error={errores.confirmar} requerido autoComplete="new-password" />
    </DialogoBase>
  )
}

function UsuariosTab() {
  const { usuario: yo } = useAuth()
  const [texto, setTexto] = useState('')
  const [incluirInactivos, setIncluirInactivos] = useState(true)
  const [pagina, setPagina] = useState(1)
  const [exito, setExito] = useState('')
  const [dialogo, setDialogo] = useState(null) // { tipo: 'crear'|'editar'|'clave', usuario }
  const [cambioEstado, setCambioEstado] = useState(null)
  const [procesando, setProcesando] = useState(false)
  const [errorEstado, setErrorEstado] = useState('')

  const { datos, error, cargando, recargar } = useApi('usuarios', async () => {
    const [usuarios, roles] = await Promise.all([listarUsuarios(), listarRoles().catch(() => [])])
    return { usuarios, roles, rolesPorId: mapaPor(roles, 'idRol') }
  })

  const nombreRol = (idRol) => {
    const r = datos?.rolesPorId[idRol]
    return r ? (ETIQUETAS_ROL[r.nombre] ?? r.nombre) : `Rol #${idRol}`
  }

  const filas = useMemo(() => {
    const busqueda = texto.trim().toLowerCase()
    return (datos?.usuarios ?? [])
      .filter((u) => incluirInactivos || u.estado !== false)
      .filter((u) => !busqueda || u.nombreUsuario.toLowerCase().includes(busqueda))
      .sort((a, b) => a.nombreUsuario.localeCompare(b.nombreUsuario, 'es'))
  }, [datos, texto, incluirInactivos])

  const esYo = (u) => u.nombreUsuario === yo?.username

  const columnas = [
    {
      clave: 'usuario',
      titulo: 'Usuario',
      render: (u) => (
        <>
          <strong>{u.nombreUsuario}</strong>
          {esYo(u) && <span className="ml-2"><Badge tono="azul">Usted</Badge></span>}
        </>
      ),
    },
    { clave: 'rol', titulo: 'Rol', render: (u) => nombreRol(u.idRol) },
    { clave: 'acceso', titulo: 'Último acceso', render: (u) => formatearFechaHora(u.ultimoAcceso) },
    { clave: 'estado', titulo: 'Estado', render: (u) => <BadgeActivo activo={u.estado} /> },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      className: 'text-right',
      render: (u) => (
        <div className="tabla-acciones d-flex flex-wrap justify-content-end">
          {!esYo(u) && (
            <button type="button" className="enlace-boton text-primary" onClick={() => setDialogo({ tipo: 'editar', usuario: u })}>
              Editar
            </button>
          )}
          <button type="button" className="enlace-boton text-primary" onClick={() => setDialogo({ tipo: 'clave', usuario: u })}>
            Contraseña
          </button>
          {!esYo(u) && (
            <button
              type="button"
              className={`enlace-boton ${u.estado === false ? 'text-success' : 'text-danger'}`}
              onClick={() => {
                setErrorEstado('')
                setCambioEstado(u)
              }}
            >
              {u.estado === false ? 'Activar' : 'Desactivar'}
            </button>
          )}
        </div>
      ),
    },
  ]

  const confirmarEstado = async () => {
    setProcesando(true)
    setErrorEstado('')
    try {
      await actualizarUsuario(cambioEstado.idUsuario, { estado: cambioEstado.estado === false })
      setExito(cambioEstado.estado === false ? 'Usuario activado.' : 'Usuario desactivado: ya no puede iniciar sesión.')
      setCambioEstado(null)
      recargar()
    } catch (e) {
      // 422: no puede desactivarse a sí mismo ni al último ADMIN activo
      setErrorEstado(normalizarError(e).message)
    } finally {
      setProcesando(false)
    }
  }

  const alListo = (mensaje) => {
    setDialogo(null)
    setExito(mensaje)
    recargar()
  }

  return (
    <div>
      <div className="d-flex flex-column flex-md-row align-items-md-center justify-content-md-between mb-3">
        <div>
          <h2 className="h4 mb-1">Usuarios del sistema</h2>
          <p className="text-muted small mb-0">Un usuario desactivado no puede iniciar sesión. Por seguridad no puede desactivar ni cambiar el rol de su propia cuenta.</p>
        </div>
        <Button color="primary" className="btn-round mt-2 mt-md-0" onClick={() => setDialogo({ tipo: 'crear' })}>
          Nuevo usuario
        </Button>
      </div>
      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />
      <ErrorAlert mensaje={error?.message} />
      <Table
        columnas={columnas}
        filas={filas.slice((pagina - 1) * TAMANO_PAGINA, pagina * TAMANO_PAGINA)}
        claveFila={(u) => u.idUsuario}
        cargando={cargando}
        busqueda={texto}
        onBuscar={(v) => {
          setTexto(v)
          setPagina(1)
        }}
        placeholderBusqueda="Buscar por usuario"
        acciones={
          <label className="d-flex align-items-center small mb-0">
            <input type="checkbox" checked={incluirInactivos} onChange={(e) => { setIncluirInactivos(e.target.checked); setPagina(1) }} className="mr-2" />
            Mostrar inactivos
          </label>
        }
        vacio={<EmptyState titulo="No hay usuarios" descripcion="Ajuste la búsqueda o cree un usuario." />}
        pie={<Pagination pagina={pagina} totalElementos={filas.length} tamano={TAMANO_PAGINA} onCambiarPagina={setPagina} />}
      />

      {dialogo?.tipo === 'crear' && <CrearUsuarioDialog roles={datos?.roles ?? []} onCerrar={() => setDialogo(null)} onListo={alListo} />}
      {dialogo?.tipo === 'editar' && <EditarUsuarioDialog usuario={dialogo.usuario} roles={datos?.roles ?? []} onCerrar={() => setDialogo(null)} onListo={alListo} />}
      {dialogo?.tipo === 'clave' && <ContrasenaDialog usuario={dialogo.usuario} onCerrar={() => setDialogo(null)} onListo={alListo} />}

      <ConfirmDialog
        abierto={Boolean(cambioEstado)}
        titulo={cambioEstado?.estado === false ? 'Activar usuario' : 'Desactivar usuario'}
        mensaje={
          cambioEstado?.estado === false
            ? `¿Activar a "${cambioEstado?.nombreUsuario}"? Podrá iniciar sesión de nuevo.`
            : `¿Desactivar a "${cambioEstado?.nombreUsuario}"? Ya no podrá iniciar sesión (su sesión deja de ser válida de inmediato).`
        }
        textoConfirmar={cambioEstado?.estado === false ? 'Activar' : 'Desactivar'}
        peligro={cambioEstado?.estado !== false}
        cargando={procesando}
        error={errorEstado}
        onConfirmar={confirmarEstado}
        onCancelar={() => setCambioEstado(null)}
      />
    </div>
  )
}

function RolesTab() {
  const { datos, error, cargando } = useApi('roles', () => listarRoles())
  const columnas = [
    { clave: 'nombre', titulo: 'Rol', render: (r) => <strong>{ETIQUETAS_ROL[r.nombre] ?? r.nombre}</strong> },
    { clave: 'codigo', titulo: 'Código', render: (r) => r.nombre },
    { clave: 'descripcion', titulo: 'Descripción', render: (r) => r.descripcion || '—' },
    { clave: 'estado', titulo: 'Estado', render: (r) => <BadgeActivo activo={r.activo} /> },
  ]
  return (
    <div>
      <h2 className="h4 mb-1">Roles</h2>
      <p className="text-muted small">
        Solo lectura. Los roles son fijos del sistema: sus nombres controlan los permisos de cada pantalla y del backend, por lo que no se crean, renombran ni
        eliminan desde aquí.
      </p>
      <ErrorAlert mensaje={error?.message} />
      <Table columnas={columnas} filas={datos ?? []} claveFila={(r) => r.idRol} cargando={cargando} />
    </div>
  )
}

export default function UsuariosPagina() {
  return (
    <div>
      <div className="card card-body">
        <h1 className="h3 mb-0">Usuarios y roles</h1>
        <p className="text-muted mb-0">Cuentas de acceso al sistema.</p>
      </div>
      <div className="card card-body pestanas-contenedor">
        <Pestanas
          etiqueta="Usuarios y roles"
          pestanas={[
            { clave: 'usuarios', titulo: 'Usuarios', contenido: <UsuariosTab /> },
            { clave: 'roles', titulo: 'Roles', contenido: <RolesTab /> },
          ]}
        />
      </div>
    </div>
  )
}
