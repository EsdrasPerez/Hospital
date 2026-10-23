package com.example.hospital.reportes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.citas.CitasRepository;
import com.example.hospital.citas.CitasService;
import com.example.hospital.citas.dto.CitasDTO;
import com.example.hospital.consultas.Consultas;
import com.example.hospital.consultas.ConsultasRepository;
import com.example.hospital.cuentas.CuentasService;
import com.example.hospital.cuentas.dto.CuentasDTO;
import com.example.hospital.diagnosticoconsulta.DiagnosticoConsulta;
import com.example.hospital.diagnosticoconsulta.DiagnosticoConsultaRepository;
import com.example.hospital.diagnosticos.Diagnosticos;
import com.example.hospital.diagnosticos.DiagnosticosRepository;
import com.example.hospital.emergencia.Emergencia;
import com.example.hospital.emergencia.EmergenciaRepository;
import com.example.hospital.emergencia.EmergenciaService;
import com.example.hospital.facturas.FacturasRepository;
import com.example.hospital.facturas.FacturasService;
import com.example.hospital.facturas.dto.FacturasDTO;
import com.example.hospital.habitacion.HabitacionRepository;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.hospitalizaciones.HospitalizacionesService;
import com.example.hospital.hospitalizaciones.dto.HospitalizacionesDTO;
import com.example.hospital.medicamentos.MedicamentosService;
import com.example.hospital.medicamentos.dto.MedicamentosDTO;
import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.paciente.PacienteService;
import com.example.hospital.paciente.dto.PacienteResponse;
import com.example.hospital.reportes.dto.HistorialPacienteDTO;
import com.example.hospital.reportes.dto.ResumenDashboardDTO;
import com.example.hospital.reportes.impl.ReportesServiceImpl;
import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.tratamiento.Tratamiento;
import com.example.hospital.tratamiento.TratamientoRepository;
import com.example.hospital.tratamientomedicamento.TratamientoMedicamento;
import com.example.hospital.tratamientomedicamento.TratamientoMedicamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportesServiceImplTest {

    @Mock PacienteRepository pacienteRepository;
    @Mock CitasRepository citasRepository;
    @Mock HospitalizacionesRepository hospitalizacionesRepository;
    @Mock EmergenciaRepository emergenciaRepository;
    @Mock HabitacionRepository habitacionRepository;
    @Mock FacturasRepository facturasRepository;
    @Mock MedicamentosService medicamentosService;
    @Mock PacienteService pacienteService;
    @Mock CitasService citasService;
    @Mock HospitalizacionesService hospitalizacionesService;
    @Mock EmergenciaService emergenciaService;
    @Mock CuentasService cuentasService;
    @Mock FacturasService facturasService;
    @Mock EstadosCatalogo estados;
    @Mock ConsultasRepository consultasRepository;
    @Mock DiagnosticoConsultaRepository diagnosticoConsultaRepository;
    @Mock DiagnosticosRepository diagnosticosRepository;
    @Mock TratamientoRepository tratamientoRepository;
    @Mock TratamientoMedicamentoRepository tratamientoMedicamentoRepository;
    @InjectMocks ReportesServiceImpl service;

    @BeforeEach
    void catalogo() {
        lenient().when(estados.hospActiva()).thenReturn(1L);
        lenient().when(estados.habDisponible()).thenReturn(2L);
        lenient().when(estados.facturaPendiente()).thenReturn(3L);
    }

    private PacienteResponse paciente(boolean activo) {
        return new PacienteResponse(1L, "123", "Ana", "Lopez", null, null, "F", null, null, null, null, null, null, activo);
    }

    // ---------- resumen ----------

    @Test
    void resumen_cuentaSoloRegistrosActivos_yResuelveLosEstadosPorNombre() {
        when(pacienteRepository.countByActivoTrue()).thenReturn(4L);
        when(citasRepository.countByFechaAndActivoTrue(any(LocalDate.class))).thenReturn(5L);
        when(hospitalizacionesRepository.countByIdEstadoHospAndActivoTrue(1L)).thenReturn(2L);
        when(emergenciaRepository.countByActivoTrueAndFechaHoraAltaIsNull()).thenReturn(1L);
        when(habitacionRepository.countByIdEstadoHabitacionAndActivoTrue(2L)).thenReturn(7L);
        when(medicamentosService.findBajoStock()).thenReturn(List.of(
                new MedicamentosDTO(1L, "A", null, null, null, 0, 5, BigDecimal.ONE, false, true),
                new MedicamentosDTO(2L, "B", null, null, null, 1, 5, BigDecimal.ONE, false, true)));
        when(facturasRepository.countByIdEstadoFacturaAndActivoTrue(3L)).thenReturn(9L);

        ResumenDashboardDTO r = service.resumen();

        assertThat(r.fecha()).isEqualTo(LocalDate.now());
        assertThat(r.pacientesActivos()).isEqualTo(4);
        assertThat(r.citasDelDia()).isEqualTo(5);
        assertThat(r.hospitalizacionesActivas()).isEqualTo(2);
        assertThat(r.emergenciasEnCurso()).isEqualTo(1);
        assertThat(r.habitacionesDisponibles()).isEqualTo(7);
        assertThat(r.medicamentosBajoStock()).isEqualTo(2);
        assertThat(r.facturasPendientes()).isEqualTo(9);
        // el conteo de hospitalizaciones ya no incluye las anuladas
        verify(hospitalizacionesRepository, never()).countByIdEstadoHosp(any());
    }

    // ---------- historial: datos financieros según rol ----------

    private void stubBase() {
        when(pacienteService.obtenerDetalle(1L)).thenReturn(paciente(true));
        when(citasService.findByPaciente(1L)).thenReturn(List.of());
        when(hospitalizacionesService.findByPaciente(1L)).thenReturn(List.of());
        when(emergenciaRepository.findByIdPaciente(1L)).thenReturn(List.of());
    }

    @Test
    void historial_conFinanzas_incluyeCuentasYFacturas() {
        stubBase();
        when(cuentasService.buscar(1L, null, null, false)).thenReturn(List.of(new CuentasDTO(1L, 1L, null, null, "Abierta", BigDecimal.TEN, true)));
        when(facturasService.buscar(1L, null, null, null, null, false)).thenReturn(List.of(
                new FacturasDTO(2L, 1L, null, null, null, null, null, 1L, 1L, true)));

        HistorialPacienteDTO h = service.historialPaciente(1L, false, true);

        assertThat(h.incluyeDatosFinancieros()).isTrue();
        assertThat(h.cuentas()).hasSize(1);
        assertThat(h.facturas()).hasSize(1);
    }

    @Test
    void historial_sinFinanzas_medicoOEnfermero_omiteCuentasYFacturas_sinConsultarlas() {
        stubBase();

        HistorialPacienteDTO h = service.historialPaciente(1L, false, false);

        assertThat(h.incluyeDatosFinancieros()).isFalse();
        assertThat(h.cuentas()).isEmpty();
        assertThat(h.facturas()).isEmpty();
        verify(cuentasService, never()).buscar(any(), any(), any(), org.mockito.ArgumentMatchers.anyBoolean());
        verify(facturasService, never()).buscar(any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    // ---------- historial clínico ----------

    private Consultas consulta(Long id, Long idCita, boolean activo) {
        Consultas c = new Consultas();
        c.setIdConsulta(id);
        c.setIdCita(idCita);
        c.setFechaHora(LocalDateTime.of(2026, 10, 20, 10, 0));
        c.setActivo(activo);
        return c;
    }

    private Diagnosticos diagnostico(Long id, boolean activo) {
        Diagnosticos d = new Diagnosticos();
        d.setIdDiagnostico(id);
        d.setDescripcion("Dx " + id);
        d.setActivo(activo);
        return d;
    }

    private Tratamiento tratamiento(Long id, Long idDx, boolean activo) {
        Tratamiento t = new Tratamiento();
        t.setIdTratamiento(id);
        t.setIdDiagnostico(idDx);
        t.setDescripcion("Trat " + id);
        t.setActivo(activo);
        return t;
    }

    private DiagnosticoConsulta relacion(Long idDx, Long idConsulta) {
        DiagnosticoConsulta dc = new DiagnosticoConsulta();
        dc.setIdDiagnostico(idDx);
        dc.setIdConsulta(idConsulta);
        return dc;
    }

    private void stubCadenaClinica() {
        when(pacienteService.obtenerDetalle(1L)).thenReturn(paciente(true));
        when(citasService.findByPaciente(1L)).thenReturn(List.of(
                new CitasDTO(100L, LocalDate.of(2026, 10, 20), null, "Control", null, 1L, 1L, 1L, true),
                new CitasDTO(101L, LocalDate.of(2026, 10, 21), null, "Anulada", null, 1L, 1L, 1L, false)));
        when(hospitalizacionesService.findByPaciente(1L)).thenReturn(List.of(
                new HospitalizacionesDTO(1L, 1L, 1L, 1L, null, null, null, "m", null, 1L, true),
                new HospitalizacionesDTO(2L, 1L, 1L, 1L, null, null, null, "m", null, 1L, false)));
        Emergencia e1 = new Emergencia();
        e1.setIdEmergencia(1L);
        e1.setActivo(true);
        Emergencia e2 = new Emergencia();
        e2.setIdEmergencia(2L);
        e2.setActivo(false);
        when(emergenciaRepository.findByIdPaciente(1L)).thenReturn(List.of(e1, e2));
        lenient().when(consultasRepository.findByIdCita(100L)).thenReturn(Optional.of(consulta(10L, 100L, true)));
        lenient().when(consultasRepository.findByIdCita(101L)).thenReturn(Optional.of(consulta(11L, 101L, false)));
        lenient().when(diagnosticoConsultaRepository.findByIdConsulta(10L)).thenReturn(List.of(relacion(5L, 10L), relacion(6L, 10L)));
        lenient().when(diagnosticoConsultaRepository.findByIdConsulta(11L)).thenReturn(List.of(relacion(5L, 11L)));
        lenient().when(diagnosticosRepository.findById(5L)).thenReturn(Optional.of(diagnostico(5L, true)));
        lenient().when(diagnosticosRepository.findById(6L)).thenReturn(Optional.of(diagnostico(6L, false)));
        lenient().when(tratamientoRepository.findByIdDiagnostico(5L)).thenReturn(List.of(tratamiento(7L, 5L, true), tratamiento(8L, 5L, false)));
        lenient().when(tratamientoRepository.findByIdDiagnostico(6L)).thenReturn(List.of(tratamiento(9L, 6L, true)));
        TratamientoMedicamento tm = new TratamientoMedicamento(3L, 7L);
        lenient().when(tratamientoMedicamentoRepository.findByIdTratamiento(7L)).thenReturn(List.of(tm));
        lenient().when(tratamientoMedicamentoRepository.findByIdTratamiento(8L)).thenReturn(List.of());
        lenient().when(tratamientoMedicamentoRepository.findByIdTratamiento(9L)).thenReturn(List.of());
    }

    @Test
    void historial_incluyeConsultasDiagnosticosTratamientosYMedicamentos_yFiltraAnuladosPorDefecto() {
        stubCadenaClinica();

        HistorialPacienteDTO h = service.historialPaciente(1L, false, false);

        assertThat(h.incluirAnulados()).isFalse();
        assertThat(h.citas()).extracting(CitasDTO::idCita).containsExactly(100L);
        assertThat(h.hospitalizaciones()).extracting(HospitalizacionesDTO::idHospitalizacion).containsExactly(1L);
        assertThat(h.emergencias()).extracting(e -> e.idEmergencia()).containsExactly(1L);
        assertThat(h.consultas()).extracting(c -> c.idConsulta()).containsExactly(10L);
        assertThat(h.diagnosticos()).extracting(d -> d.idDiagnostico()).containsExactly(5L);
        assertThat(h.diagnosticosConsulta()).extracting(r -> r.idDiagnostico()).containsExactly(5L);
        assertThat(h.tratamientos()).extracting(t -> t.idTratamiento()).containsExactly(7L);
        assertThat(h.medicamentosTratamiento()).extracting(m -> m.idMedicamento()).containsExactly(3L);
    }

    @Test
    void historial_conIncluirAnulados_devuelveTodasLasSeccionesConElMismoCriterio() {
        stubCadenaClinica();

        HistorialPacienteDTO h = service.historialPaciente(1L, true, false);

        assertThat(h.incluirAnulados()).isTrue();
        assertThat(h.citas()).extracting(CitasDTO::idCita).containsExactly(100L, 101L);
        assertThat(h.hospitalizaciones()).hasSize(2);
        assertThat(h.emergencias()).hasSize(2);
        assertThat(h.consultas()).extracting(c -> c.idConsulta()).containsExactly(10L, 11L);
        assertThat(h.diagnosticos()).extracting(d -> d.idDiagnostico()).containsExactly(5L, 6L);
        assertThat(h.tratamientos()).extracting(t -> t.idTratamiento()).containsExactly(7L, 8L, 9L);
    }

    // ---------- paciente inactivo / inexistente ----------

    @Test
    void historial_deUnPacienteInactivo_seDevuelveConActivoFalse() {
        stubBase();
        when(pacienteService.obtenerDetalle(1L)).thenReturn(paciente(false));

        HistorialPacienteDTO h = service.historialPaciente(1L, false, false);

        assertThat(h.paciente().activo()).isFalse();
        verify(pacienteService, never()).obtener(any());
    }

    @Test
    void historial_lanza404_siElPacienteNoExiste() {
        when(pacienteService.obtenerDetalle(99L)).thenThrow(new RecursoNoEncontradoException("Paciente 99 no encontrado"));

        assertThrows(RecursoNoEncontradoException.class, () -> service.historialPaciente(99L, false, true));
    }
}
