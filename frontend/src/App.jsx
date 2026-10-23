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
import CitaDetalle from './pages/citas/CitaDetalle.jsx'
import CitaForm from './pages/citas/CitaForm.jsx'
import CitasLista from './pages/citas/CitasLista.jsx'
import ConsultaDetalle from './pages/consultas/ConsultaDetalle.jsx'
import ConsultaForm from './pages/consultas/ConsultaForm.jsx'
import ConsultasLista from './pages/consultas/ConsultasLista.jsx'
import DiagnosticoDetalle from './pages/diagnosticos/DiagnosticoDetalle.jsx'
import DiagnosticoForm from './pages/diagnosticos/DiagnosticoForm.jsx'
import DiagnosticosLista from './pages/diagnosticos/DiagnosticosLista.jsx'
import TratamientoDetalle from './pages/tratamientos/TratamientoDetalle.jsx'
import TratamientoForm from './pages/tratamientos/TratamientoForm.jsx'
import TratamientosLista from './pages/tratamientos/TratamientosLista.jsx'
import HabitacionDetalle from './pages/habitaciones/HabitacionDetalle.jsx'
import HabitacionForm from './pages/habitaciones/HabitacionForm.jsx'
import HabitacionesLista from './pages/habitaciones/HabitacionesLista.jsx'
import HospitalizacionDetalle from './pages/hospitalizaciones/HospitalizacionDetalle.jsx'
import HospitalizacionForm from './pages/hospitalizaciones/HospitalizacionForm.jsx'
import HospitalizacionesLista from './pages/hospitalizaciones/HospitalizacionesLista.jsx'
import MedicamentoDetalle from './pages/medicamentos/MedicamentoDetalle.jsx'
import MedicamentoForm from './pages/medicamentos/MedicamentoForm.jsx'
import MedicamentosLista from './pages/medicamentos/MedicamentosLista.jsx'
import RecetasLista from './pages/recetas/RecetasLista.jsx'
import CuentaDetalle from './pages/cuentas/CuentaDetalle.jsx'
import CuentaForm from './pages/cuentas/CuentaForm.jsx'
import CuentasLista from './pages/cuentas/CuentasLista.jsx'
import FacturaDetalle from './pages/facturas/FacturaDetalle.jsx'
import FacturaForm from './pages/facturas/FacturaForm.jsx'
import FacturasLista from './pages/facturas/FacturasLista.jsx'
import PagosLista from './pages/pagos/PagosLista.jsx'
import EmergenciaDetalle from './pages/emergencias/EmergenciaDetalle.jsx'
import EmergenciaForm from './pages/emergencias/EmergenciaForm.jsx'
import EmergenciasLista from './pages/emergencias/EmergenciasLista.jsx'
import ProcedimientoForm from './pages/procedimientos/ProcedimientoForm.jsx'
import ProcedimientosLista from './pages/procedimientos/ProcedimientosLista.jsx'
import TriajeForm from './pages/triaje/TriajeForm.jsx'
import TriajeLista from './pages/triaje/TriajeLista.jsx'
import PacienteDetalle from './pages/pacientes/PacienteDetalle.jsx'
import PacienteForm from './pages/pacientes/PacienteForm.jsx'
import PacientesLista from './pages/pacientes/PacientesLista.jsx'

const permisosPacientes = obtenerModulo('pacientes').permisos
const permisosEmergencia = obtenerModulo('emergencia').permisos
const permisosMedicamentos = obtenerModulo('medicamentos').permisos
const permisosRecetas = obtenerModulo('recetas').permisos
const permisosHospitalizaciones = obtenerModulo('hospitalizaciones').permisos
const permisosHabitaciones = obtenerModulo('habitaciones').permisos
const permisosTriaje = obtenerModulo('triaje').permisos
const permisosProcedimientos = obtenerModulo('procedimientos').permisos
const permisosCitas = obtenerModulo('citas').permisos
const permisosConsultas = obtenerModulo('consultas').permisos
const permisosDiagnosticos = obtenerModulo('diagnosticos').permisos
const permisosTratamientos = obtenerModulo('tratamientos').permisos
const permisosFacturas = obtenerModulo('facturas').permisos
const permisosCuentas = obtenerModulo('cuentas').permisos
const permisosPagos = obtenerModulo('pagos').permisos

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

          <Route element={<RoleRoute roles={permisosCitas.ver} />}>
            <Route path="/citas" element={<CitasLista />} />
            <Route path="/citas/:id" element={<CitaDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosCitas.crear} />}>
            <Route path="/citas/nueva" element={<CitaForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosCitas.editar} />}>
            <Route path="/citas/:id/editar" element={<CitaForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosConsultas.ver} />}>
            <Route path="/consultas" element={<ConsultasLista />} />
            <Route path="/consultas/:id" element={<ConsultaDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosConsultas.crear} />}>
            <Route path="/consultas/nueva" element={<ConsultaForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosConsultas.editar} />}>
            <Route path="/consultas/:id/editar" element={<ConsultaForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosDiagnosticos.ver} />}>
            <Route path="/diagnosticos" element={<DiagnosticosLista />} />
            <Route path="/diagnosticos/:id" element={<DiagnosticoDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosDiagnosticos.crear} />}>
            <Route path="/diagnosticos/nuevo" element={<DiagnosticoForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosDiagnosticos.editar} />}>
            <Route path="/diagnosticos/:id/editar" element={<DiagnosticoForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosTratamientos.ver} />}>
            <Route path="/tratamientos" element={<TratamientosLista />} />
            <Route path="/tratamientos/:id" element={<TratamientoDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosTratamientos.crear} />}>
            <Route path="/tratamientos/nuevo" element={<TratamientoForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosTratamientos.editar} />}>
            <Route path="/tratamientos/:id/editar" element={<TratamientoForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosEmergencia.ver} />}>
            <Route path="/emergencia" element={<EmergenciasLista />} />
            <Route path="/emergencia/:id" element={<EmergenciaDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosEmergencia.crear} />}>
            <Route path="/emergencia/nueva" element={<EmergenciaForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosEmergencia.editar} />}>
            <Route path="/emergencia/:id/editar" element={<EmergenciaForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosTriaje.ver} />}>
            <Route path="/triaje" element={<TriajeLista />} />
          </Route>
          <Route element={<RoleRoute roles={permisosTriaje.crear} />}>
            <Route path="/triaje/nuevo" element={<TriajeForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosTriaje.editar} />}>
            <Route path="/triaje/:id/editar" element={<TriajeForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosProcedimientos.ver} />}>
            <Route path="/procedimientos" element={<ProcedimientosLista />} />
          </Route>
          <Route element={<RoleRoute roles={permisosProcedimientos.crear} />}>
            <Route path="/procedimientos/nuevo" element={<ProcedimientoForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosProcedimientos.editar} />}>
            <Route path="/procedimientos/:id/editar" element={<ProcedimientoForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosHospitalizaciones.ver} />}>
            <Route path="/hospitalizaciones" element={<HospitalizacionesLista />} />
            <Route path="/hospitalizaciones/:id" element={<HospitalizacionDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosHospitalizaciones.crear} />}>
            <Route path="/hospitalizaciones/nueva" element={<HospitalizacionForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosHospitalizaciones.editar} />}>
            <Route path="/hospitalizaciones/:id/editar" element={<HospitalizacionForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosHabitaciones.ver} />}>
            <Route path="/habitaciones" element={<HabitacionesLista />} />
            <Route path="/habitaciones/:id" element={<HabitacionDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosHabitaciones.crear} />}>
            <Route path="/habitaciones/nueva" element={<HabitacionForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosHabitaciones.editar} />}>
            <Route path="/habitaciones/:id/editar" element={<HabitacionForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosMedicamentos.ver} />}>
            <Route path="/medicamentos" element={<MedicamentosLista />} />
            <Route path="/medicamentos/:id" element={<MedicamentoDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosMedicamentos.crear} />}>
            <Route path="/medicamentos/nuevo" element={<MedicamentoForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosMedicamentos.editar} />}>
            <Route path="/medicamentos/:id/editar" element={<MedicamentoForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosRecetas.ver} />}>
            <Route path="/recetas" element={<RecetasLista />} />
          </Route>

          <Route element={<RoleRoute roles={permisosFacturas.ver} />}>
            <Route path="/facturas" element={<FacturasLista />} />
            <Route path="/facturas/:id" element={<FacturaDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosFacturas.crear} />}>
            <Route path="/facturas/nueva" element={<FacturaForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosFacturas.editar} />}>
            <Route path="/facturas/:id/editar" element={<FacturaForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosCuentas.ver} />}>
            <Route path="/cuentas" element={<CuentasLista />} />
            <Route path="/cuentas/:id" element={<CuentaDetalle />} />
          </Route>
          <Route element={<RoleRoute roles={permisosCuentas.crear} />}>
            <Route path="/cuentas/nueva" element={<CuentaForm />} />
          </Route>
          <Route element={<RoleRoute roles={permisosCuentas.editar} />}>
            <Route path="/cuentas/:id/editar" element={<CuentaForm />} />
          </Route>

          <Route element={<RoleRoute roles={permisosPagos.ver} />}>
            <Route path="/pagos" element={<PagosLista />} />
          </Route>

          <Route path="/no-autorizado" element={<NoAutorizado />} />
          <Route path="/inicio" element={<Navigate to="/" replace />} />
          <Route path="*" element={<NoEncontrado />} />
        </Route>
      </Route>
    </Routes>
  )
}
