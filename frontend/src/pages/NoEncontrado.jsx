import { Link } from 'react-router-dom'
import { Card, CardBody } from 'reactstrap'

export default function NoEncontrado() {
  return (
    <Card>
      <CardBody className="text-center py-5">
        <p className="display-3 text-muted mb-0">404</p>
        <h4 className="mt-2">Página no encontrada</h4>
        <p className="text-muted">La dirección que busca no existe o fue movida.</p>
        <Link to="/" className="btn btn-primary btn-round mt-2">
          Volver al inicio
        </Link>
      </CardBody>
    </Card>
  )
}
