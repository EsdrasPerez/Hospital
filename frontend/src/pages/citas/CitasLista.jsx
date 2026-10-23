import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { listarCitas } from '../../api/citas.js'
import { useAuth } from '../../auth/useAuth.js'
import { BadgeActivo } from '../../components/Badge.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import EstadoCitaBadge from '../../components/EstadoCitaBadge.jsx'
import Pagination from '../../components/Pagination.jsx'
import Table from '../../components/Table.jsx'
import { puede } from '../../config/menu.js'
import { useApi } from '../../hooks/useApi.js'
import { useDatosCita } from '../../hooks/useDatosCita.js'
import { formatearFecha, formatearHora } from '../../utils/formato.js'
import { nombreMedico, nombrePaciente } from './utilsCita.js'

const TAMANO_PAGINA = 10
const claseSelect = 'form-control'

export default function CitasLista() {
  const { rol } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const puedeCrear = puede(rol, 'citas', 'crear')
  const puedeEditar = puede(rol, 'citas', 'editar')

  const [idPaciente, setIdPaciente] = useState('')
  const [idMedico, setIdMedico] = useState('')
  const [fecha, setFecha] = useState('')
  const [idEstado, setIdEstado] = useState('')
  const [texto, setTexto] = useState('')
  const [incluirInactivas, setIncluirInactivas] = useState(false)
  const [pagina, setPagina] = useState(1)
  const [exito, setExito] = useState(location.state?.mensaje ?? '')

  const datosCita = useDatosCita()
  // El backend combina los filtros de paciente, médico y fecha; estado y búsqueda de texto se filtran aquí.
  const { datos, error, cargando } = useApi(`citas|${idPaciente}|${idMedico}|${fecha}`, () =>
    listarCitas({ idPaciente, idMedico, fecha }),
  )

  // Limpia el mensaje de éxito heredado de la navegación.
  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const filas = useMemo(() => {
    const busqueda = texto.trim().toLowerCase()
    return (datos ?? [])
      .filter((c) => incluirInactivas || c.activo !== false)
      .filter((c) => !idPaciente || String(c.idPaciente) === idPaciente)
      .filter((c) => !idMedico || String(c.idMedico) === idMedico)
      .filter((c) => !fecha || c.fecha === fecha)
      .filter((c) => !idEstado || String(c.idEstado) === idEstado)
      .filter((c) => {
        if (!busqueda) return true
        const paciente = nombrePaciente(datosCita.pacientesPorId[c.idPaciente], c.idPaciente)
        const medico = nombreMedico(datosCita.medicosPorId[c.idMedico], c.idMedico)
        return `${paciente} ${medico} ${c.motivo ?? ''}`.toLowerCase().includes(busqueda)
      })
      .sort((a, b) => `${b.fecha} ${b.hora}`.localeCompare(`${a.fecha} ${a.hora}`))
  }, [datos, texto, incluirInactivas, idPaciente, idMedico, fecha, idEstado, datosCita])

  const alCambiar = (setter) => (e) => {
    setter(e.target.value)
    setPagina(1)
  }

  const columnas = [
    { clave: 'fecha', titulo: 'Fecha', render: (c) => formatearFecha(c.fecha) },
    { clave: 'hora', titulo: 'Hora', render: (c) => formatearHora(c.hora) },
    {
      clave: 'paciente',
      titulo: 'Paciente',
      render: (c) => (
        <Link to={`/citas/${c.idCita}`} className="font-weight-bold">
          {nombrePaciente(datosCita.pacientesPorId[c.idPaciente], c.idPaciente)}
        </Link>
      ),
    },
    { clave: 'medico', titulo: 'Médico', render: (c) => nombreMedico(datosCita.medicosPorId[c.idMedico], c.idMedico) },
    {
      clave: 'estado',
      titulo: 'Estado',
      render: (c) => (
        <span className="d-flex align-items-center">
          <EstadoCitaBadge estado={datosCita.estadosPorId[c.idEstado] ?? { descripcion: `#${c.idEstado}` }} />
          {c.activo === false && <BadgeActivo activo={false} />}
        </span>
      ),
    },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      className: 'text-right',
      render: (c) => (
        <div className="tabla-acciones d-flex justify-content-end">
          <Link to={`/citas/${c.idCita}`}>
            Ver
          </Link>
          {puedeEditar && c.activo !== false && (
            <Link to={`/citas/${c.idCita}/editar`}>
              Editar
            </Link>
          )}
        </div>
      ),
    },
  ]

  return (
    <div>
      <div className="card card-body d-flex flex-column flex-md-row align-items-md-center justify-content-md-between">
        <div>
          <h1 className="h3 mb-0">Citas</h1>
          <p className="text-muted mb-0">Programación y seguimiento de citas médicas.</p>
        </div>
        {puedeCrear && (
          <Link
            to="/citas/nueva"
            className="btn btn-primary btn-round"
          >
            Nueva cita
          </Link>
        )}
      </div>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />
      <ErrorAlert mensaje={error?.message} />

      <Table
        columnas={columnas}
        filas={filas.slice((pagina - 1) * TAMANO_PAGINA, pagina * TAMANO_PAGINA)}
        claveFila={(c) => c.idCita}
        cargando={cargando}
        busqueda={texto}
        onBuscar={(v) => {
          setTexto(v)
          setPagina(1)
        }}
        placeholderBusqueda="Buscar por paciente, médico o motivo"
        acciones={
          <>
            <select aria-label="Filtrar por paciente" className={claseSelect} value={idPaciente} onChange={alCambiar(setIdPaciente)}>
              <option value="">Todos los pacientes</option>
              {datosCita.pacientes
                .filter((p) => p.activo !== false)
                .map((p) => (
                  <option key={p.idPaciente} value={p.idPaciente}>
                    {nombrePaciente(p)}
                  </option>
                ))}
            </select>
            <select aria-label="Filtrar por médico" className={claseSelect} value={idMedico} onChange={alCambiar(setIdMedico)}>
              <option value="">Todos los médicos</option>
              {datosCita.medicos.map((m) => (
                <option key={m.idMedico} value={m.idMedico}>
                  {nombreMedico(m)}
                </option>
              ))}
            </select>
            <select aria-label="Filtrar por estado" className={claseSelect} value={idEstado} onChange={alCambiar(setIdEstado)}>
              <option value="">Todos los estados</option>
              {datosCita.estados.map((s) => (
                <option key={s.idEstado} value={s.idEstado}>
                  {s.descripcion}
                </option>
              ))}
            </select>
            <input
              type="date"
              aria-label="Filtrar por fecha"
              className={claseSelect}
              value={fecha}
              onChange={alCambiar(setFecha)}
            />
            <label className="d-flex align-items-center small mb-0">
              <input
                type="checkbox"
                checked={incluirInactivas}
                onChange={(e) => {
                  setIncluirInactivas(e.target.checked)
                  setPagina(1)
                }}
                className="mr-2"
              />
              Mostrar inactivas
            </label>
          </>
        }
        vacio={<EmptyState titulo="No hay citas" descripcion="Ajuste los filtros o programe una nueva cita." />}
        pie={<Pagination pagina={pagina} totalElementos={filas.length} tamano={TAMANO_PAGINA} onCambiarPagina={setPagina} />}
      />
    </div>
  )
}
