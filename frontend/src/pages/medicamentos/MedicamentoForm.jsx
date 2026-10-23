import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { actualizarMedicamento, crearMedicamento, obtenerMedicamento } from '../../api/medicamentos.js'
import ErrorAlert from '../../components/ErrorAlert.jsx'
import FormField from '../../components/FormField.jsx'
import Loading from '../../components/Loading.jsx'
import { useApi } from '../../hooks/useApi.js'
import { normalizarError } from '../../utils/errores.js'

const VACIO = {
  nombreComercial: '',
  nombreGenerico: '',
  presentacion: '',
  laboratorio: '',
  stock: '0',
  stockMinimo: '0',
  precio: '',
  requiereReceta: false,
}

function aFormulario(m) {
  const texto = (v) => (v != null ? String(v) : '')
  return {
    nombreComercial: m.nombreComercial ?? '',
    nombreGenerico: m.nombreGenerico ?? '',
    presentacion: m.presentacion ?? '',
    laboratorio: m.laboratorio ?? '',
    stock: texto(m.stock),
    stockMinimo: texto(m.stockMinimo),
    precio: texto(m.precio),
    requiereReceta: Boolean(m.requiereReceta),
  }
}

/** Replica las reglas del backend (MedicamentosServiceImpl) y los tamaños de las columnas. */
function validar(f) {
  const e = {}
  const largo = (campo, etiqueta, max) => {
    if (f[campo].trim().length > max) e[campo] = `${etiqueta}: máximo ${max} caracteres`
  }
  if (!f.nombreComercial.trim()) e.nombreComercial = 'El nombre comercial es obligatorio'
  largo('nombreComercial', 'El nombre comercial', 150)
  largo('nombreGenerico', 'El nombre genérico', 150)
  largo('presentacion', 'La presentación', 100)
  largo('laboratorio', 'El laboratorio', 100)
  const entero = (campo, etiqueta) => {
    const n = Number(f[campo])
    if (f[campo] === '' || !Number.isInteger(n) || n < 0) e[campo] = `${etiqueta} debe ser un entero mayor o igual a cero`
  }
  entero('stock', 'El stock')
  entero('stockMinimo', 'El stock mínimo')
  const precio = Number(f.precio)
  if (f.precio === '' || Number.isNaN(precio) || precio <= 0) e.precio = 'El precio es obligatorio y debe ser mayor a cero'
  else if (precio > 99999999.99) e.precio = 'El precio es demasiado alto'
  else if ((f.precio.split('.')[1] ?? '').length > 2) e.precio = 'El precio admite como máximo 2 decimales'
  return e
}

export default function MedicamentoForm() {
  const { id } = useParams()
  const esEdicion = Boolean(id)
  const navigate = useNavigate()
  const { datos: original, error: errorCarga, cargando } = useApi(`medicamento-form|${id ?? 'nuevo'}`, () =>
    esEdicion ? obtenerMedicamento(id) : null,
  )

  const [cambios, setCambios] = useState({})
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState('')
  const [guardando, setGuardando] = useState(false)

  if (cargando) return <Loading texto="Cargando..." />

  if (errorCarga) {
    return (
      <div>
        <ErrorAlert
          titulo={errorCarga.status === 404 ? 'Medicamento no encontrado' : 'No se pudo cargar el formulario'}
          mensaje={errorCarga.message}
        />
        <Link to="/medicamentos" className="volver-enlace">
          &larr; Volver a medicamentos
        </Link>
      </div>
    )
  }

  const form = { ...(original ? aFormulario(original) : VACIO), ...cambios }

  const cambiar = (e) => {
    const { name, value, type, checked } = e.target
    setCambios((c) => ({ ...c, [name]: type === 'checkbox' ? checked : value }))
    if (errores[name]) setErrores((er) => ({ ...er, [name]: undefined }))
  }

  const enviar = async (e) => {
    e.preventDefault()
    setErrorGeneral('')
    const locales = validar(form)
    setErrores(locales)
    if (Object.keys(locales).length) return

    setGuardando(true)
    try {
      const textoONulo = (v) => (v.trim() === '' ? null : v.trim())
      const payload = {
        nombreComercial: form.nombreComercial.trim(),
        nombreGenerico: textoONulo(form.nombreGenerico),
        presentacion: textoONulo(form.presentacion),
        laboratorio: textoONulo(form.laboratorio),
        // En edición el stock se conserva tal cual (los cambios se registran con "Ajustar stock")
        stock: Number(form.stock),
        stockMinimo: Number(form.stockMinimo),
        precio: Number(form.precio),
        requiereReceta: form.requiereReceta,
      }
      const guardado = esEdicion ? await actualizarMedicamento(id, payload) : await crearMedicamento(payload)
      navigate(`/medicamentos/${guardado.idMedicamento}`, {
        replace: true,
        state: { mensaje: esEdicion ? 'Medicamento actualizado.' : 'Medicamento registrado.' },
      })
    } catch (err) {
      // 400 con `detalles` por campo (mismos nombres que el DTO); 409 nombre comercial repetido; 404 no existe: se muestran con `message`
      const { message, detalles } = normalizarError(err)
      setErrores(detalles)
      setErrorGeneral(Object.keys(detalles).length ? 'Revise los campos marcados.' : message)
    } finally {
      setGuardando(false)
    }
  }

  return (
    <div>
      <Link to={esEdicion ? `/medicamentos/${id}` : '/medicamentos'} className="volver-enlace">
        &larr; {esEdicion ? 'Volver al detalle' : 'Volver a medicamentos'}
      </Link>
      <div className="card card-body">
        <h1 className="h3 mb-0">{esEdicion ? 'Editar medicamento' : 'Nuevo medicamento'}</h1>
      </div>

      <form onSubmit={enviar} noValidate className="card card-body">
        <ErrorAlert mensaje={errorGeneral} />

        <div className="row">
          <FormField
            nombre="nombreComercial"
            etiqueta="Nombre comercial"
            valor={form.nombreComercial}
            onChange={cambiar}
            error={errores.nombreComercial}
            maxLength={150}
            requerido
          />
          <FormField
            nombre="nombreGenerico"
            etiqueta="Nombre genérico"
            valor={form.nombreGenerico}
            onChange={cambiar}
            error={errores.nombreGenerico}
            maxLength={150}
          />
          <FormField
            nombre="presentacion"
            etiqueta="Presentación"
            valor={form.presentacion}
            onChange={cambiar}
            error={errores.presentacion}
            maxLength={100}
            placeholder="Ej. Tabletas 500 mg, caja x 20"
          />
          <FormField
            nombre="laboratorio"
            etiqueta="Laboratorio"
            valor={form.laboratorio}
            onChange={cambiar}
            error={errores.laboratorio}
            maxLength={100}
          />
          <FormField
            nombre="stock"
            etiqueta="Stock actual"
            tipo="number"
            step="1"
            min="0"
            valor={form.stock}
            onChange={cambiar}
            error={errores.stock}
            disabled={esEdicion}
            ayuda={esEdicion ? 'Para modificarlo use «Ajustar stock» en el detalle del medicamento.' : undefined}
            requerido
          />
          <FormField
            nombre="stockMinimo"
            etiqueta="Stock mínimo"
            tipo="number"
            step="1"
            min="0"
            valor={form.stockMinimo}
            onChange={cambiar}
            error={errores.stockMinimo}
            ayuda="Por debajo o igual a este valor se marca «Stock bajo»."
            requerido
          />
          <FormField
            nombre="precio"
            etiqueta="Precio (Q)"
            tipo="number"
            step="0.01"
            min="0"
            valor={form.precio}
            onChange={cambiar}
            error={errores.precio}
            requerido
          />
          <div className="col-md-6 d-flex align-items-center pt-3">
            <label className="d-flex align-items-center mb-0">
              <input type="checkbox" name="requiereReceta" checked={form.requiereReceta} onChange={cambiar} className="mr-2" />
              Requiere receta médica
            </label>
          </div>
        </div>

        <div className="acciones-grupo d-flex justify-content-end border-top pt-3">
          <Link to={esEdicion ? `/medicamentos/${id}` : '/medicamentos'} className="btn btn-secondary btn-round">
            Cancelar
          </Link>
          <button type="submit" disabled={guardando} className="btn btn-primary btn-round">
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Registrar medicamento'}
          </button>
        </div>
      </form>
    </div>
  )
}
