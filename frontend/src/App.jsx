import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './auth/ProtectedRoute.jsx'
import RoleRoute from './auth/RoleRoute.jsx'
import { obtenerModulo } from './config/menu.js'
import AuthLayout from './layouts/AuthLayout.jsx'
import MainLayout from './layouts/MainLayout.jsx'
import Inicio from './pages/Inicio.jsx'
import Login from './pages/Login.jsx'
import NoAutorizado from './pages/NoAutorizado.jsx'
import NoEncontrado from './pages/NoEncontrado.jsx'
import PacienteDetalle from './pages/pacientes/PacienteDetalle.jsx'
import PacienteForm from './pages/pacientes/PacienteForm.jsx'
import PacientesLista from './pages/pacientes/PacientesLista.jsx'

const permisosPacientes = obtenerModulo('pacientes').permisos

export default function App() {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<Login />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route index element={<Inicio />} />

          <Route element={<RoleRoute roles={permisosPacientes.ver} />}>
            <Route path="/pacientes" element={<PacientesLista />} />
            <Route path="/pacientes/:id" element={<PacienteDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosPacientes.crear} />}>
            <Route path="/pacientes/nuevo" element={<PacienteForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosPacientes.editar} />}>
            <Route path="/pacientes/:id/editar" element={<PacienteForm />} />
          </Route>

          <Route path="/no-autorizado" element={<NoAutorizado />} />
          <Route path="/inicio" element={<Navigate to="/" replace />} />
          <Route path="*" element={<NoEncontrado />} />
        </Route>
      </Route>
    </Routes>
  )
}
