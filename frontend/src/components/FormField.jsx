import { useId } from 'react'

const base =
  'block w-full rounded-lg border bg-white px-3 py-2 text-sm text-slate-900 shadow-sm placeholder:text-slate-400 ' +
  'focus:border-teal-600 focus:outline-none focus:ring-2 focus:ring-teal-600/30 disabled:bg-slate-100 disabled:text-slate-500'

/**
 * Campo de formulario con etiqueta y error debajo.
 * - tipo: 'text' | 'email' | 'date' | 'password' | 'select' | 'textarea' ...
 * - Para 'select' pasar `opciones: [{ valor, etiqueta }]` y `placeholder` (opción vacía).
 * - `error` es el mensaje del campo (cliente o `detalles` del backend).
 */
export default function FormField({
  nombre,
  etiqueta,
  tipo = 'text',
  valor,
  onChange,
  error,
  requerido = false,
  opciones = [],
  placeholder,
  ayuda,
  className = '',
  ...resto
}) {
  const id = useId()
  const errorId = `${id}-error`
  const comunes = {
    id,
    name: nombre,
    value: valor ?? '',
    onChange,
    'aria-invalid': error ? true : undefined,
    'aria-describedby': error ? errorId : undefined,
    className: `${base} ${error ? 'border-red-400' : 'border-slate-300'}`,
    ...resto,
  }

  let control
  if (tipo === 'select') {
    control = (
      <select {...comunes}>
        <option value="">{placeholder ?? 'Seleccione...'}</option>
        {opciones.map((o) => (
          <option key={o.valor} value={o.valor}>
            {o.etiqueta}
          </option>
        ))}
      </select>
    )
  } else if (tipo === 'textarea') {
    control = <textarea rows={3} placeholder={placeholder} {...comunes} />
  } else {
    control = <input type={tipo} placeholder={placeholder} {...comunes} />
  }

  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1 block text-sm font-medium text-slate-700">
        {etiqueta}
        {requerido && (
          <span className="ml-0.5 text-red-600" aria-hidden="true">
            *
          </span>
        )}
      </label>
      {control}
      {ayuda && !error && <p className="mt-1 text-xs text-slate-500">{ayuda}</p>}
      {error && (
        <p id={errorId} className="mt-1 text-xs font-medium text-red-600">
          {error}
        </p>
      )}
    </div>
  )
}
