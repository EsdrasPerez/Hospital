// Permisos y menú por rol, derivados de los @PreAuthorize de cada controller del backend
// (hospital/src/main/java/com/example/hospital/<paquete>/*Controller.java).
//
// IMPORTANTE: muchos endpoints GET no tienen @PreAuthorize, así que los puede consultar
// cualquier usuario autenticado (constante TODOS). La escritura suele ser más restrictiva.
// Cada módulo declara por operación: ver (listar/consultar), crear, editar, desactivar.
//
// `implementado: false` => el módulo existe en el backend pero su pantalla llega en una
// fase futura; no aparece en el menú todavía.

export const ADMIN = 'ADMIN'
export const MEDICO = 'MEDICO'
export const ENFERMERO = 'ENFERMERO'
export const RECEPCIONISTA = 'RECEPCIONISTA'
export const FARMACEUTICO = 'FARMACEUTICO'
export const CAJERO = 'CAJERO'

export const TODOS = [ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA, FARMACEUTICO, CAJERO]
const CLINICOS = [ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA]
const SOLO_ADMIN = [ADMIN]

export const MODULOS = [
  {
    clave: 'pacientes',
    titulo: 'Pacientes',
    ruta: '/pacientes',
    grupo: 'Atención',
    implementado: true,
    permisos: {
      ver: CLINICOS,
      crear: [ADMIN, RECEPCIONISTA],
      editar: [ADMIN, RECEPCIONISTA],
      desactivar: [ADMIN],
    },
  },
  {
    clave: 'citas',
    titulo: 'Citas',
    ruta: '/citas',
    grupo: 'Atención',
    implementado: false,
    permisos: {
      ver: TODOS,
      crear: [ADMIN, RECEPCIONISTA],
      editar: [ADMIN, RECEPCIONISTA],
      desactivar: SOLO_ADMIN,
      cambiarEstado: [ADMIN, RECEPCIONISTA, MEDICO],
    },
  },
  {
    clave: 'consultas',
    titulo: 'Consultas',
    ruta: '/consultas',
    grupo: 'Atención',
    implementado: false,
    permisos: { ver: TODOS, crear: [ADMIN, MEDICO], editar: [ADMIN, MEDICO], desactivar: SOLO_ADMIN },
  },
  {
    clave: 'diagnosticos',
    titulo: 'Diagnósticos',
    ruta: '/diagnosticos',
    grupo: 'Atención',
    implementado: false,
    permisos: { ver: TODOS, crear: [ADMIN, MEDICO], editar: [ADMIN, MEDICO], desactivar: SOLO_ADMIN },
  },
  {
    clave: 'tratamientos',
    titulo: 'Tratamientos',
    ruta: '/tratamientos',
    grupo: 'Atención',
    implementado: false,
    permisos: { ver: TODOS, crear: [ADMIN, MEDICO], editar: [ADMIN, MEDICO], desactivar: SOLO_ADMIN },
  },
  {
    clave: 'emergencia',
    titulo: 'Emergencia',
    ruta: '/emergencia',
    grupo: 'Atención',
    implementado: false,
    permisos: {
      ver: TODOS,
      crear: [ADMIN, MEDICO, ENFERMERO],
      editar: [ADMIN, MEDICO, ENFERMERO],
      desactivar: SOLO_ADMIN,
    },
  },
  {
    clave: 'triaje',
    titulo: 'Triaje',
    ruta: '/triaje',
    grupo: 'Atención',
    implementado: false,
    permisos: {
      ver: CLINICOS,
      crear: [ADMIN, MEDICO, ENFERMERO],
      editar: [ADMIN, MEDICO, ENFERMERO],
      desactivar: SOLO_ADMIN,
    },
  },
  {
    clave: 'hospitalizaciones',
    titulo: 'Hospitalizaciones',
    ruta: '/hospitalizaciones',
    grupo: 'Atención',
    implementado: false,
    permisos: {
      ver: CLINICOS,
      crear: [ADMIN, MEDICO, RECEPCIONISTA],
      editar: [ADMIN, MEDICO],
      desactivar: SOLO_ADMIN,
    },
  },
  {
    clave: 'habitaciones',
    titulo: 'Habitaciones',
    ruta: '/habitaciones',
    grupo: 'Atención',
    implementado: false,
    permisos: { ver: CLINICOS, crear: SOLO_ADMIN, editar: SOLO_ADMIN, desactivar: SOLO_ADMIN },
  },
  {
    clave: 'medicamentos',
    titulo: 'Medicamentos',
    ruta: '/medicamentos',
    grupo: 'Farmacia',
    implementado: false,
    permisos: {
      ver: TODOS,
      crear: [ADMIN, FARMACEUTICO, MEDICO],
      editar: [ADMIN, FARMACEUTICO],
      desactivar: SOLO_ADMIN,
    },
  },
  {
    clave: 'facturas',
    titulo: 'Facturas',
    ruta: '/facturas',
    grupo: 'Facturación',
    implementado: false,
    permisos: {
      ver: TODOS,
      crear: [ADMIN, CAJERO, RECEPCIONISTA],
      editar: [ADMIN, CAJERO],
      desactivar: SOLO_ADMIN,
    },
  },
  {
    clave: 'cuentas',
    titulo: 'Cuentas',
    ruta: '/cuentas',
    grupo: 'Facturación',
    implementado: false,
    permisos: { ver: TODOS, crear: [ADMIN, CAJERO], editar: [ADMIN, CAJERO], desactivar: SOLO_ADMIN },
  },
  {
    clave: 'pagos',
    titulo: 'Pagos',
    ruta: '/pagos',
    grupo: 'Facturación',
    implementado: false,
    permisos: { ver: [ADMIN, CAJERO], crear: [ADMIN, CAJERO], editar: SOLO_ADMIN, desactivar: SOLO_ADMIN },
  },
  {
    clave: 'personal',
    titulo: 'Personal médico',
    ruta: '/personal',
    grupo: 'Administración',
    implementado: false,
    // /api/medico, /api/turnos: CLINICOS; /api/enfermeros: TODOS. Escritura: solo ADMIN.
    permisos: { ver: CLINICOS, crear: SOLO_ADMIN, editar: SOLO_ADMIN, desactivar: SOLO_ADMIN },
  },
  {
    clave: 'catalogos',
    titulo: 'Catálogos',
    ruta: '/catalogos',
    grupo: 'Administración',
    implementado: false,
    // Clínicas, departamentos, especialidades, tipos y estados: lectura TODOS, escritura ADMIN.
    // Alergias: lectura CLINICOS, escritura ADMIN/MEDICO.
    permisos: { ver: TODOS, crear: SOLO_ADMIN, editar: SOLO_ADMIN, desactivar: SOLO_ADMIN },
  },
  {
    clave: 'usuarios',
    titulo: 'Usuarios y roles',
    ruta: '/usuarios',
    grupo: 'Administración',
    implementado: false,
    permisos: { ver: SOLO_ADMIN, crear: SOLO_ADMIN, editar: SOLO_ADMIN, desactivar: SOLO_ADMIN },
  },
]

export const obtenerModulo = (clave) => MODULOS.find((m) => m.clave === clave)

/** El rol puede realizar la operación (ver/crear/editar/desactivar) en el módulo? */
export const puede = (rol, claveModulo, operacion = 'ver') =>
  !!rol && !!obtenerModulo(claveModulo)?.permisos[operacion]?.includes(rol)

/** Módulos implementados que el rol puede al menos listar, agrupados para el sidebar. */
export function menuParaRol(rol) {
  const visibles = MODULOS.filter((m) => m.implementado && m.permisos.ver.includes(rol))
  const grupos = []
  for (const modulo of visibles) {
    let grupo = grupos.find((g) => g.titulo === modulo.grupo)
    if (!grupo) {
      grupo = { titulo: modulo.grupo, items: [] }
      grupos.push(grupo)
    }
    grupo.items.push(modulo)
  }
  return grupos
}
