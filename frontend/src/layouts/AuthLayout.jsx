import { Outlet } from 'react-router-dom'
import { Card, CardBody, Col, Row } from 'reactstrap'

export default function AuthLayout() {
  return (
    <div className="login-fondo">
      <div className="login-contenido">
        <div className="text-center text-white mb-4">
          <svg viewBox="0 0 24 24" width="44" height="44" fill="currentColor" aria-hidden="true">
            <path d="M10 3h4v7h7v4h-7v7h-4v-7H3v-4h7z" />
          </svg>
          <h2 className="mb-0 mt-2">Hospital</h2>
          <p className="mb-0">Sistema de gestión hospitalaria</p>
        </div>
        <Row className="w-100 justify-content-center">
          <Col xs="12" sm="9" md="6" lg="4">
            <Card className="card-login mb-0">
              <CardBody>
                <Outlet />
              </CardBody>
            </Card>
          </Col>
        </Row>
      </div>
    </div>
  )
}
