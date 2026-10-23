import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { obtenerHistorialPaciente } from '../../api/reportes.js'
import { listarEstadosCita, listarEstadosFactura } from '../../api/catalogos.js'
import { listarMedicamentos } from '../../api/medicamentos.js'
import { useAuth } from '../../auth/useAuth.js'
import Badge from '../../components/Badge.jsx'
import EstadoCitaBadge from '../../components/EstadoCitaBadge.jsx'
import EstadoFacturaBadge from '../../components/EstadoFacturaBadge.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import { puede } from '../../config/menu.js'
import { mapaPor, useApi } from '../../hooks/useApi.js'
import { formatearFecha, formatearFechaHora, formatearHora, formatearMoneda } from '../../utils/formato.js'
import { estadoEmergencia } from '../emergencias/utilsEmergencia.js'

const TIPOS = [
  { clave: 'consulta', titulo: 'Consultas', singular: 'Consulta', icono: 'files_paper' },
  { clave: 'diagnostico', titulo: 'Diagnósticos', singular: 'Diagnóstico', icono: 'education_glasses' },
  { clave: 'tratamiento', titulo: 'Tratamientos', singular: 'Tratamiento', icono: 'objects_support-17' },
  { clave: 'cita', titulo: 'Citas', singular: 'Cita', icono: 'ui-1_calendar-60' },
  { clave: 'hospitalizacion', titulo: 'Hospitalizaciones', singular: 'Hospitalización', icono: 'business_bank' },
  { clave: 'emergencia', titulo: 'Emergencias', singular: 'Emergencia', icono: 'health_ambulance' },
  { clave: 'cuenta', titulo: 'Cuentas', singular: 'Cuenta', icono: 'business_money-coins' },
  { clave: 'factura', titulo: 'Facturas', singular: 'Factura', icono: 'files_single-copy-04' },
]
const porClave = Object.fromEntries(TIPOS.map((t) => [t.clave, t]))

/**
 * Historial clínico: línea de tiempo con lo que devuelve GET /api/reportes/paciente/{id}/historial (citas, consultas, diagnósticos,
 * tratamientos con sus medicamentos, hospitalizaciones, emergencias y, solo para ADMIN, cuentas y facturas). Cada registro enlaza a su
 * pantalla si el rol puede abrirla. "Mostrar anulados" se envía al servidor (`incluirAnulados`). Un paciente inactivo conserva su
 * historial y se muestra con un aviso.
 */
export default function HistorialPaciente({ idPaciente }) {
  const { rol } = useAuth()
  const [tipo, setTipo] = useState('')
  const [incluirInactivos, setIncluirInactivos] = useState(false)

  const { datos, error, cargando } = useApi(`historial|${idPaciente}|${incluirInactivos}`, async () => {
    const [historial, estadosCita, estadosFactura, medicamentos] = await Promise.all([
      obtenerHistorialPaciente(idPaciente, { incluirAnulados: incluirInactivos }),
      listarEstadosCita().catch(() => []),
      listarEstadosFactura().catch(() => []),
      listarMedicamentos().catch(() => []),
    ])
    return {
      historial,
      estadosCita: mapaPor(estadosCita, 'idEstado'),
      estadosFactura: mapaPor(estadosFactura, 'idEstadoFactura'),
      medicamentos: mapaPor(medicamentos, 'idMedicamento'),
    }
  })

  const verCaja = puede(rol, 'facturas', 'ver')

  const eventos = useMemo(() => {
    if (!datos) return []
    const { historial: h, estadosCita, estadosFactura, medicamentos } = datos
    const enlace = (modulo, ruta) => (puede(rol, modulo, 'ver') ? ruta : null)
    const lista = [
      ...(h.consultas ?? []).map((c) => ({
        tipo: 'consulta',
        id: `cons-${c.idConsulta}`,
        fecha: c.fechaHora ?? '',
        titulo: `Consulta #${c.idConsulta}`,
        detalle: c.sintomas,
        etiqueta: null,
        ruta: enlace('consultas', `/consultas/${c.idConsulta}`),
        activo: c.activo,
      })),
      ...(h.diagnosticos ?? []).map((d) => ({
        tipo: 'diagnostico',
        id: `diag-${d.idDiagnostico}`,
        fecha: d.fecha ?? '',
        titulo: `Diagnóstico #${d.idDiagnostico}`,
        detalle: [d.descripcion, d.observaciones].filter(Boolean).join(' · '),
        etiqueta: null,
        ruta: enlace('diagnosticos', `/diagnosticos/${d.idDiagnostico}`),
        activo: d.activo,
      })),
      ...(h.tratamientos ?? []).map((t) => {
        const meds = (h.medicamentosTratamiento ?? [])
          .filter((m) => m.idTratamiento === t.idTratamiento)
          .map((m) => medicamentos[m.idMedicamento]?.nombreComercial ?? `Medicamento #${m.idMedicamento}`)
        return {
          tipo: 'tratamiento',
          id: `trat-${t.idTratamiento}`,
          fecha: t.fechaInicio ?? '',
          titulo: `Tratamiento #${t.idTratamiento}`,
          detalle: [t.descripcion, meds.length ? `Medicamentos: ${meds.join(', ')}` : ''].filter(Boolean).join(' · '),
          etiqueta: null,
          ruta: enlace('tratamientos', `/tratamientos/${t.idTratamiento}`),
          activo: t.activo,
        }
      }),
      ...(h.citas ?? []).map((c) => ({
        tipo: 'cita',
        id: `cita-${c.idCita}`,
        fecha: `${c.fecha ?? ''}T${(c.hora ?? '00:00:00').slice(0, 8)}`,
        titulo: `Cita del ${formatearFecha(c.fecha)} a las ${formatearHora(c.hora)}`,
        detalle: c.motivo,
        etiqueta: <EstadoCitaBadge estado={estadosCita[c.idEstado]} />,
        ruta: enlace('citas', `/citas/${c.idCita}`),
        activo: c.activo,
      })),
      ...(h.hospitalizaciones ?? []).map((x) => ({
        tipo: 'hospitalizacion',
        id: `hosp-${x.idHospitalizacion}`,
        fecha: x.fechaIngreso ?? '',
        titulo: `Hospitalización #${x.idHospitalizacion}`,
        detalle: [x.motivoIngreso, x.fechaAlta ? `Alta: ${formatearFechaHora(x.fechaAlta)}` : 'En curso'].filter(Boolean).join(' · '),
        etiqueta: <Badge tono={x.fechaAlta ? 'verde' : 'teal'}>{x.fechaAlta ? 'Con alta' : 'Activa'}</Badge>,
        ruta: enlace('hospitalizaciones', `/hospitalizaciones/${x.idHospitalizacion}`),
        activo: x.activo,
      })),
      ...(h.emergencias ?? []).map((e) => {
        const est = estadoEmergencia(e)
        return {
          tipo: 'emergencia',
          id: `emer-${e.idEmergencia}`,
          fecha: e.fechaHoraIngreso ?? '',
          titulo: `Emergencia #${e.idEmergencia}`,
          detalle: e.motivo,
          etiqueta: <Badge tono={est.tono}>{est.texto}</Badge>,
          ruta: enlace('emergencia', `/emergencia/${e.idEmergencia}`),
          activo: e.activo,
        }
      }),
      ...(verCaja && h.incluyeDatosFinancieros
        ? [
            ...(h.cuentas ?? []).map((c) => ({
              tipo: 'cuenta',
              id: `cuenta-${c.idCuenta}`,
              fecha: c.fechaApertura ?? '',
              titulo: `Cuenta #${c.idCuenta}`,
              detalle: `Saldo ${formatearMoneda(c.saldo)}`,
              etiqueta: <Badge tono="azul">{c.estado}</Badge>,
              ruta: enlace('cuentas', `/cuentas/${c.idCuenta}`),
              activo: c.activo,
            })),
            ...(h.facturas ?? []).map((f) => ({
              tipo: 'factura',
              id: `fact-${f.idFactura}`,
              fecha: f.fechaEmision ?? '',
              titulo: `Factura #${f.idFactura}`,
              detalle: `Total ${formatearMoneda(f.total)}`,
              etiqueta: <EstadoFacturaBadge estado={estadosFactura[f.idEstadoFactura]} activo={f.activo} />,
              ruta: enlace('facturas', `/facturas/${f.idFactura}`),
              activo: f.activo,
            })),
          ]
        : []),
    ]
    return lista.sort((a, b) => String(b.fecha).localeCompare(String(a.fecha)))
  }, [datos, rol, verCaja])

  // El servidor ya omite los anulados salvo `incluirAnulados`; aquí solo se filtra por tipo
  const visibles = eventos.filter((e) => !tipo || e.tipo === tipo)
  const tiposPresentes = TIPOS.filter((t) => eventos.some((e) => e.tipo === t.clave))

  return (
    <div className="card card-body">
      <h2 className="h5 mb-1">Historial clínico</h2>
      <p className="text-muted small">Atención clínica del paciente (citas, consultas, diagnósticos, tratamientos, hospitalizaciones y emergencias), de la más reciente a la más antigua.</p>

      {/* 404: el paciente no existe. Un paciente inactivo sí devuelve su historial (histórico) */}
      <ErrorAlert tipo="aviso" mensaje={error?.message} />
      {datos?.historial.paciente?.activo === false && (
        <ErrorAlert tipo="aviso" titulo="Paciente inactivo" mensaje="El paciente está desactivado; se muestra su historial registrado." />
      )}
      {datos && !datos.historial.incluyeDatosFinancieros && verCaja && (
        <p className="text-muted small">Las cuentas y facturas no se incluyen para su perfil.</p>
      )}

      {cargando ? (
        <p className="text-muted mb-0">Cargando historial...</p>
      ) : !error ? (
        <>
          <div className="d-flex flex-wrap align-items-center mb-3 acciones-filtros" role="group" aria-label="Filtrar por tipo de registro">
            <button type="button" className={`btn btn-sm btn-round ${tipo === '' ? 'btn-primary' : 'btn-outline-secondary'}`} aria-pressed={tipo === ''} onClick={() => setTipo('')}>
              Todo ({eventos.length})
            </button>
            {tiposPresentes.map((t) => (
              <button key={t.clave} type="button" className={`btn btn-sm btn-round ${tipo === t.clave ? 'btn-primary' : 'btn-outline-secondary'}`} aria-pressed={tipo === t.clave} onClick={() => setTipo(t.clave)}>
                {t.titulo} ({eventos.filter((e) => e.tipo === t.clave).length})
              </button>
            ))}
            <label className="d-flex align-items-center small mb-0 ml-2">
              <input type="checkbox" className="mr-2" checked={incluirInactivos} onChange={(e) => setIncluirInactivos(e.target.checked)} />
              Mostrar anulados
            </label>
          </div>

          {visibles.length === 0 ? (
            <p className="text-muted mb-0">No hay registros para mostrar.</p>
          ) : (
            <ol className="linea-tiempo">
              {visibles.map((e) => (
                <li key={e.id}>
                  <span className="linea-tiempo-punto" aria-hidden="true">
                    <i className={`now-ui-icons ${porClave[e.tipo].icono}`} />
                  </span>
                  <div className="d-flex flex-wrap align-items-center justify-content-between">
                    <div>
                      <span className="small text-muted d-block">
                        {porClave[e.tipo].singular} · {String(e.fecha).length > 10 ? formatearFechaHora(e.fecha) : formatearFecha(e.fecha)}
                      </span>
                      {e.ruta ? (
                        <Link to={e.ruta} className="font-weight-bold">
                          {e.titulo}
                        </Link>
                      ) : (
                        <strong>{e.titulo}</strong>
                      )}
                      {e.detalle && <div className="text-muted small">{e.detalle}</div>}
                    </div>
                    <div className="d-flex align-items-center acciones-grupo">
                      {e.etiqueta}
                      {e.activo === false && <Badge tono="rojo">Anulado</Badge>}
                    </div>
                  </div>
                </li>
              ))}
            </ol>
          )}
        </>
      ) : null}
    </div>
  )
}
