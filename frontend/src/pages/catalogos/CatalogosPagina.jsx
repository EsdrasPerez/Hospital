import CatalogoCrud from '../../components/CatalogoCrud.jsx'
import Pestanas from '../../components/Pestanas.jsx'
import { useAuth } from '../../auth/useAuth.js'
import { CATALOGOS } from '../../config/catalogos.js'
import EstadoCitaTransiciones from './EstadoCitaTransiciones.jsx'

/**
 * Catálogos simples del sistema (alergias, tipos, estados, métodos de pago, niveles de urgencia...). Cada rol ve solo los
 * catálogos que puede modificar (ADMIN todos; MEDICO solo alergias), según los @PreAuthorize del backend.
 */
export default function CatalogosPagina() {
  const { rol } = useAuth()
  const visibles = CATALOGOS.filter((c) => c.escribir.includes(rol))

  return (
    <div>
      <div className="card card-body">
        <h1 className="h3 mb-0">Catálogos</h1>
        <p className="text-muted mb-0">Valores de referencia usados por el resto de los módulos.</p>
      </div>
      <div className="card card-body pestanas-contenedor">
        <Pestanas
          etiqueta="Catálogos"
          pestanas={visibles.map((c) => ({
            clave: c.clave,
            titulo: c.titulo,
            contenido: <CatalogoCrud config={c} extra={c.transiciones ? (estados) => <EstadoCitaTransiciones estados={estados} /> : undefined} />,
          }))}
        />
      </div>
    </div>
  )
}
