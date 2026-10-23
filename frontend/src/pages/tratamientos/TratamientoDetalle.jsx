import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { obtenerDiagnostico } from '../../api/diagnosticos.js'
import { listarMedicamentos } from '../../api/medicamentos.js'
import { asignarMedicamento, listarRecetas, quitarMedicamento } from '../../api/recetas.js'
import { desactivarTratamiento, obtenerTratamiento } from '../../api/tratamientos.js'
import { useAuth } from '../../auth/useAuth.js'
import { BadgeActivo } from '../../components/Badge.jsx'
import ConfirmDialog from '../../components/ConfirmDialog.jsx'
import Dato from '../../components/Dato.jsx'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import Loading from '../../components/Loading.jsx'
import StockBadge from '../../components/StockBadge.jsx'
import { puede } from '../../config/menu.js'
import { mapaPor, useApi } from '../../hooks/useApi.js'
import { normalizarError } from '../../utils/errores.js'
import { formatearFecha } from '../../utils/formato.js'

export default function TratamientoDetalle() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { rol } = useAuth()
  const puedeEditar = puede(rol, 'tratamientos', 'editar')
  const puedeDesactivar = puede(rol, 'tratamientos', 'desactivar')
  // /api/tratamiento-medicamento: lectura ADMIN/MEDICO/FARMACEUTICO, asignar y quitar solo ADMIN/MEDICO
  const puedeVerMedicamentos = puede(rol, 'recetas', 'ver')
  const puedeAsignar = puede(rol, 'recetas', 'crear')
  const puedeQuitar = puede(rol, 'recetas', 'desactivar')

  const [exito, setExito] = useState(location.state?.mensaje ?? '')
  const [confirmando, setConfirmando] = useState(false)
  const [desactivando, setDesactivando] = useState(false)
  const [errorDesactivar, setErrorDesactivar] = useState('')
  const [nuevoMedicamento, setNuevoMedicamento] = useState('')
  const [asignando, setAsignando] = useState(false)
  const [errorAsignar, setErrorAsignar] = useState('')
  const [aQuitar, setAQuitar] = useState(null)
  const [quitando, setQuitando] = useState(false)
  const [errorQuitar, setErrorQuitar] = useState('')

  const { datos, error, cargando } = useApi(`tratamiento|${id}`, async () => {
    const tratamiento = await obtenerTratamiento(id)
    const diagnostico = await obtenerDiagnostico(tratamiento.idDiagnostico).catch(() => null)
    return { tratamiento, diagnostico }
  })

  // Medicamentos asignados a este tratamiento (filtrados en el servidor con idTratamiento)
  const { datos: recetas, recargar: recargarRecetas } = useApi(`tratamiento-medicamentos|${id}|${puedeVerMedicamentos}`, async () => {
    if (!puedeVerMedicamentos) return null
    const [relaciones, medicamentos] = await Promise.all([listarRecetas({ idTratamiento: id }), listarMedicamentos().catch(() => [])])
    const porId = mapaPor(medicamentos, 'idMedicamento')
    return {
      asignados: relaciones.map((r) => ({ ...r, medicamento: porId[r.idMedicamento] })),
      medicamentos,
    }
  })

  useEffect(() => {
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null })
  }, [location.state, location.pathname, navigate])

  const desactivar = async () => {
    setDesactivando(true)
    setErrorDesactivar('')
    try {
      await desactivarTratamiento(id)
      navigate('/tratamientos', { replace: true, state: { mensaje: 'El tratamiento fue desactivado.' } })
    } catch (e) {
      // 422: tiene medicamentos asociados
      setErrorDesactivar(normalizarError(e).message)
      setDesactivando(false)
    }
  }

  const asignar = async () => {
    setAsignando(true)
    setErrorAsignar('')
    try {
      await asignarMedicamento(Number(nuevoMedicamento), Number(id))
      setNuevoMedicamento('')
      setExito('Medicamento asignado al tratamiento.')
      recargarRecetas()
    } catch (e) {
      // 400: ids ausentes (detalles); 409: ya asignado; 422: tratamiento o medicamento inexistente o inactivo
      const { message, detalles } = normalizarError(e)
      const campos = Object.values(detalles ?? {})
      setErrorAsignar(campos.length ? campos.join(' ') : message)
    } finally {
      setAsignando(false)
    }
  }

  const quitar = async () => {
    setQuitando(true)
    setErrorQuitar('')
    try {
      await quitarMedicamento(aQuitar.idMedicamento, aQuitar.idTratamiento)
      setAQuitar(null)
      setExito('Medicamento quitado del tratamiento.')
      recargarRecetas()
    } catch (e) {
      setErrorQuitar(normalizarError(e).message)
    } finally {
      setQuitando(false)
    }
  }

  if (cargando) return <Loading texto="Cargando tratamiento..." />

  if (error) {
    return (
      <div>
        <ErrorAlert titulo={error.status === 404 ? 'Tratamiento no encontrado' : 'No se pudo cargar el tratamiento'} mensaje={error.message} />
        <Link to="/tratamientos" className="volver-enlace">
          &larr; Volver a tratamientos
        </Link>
      </div>
    )
  }

  const { tratamiento: t, diagnostico } = datos
  const activo = t.activo !== false

  return (
    <div>
      <Link to="/tratamientos" className="volver-enlace">
        &larr; Volver a tratamientos
      </Link>

      <ErrorAlert tipo="exito" mensaje={exito} onCerrar={() => setExito('')} />

      <div className="card card-body d-flex flex-column flex-md-row align-items-md-center justify-content-md-between">
        <div>
          <h1 className="h3 mb-0">{t.descripcion}</h1>
          <p className="text-muted small">
            <BadgeActivo activo={t.activo} />
          </p>
        </div>
        <div className="acciones-grupo d-flex">
          {activo && puedeEditar && (
            <Link
              to={`/tratamientos/${t.idTratamiento}/editar`}
              className="btn btn-secondary btn-round"
            >
              Editar
            </Link>
          )}
          {activo && puedeDesactivar && (
            <button
              type="button"
              onClick={() => {
                setErrorDesactivar('')
                setConfirmando(true)
              }}
              className="btn btn-danger btn-round"
            >
              Desactivar
            </button>
          )}
        </div>
      </div>

      <div className="card card-body">
        <div className="row">
          <Dato etiqueta="Diagnóstico" className="col-12">
            <Link to={`/diagnosticos/${t.idDiagnostico}`}>
              {diagnostico?.descripcion ?? `Diagnóstico #${t.idDiagnostico}`}
            </Link>
          </Dato>
          <Dato etiqueta="Duración">{t.duracionDias != null ? `${t.duracionDias} días` : ''}</Dato>
          <Dato etiqueta="Fecha de inicio">{t.fechaInicio ? formatearFecha(t.fechaInicio) : ''}</Dato>
          <Dato etiqueta="Fecha de fin">{t.fechaFin ? formatearFecha(t.fechaFin) : ''}</Dato>
          <Dato etiqueta="Indicaciones" className="col-12">
            {t.indicaciones}
          </Dato>
        </div>
      </div>

      {puedeVerMedicamentos && (
        <div className="card card-body">
          <h2 className="h5 mb-3">Medicamentos del tratamiento</h2>
          {recetas === null ? (
            <p className="text-muted small mb-0">Cargando...</p>
          ) : recetas.asignados.length === 0 ? (
            <p className="text-muted">No hay medicamentos asignados a este tratamiento.</p>
          ) : (
            <ul className="lista-divisoria">
              {recetas.asignados.map((r) => (
                <li key={r.idMedicamento} className="d-flex flex-wrap align-items-center justify-content-between">
                  <span>
                    <Link to={`/medicamentos/${r.idMedicamento}`} className="font-weight-bold">
                      {r.medicamento?.nombreComercial ?? `Medicamento #${r.idMedicamento}`}
                    </Link>
                    {r.medicamento?.presentacion && <span className="text-muted small ml-2">{r.medicamento.presentacion}</span>}
                  </span>
                  <span className="d-flex align-items-center acciones-grupo">
                    {r.medicamento && <StockBadge medicamento={r.medicamento} />}
                    {puedeQuitar && (
                      <button
                        type="button"
                        className="btn btn-link text-danger p-0"
                        onClick={() => {
                          setErrorQuitar('')
                          setAQuitar(r)
                        }}
                      >
                        Quitar
                      </button>
                    )}
                  </span>
                </li>
              ))}
            </ul>
          )}

          {puedeAsignar && activo && recetas !== null && (
            <div className="border-top pt-3 mt-3">
              <ErrorAlert mensaje={errorAsignar} onCerrar={() => setErrorAsignar('')} />
              <div className="d-flex flex-wrap align-items-end acciones-grupo">
                <div className="flex-grow-1">
                  <label htmlFor="nuevo-medicamento" className="d-block font-weight-bold small mb-1">
                    Asignar un medicamento
                  </label>
                  <select id="nuevo-medicamento" className="form-control" value={nuevoMedicamento} onChange={(e) => setNuevoMedicamento(e.target.value)}>
                    <option value="">Seleccione...</option>
                    {recetas.medicamentos
                      .filter((m) => m.activo !== false && !recetas.asignados.some((r) => r.idMedicamento === m.idMedicamento))
                      .map((m) => (
                        <option key={m.idMedicamento} value={m.idMedicamento}>
                          {m.nombreComercial}
                          {m.presentacion ? ` · ${m.presentacion}` : ''}
                        </option>
                      ))}
                  </select>
                </div>
                <button type="button" className="btn btn-primary btn-round" onClick={asignar} disabled={!nuevoMedicamento || asignando}>
                  {asignando ? 'Asignando...' : 'Asignar'}
                </button>
              </div>
            </div>
          )}
          {puedeAsignar && !activo && <p className="text-muted small mb-0 mt-2">No se pueden asignar medicamentos a un tratamiento inactivo.</p>}
        </div>
      )}

      <ConfirmDialog
        abierto={!!aQuitar}
        titulo="Quitar medicamento"
        mensaje="¿Desea quitar este medicamento del tratamiento? El medicamento no se elimina."
        textoConfirmar="Quitar"
        peligro
        cargando={quitando}
        error={errorQuitar}
        onConfirmar={quitar}
        onCancelar={() => setAQuitar(null)}
      />
      <ConfirmDialog
        abierto={confirmando}
        titulo="Desactivar tratamiento"
        mensaje="¿Desea desactivar este tratamiento? No se permite si tiene medicamentos asociados."
        textoConfirmar="Desactivar"
        peligro
        cargando={desactivando}
        error={errorDesactivar}
        onConfirmar={desactivar}
        onCancelar={() => setConfirmando(false)}
      />
    </div>
  )
}
