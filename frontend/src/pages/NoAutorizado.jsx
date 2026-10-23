import { Link } from 'react-router-dom'
import { Card, CardBody } from 'reactstrap'

export default function NoAutorizado() {
  return (
    <Card>
      <CardBody className="text-center py-5">
        <p className="display-3 text-muted mb-0">403</p>
        <h4 className="mt-2">Acceso no autorizado</h4>
        <p className="text-muted">Su perfil no tiene permisos para acceder a esta sección.</p>
        <Link to="/" className="btn btn-primary btn-round mt-2">
          Volver al inicio
        </Link>
      </CardBody>
    </Card>
  )
}
