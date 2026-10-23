// El backend guarda el estado de la cuenta como texto libre (VARCHAR(20)); estos son los valores que usa el servicio.
export const ESTADOS_CUENTA = ['Abierta', 'Cerrada', 'Liquidada']

const TONOS = { abierta: 'azul', cerrada: 'gris', liquidada: 'verde' }
export const tonoEstadoCuenta = (estado) => TONOS[String(estado ?? '').toLowerCase()] ?? 'gris'
