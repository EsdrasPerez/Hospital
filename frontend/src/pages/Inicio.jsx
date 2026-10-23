import { Link } from 'react-router-dom'
import { Card, CardBody, Col, Row } from 'reactstrap'
import { useAuth } from '../auth/useAuth.js'
import { menuParaRol } from '../config/menu.js'
import { ETIQUETAS_ROL } from '../utils/formato.js'

export default function Inicio() {
  const { usuario, rol } = useAuth()
  const modulos = menuParaRol(rol).flatMap((g) => g.items)

  return (
    <div>
      <Card>
        <CardBody>
          <h3 className="mb-1">Bienvenido, {usuario?.username}</h3>
          <p className="text-muted mb-0">Perfil: {ETIQUETAS_ROL[rol] ?? rol}</p>
        </CardBody>
      </Card>

      {modulos.length > 0 ? (
        <Row>
          {modulos.map((m) => (
            <Col key={m.clave} xs="12" sm="6" xl="4">
              <Link to={m.ruta} className="tarjeta-modulo">
                <Card>
                  <CardBody>
                    <div className="d-flex align-items-center">
                      <div className="mr-3 text-primary" style={{ fontSize: '2rem', lineHeight: 1 }}>
                        <i className={`now-ui-icons ${m.icono ?? 'design_app'}`} />
                      </div>
                      <div className="tarjeta-modulo-texto">
                        <h5 className="mb-0">{m.titulo}</h5>
                        <small className="text-muted">Abrir módulo</small>
                      </div>
                    </div>
                  </CardBody>
                </Card>
              </Link>
            </Col>
          ))}
        </Row>
      ) : (
        <Card>
          <CardBody className="text-muted">Aún no hay módulos disponibles para su perfil en esta versión. Se habilitarán en próximas fases.</CardBody>
        </Card>
      )}
    </div>
  )
}
