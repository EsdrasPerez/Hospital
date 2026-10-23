package com.example.hospital.reportes.impl;

import java.util.List;
import com.example.hospital.tratamientomedicamento.dto.TratamientoMedicamentoDTO;
import com.example.hospital.tratamientomedicamento.TratamientoMedicamentoRepository;
import com.example.hospital.tratamiento.dto.TratamientoDTO;
import com.example.hospital.tratamiento.TratamientoRepository;
import com.example.hospital.tratamiento.Tratamiento;
import com.example.hospital.hospitalizaciones.dto.HospitalizacionesDTO;
import com.example.hospital.facturas.dto.FacturasDTO;
import com.example.hospital.emergencia.dto.EmergenciaDTO;
import com.example.hospital.emergencia.Emergencia;
import com.example.hospital.diagnosticos.dto.DiagnosticosDTO;
import com.example.hospital.diagnosticos.DiagnosticosRepository;
import com.example.hospital.diagnosticos.Diagnosticos;
import com.example.hospital.diagnosticoconsulta.dto.DiagnosticoConsultaDTO;
import com.example.hospital.diagnosticoconsulta.DiagnosticoConsultaRepository;
import com.example.hospital.diagnosticoconsulta.DiagnosticoConsulta;
import com.example.hospital.cuentas.dto.CuentasDTO;
import com.example.hospital.consultas.dto.ConsultasDTO;
import com.example.hospital.consultas.ConsultasRepository;
import com.example.hospital.consultas.Consultas;
import com.example.hospital.citas.dto.CitasDTO;
import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.citas.CitasRepository;
import com.example.hospital.citas.CitasService;
import com.example.hospital.cuentas.CuentasService;
import com.example.hospital.emergencia.EmergenciaRepository;
import com.example.hospital.emergencia.EmergenciaService;
import com.example.hospital.facturas.FacturasRepository;
import com.example.hospital.facturas.FacturasService;
import com.example.hospital.habitacion.HabitacionRepository;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.hospitalizaciones.HospitalizacionesService;
import com.example.hospital.medicamentos.MedicamentosService;
import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.paciente.PacienteService;
import com.example.hospital.paciente.dto.PacienteResponse;
import com.example.hospital.reportes.ReportesService;
import com.example.hospital.reportes.dto.HistorialPacienteDTO;
import com.example.hospital.reportes.dto.ResumenDashboardDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ReportesServiceImpl implements ReportesService {

    // Los estados (hospitalización Activa, habitación Disponible, factura Pendiente) se resuelven por nombre, no por id fijo
    private final EstadosCatalogo estados;

    private final PacienteRepository pacienteRepository;
    private final CitasRepository citasRepository;
    private final HospitalizacionesRepository hospitalizacionesRepository;
    private final EmergenciaRepository emergenciaRepository;
    private final HabitacionRepository habitacionRepository;
    private final FacturasRepository facturasRepository;
    private final MedicamentosService medicamentosService;

    private final PacienteService pacienteService;
    private final CitasService citasService;
    private final HospitalizacionesService hospitalizacionesService;
    private final EmergenciaService emergenciaService;
    private final CuentasService cuentasService;
    private final FacturasService facturasService;
    private final ConsultasRepository consultasRepository;
    private final DiagnosticoConsultaRepository diagnosticoConsultaRepository;
    private final DiagnosticosRepository diagnosticosRepository;
    private final TratamientoRepository tratamientoRepository;
    private final TratamientoMedicamentoRepository tratamientoMedicamentoRepository;

    public ReportesServiceImpl(PacienteRepository pacienteRepository,
                               CitasRepository citasRepository,
                               HospitalizacionesRepository hospitalizacionesRepository,
                               EmergenciaRepository emergenciaRepository,
                               HabitacionRepository habitacionRepository,
                               FacturasRepository facturasRepository,
                               MedicamentosService medicamentosService,
                               PacienteService pacienteService,
                               CitasService citasService,
                               HospitalizacionesService hospitalizacionesService,
                               EmergenciaService emergenciaService,
                               CuentasService cuentasService,
                               FacturasService facturasService,
                               EstadosCatalogo estados,
                               ConsultasRepository consultasRepository,
                               DiagnosticoConsultaRepository diagnosticoConsultaRepository,
                               DiagnosticosRepository diagnosticosRepository,
                               TratamientoRepository tratamientoRepository,
                               TratamientoMedicamentoRepository tratamientoMedicamentoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.citasRepository = citasRepository;
        this.hospitalizacionesRepository = hospitalizacionesRepository;
        this.emergenciaRepository = emergenciaRepository;
        this.habitacionRepository = habitacionRepository;
        this.facturasRepository = facturasRepository;
        this.medicamentosService = medicamentosService;
        this.pacienteService = pacienteService;
        this.citasService = citasService;
        this.hospitalizacionesService = hospitalizacionesService;
        this.emergenciaService = emergenciaService;
        this.cuentasService = cuentasService;
        this.facturasService = facturasService;
        this.estados = estados;
        this.consultasRepository = consultasRepository;
        this.diagnosticoConsultaRepository = diagnosticoConsultaRepository;
        this.diagnosticosRepository = diagnosticosRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.tratamientoMedicamentoRepository = tratamientoMedicamentoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenDashboardDTO resumen() {
        LocalDate hoy = LocalDate.now();
        return new ResumenDashboardDTO(
                hoy,
                pacienteRepository.countByActivoTrue(),
                citasRepository.countByFechaAndActivoTrue(hoy),
                hospitalizacionesRepository.countByIdEstadoHospAndActivoTrue(estados.hospActiva()),
                emergenciaRepository.countByActivoTrueAndFechaHoraAltaIsNull(),
                habitacionRepository.countByIdEstadoHabitacionAndActivoTrue(estados.habDisponible()),
                medicamentosService.findBajoStock().size(),
                facturasRepository.countByIdEstadoFacturaAndActivoTrue(estados.facturaPendiente())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialPacienteDTO historialPaciente(Long idPaciente, boolean incluirAnulados, boolean incluirFinanzas) {
        // Es un histórico: se admite un paciente inactivo (404 solo si no existe)
        PacienteResponse paciente = pacienteService.obtenerDetalle(idPaciente);

        List<CitasDTO> citas = filtrar(citasService.findByPaciente(idPaciente), CitasDTO::activo, incluirAnulados);
        List<HospitalizacionesDTO> hospitalizaciones = filtrar(hospitalizacionesService.findByPaciente(idPaciente), HospitalizacionesDTO::activo, incluirAnulados);
        List<EmergenciaDTO> emergencias = filtrar(emergenciaRepository.findByIdPaciente(idPaciente).stream().map(this::aDTO).toList(),
                EmergenciaDTO::activo, incluirAnulados);

        // Datos financieros solo para ADMIN/CAJERO (minimización de datos): para los demás roles van vacíos
        List<CuentasDTO> cuentas = incluirFinanzas
                ? filtrar(cuentasService.buscar(idPaciente, null, null, false), CuentasDTO::activo, incluirAnulados) : List.of();
        List<FacturasDTO> facturas = incluirFinanzas
                ? filtrar(facturasService.buscar(idPaciente, null, null, null, null, false), FacturasDTO::activo, incluirAnulados) : List.of();

        // Cadena clínica: citas -> consultas -> diagnósticos -> tratamientos -> medicamentos
        List<ConsultasDTO> consultas = new java.util.ArrayList<>();
        for (CitasDTO cita : citas) {
            consultasRepository.findByIdCita(cita.idCita()).map(this::aDTO).ifPresent(consultas::add);
        }
        List<ConsultasDTO> consultasVisibles = filtrar(consultas, ConsultasDTO::activo, incluirAnulados);

        List<DiagnosticoConsultaDTO> relaciones = new java.util.ArrayList<>();
        java.util.Map<Long, DiagnosticosDTO> diagnosticos = new java.util.LinkedHashMap<>();
        for (ConsultasDTO c : consultasVisibles) {
            for (DiagnosticoConsulta dc : diagnosticoConsultaRepository.findByIdConsulta(c.idConsulta())) {
                diagnosticosRepository.findById(dc.getIdDiagnostico()).map(this::aDTO)
                        .filter(d -> incluirAnulados || !Boolean.FALSE.equals(d.activo()))
                        .ifPresent(d -> {
                            relaciones.add(new DiagnosticoConsultaDTO(dc.getIdDiagnostico(), dc.getIdConsulta()));
                            diagnosticos.putIfAbsent(d.idDiagnostico(), d);
                        });
            }
        }
        List<TratamientoDTO> tratamientos = new java.util.ArrayList<>();
        for (Long idDiagnostico : diagnosticos.keySet()) {
            tratamientoRepository.findByIdDiagnostico(idDiagnostico).stream().map(this::aDTO)
                    .filter(t -> incluirAnulados || !Boolean.FALSE.equals(t.activo()))
                    .forEach(tratamientos::add);
        }
        List<TratamientoMedicamentoDTO> medicamentos = new java.util.ArrayList<>();
        for (TratamientoDTO t : tratamientos) {
            tratamientoMedicamentoRepository.findByIdTratamiento(t.idTratamiento())
                    .forEach(tm -> medicamentos.add(new TratamientoMedicamentoDTO(tm.getIdMedicamento(), tm.getIdTratamiento())));
        }

        return new HistorialPacienteDTO(paciente, citas, hospitalizaciones, emergencias, cuentas, facturas,
                consultasVisibles, relaciones, new java.util.ArrayList<>(diagnosticos.values()), tratamientos, medicamentos,
                incluirFinanzas, incluirAnulados);
    }

    /** Mismo criterio en todas las secciones: se omiten los registros anulados (activo=false) salvo `incluirAnulados`. */
    private <T> List<T> filtrar(List<T> lista, java.util.function.Function<T, Boolean> activo, boolean incluirAnulados) {
        return lista.stream().filter(x -> incluirAnulados || !Boolean.FALSE.equals(activo.apply(x))).toList();
    }

    private EmergenciaDTO aDTO(Emergencia e) {
        return new EmergenciaDTO(e.getIdEmergencia(), e.getNombreTemporal(), e.getFechaHoraIngreso(), e.getMotivo(), e.getSignosVitales(),
                e.getFechaHoraAlta(), e.getIdPaciente(), e.getIdHabitacion(), e.getIdMedico(), e.getIdTriaje(), e.getActivo());
    }

    private ConsultasDTO aDTO(Consultas c) {
        return new ConsultasDTO(c.getIdConsulta(), c.getFechaHora(), c.getSintomas(), c.getPeso(), c.getTalla(), c.getTemperatura(),
                c.getPresionArterial(), c.getFrecuenciaCardiaca(), c.getIdCita(), c.getActivo());
    }

    private DiagnosticosDTO aDTO(Diagnosticos d) {
        return new DiagnosticosDTO(d.getIdDiagnostico(), d.getObservaciones(), d.getIdTipoDiagnostico(), d.getDescripcion(), d.getFecha(), d.getActivo());
    }

    private TratamientoDTO aDTO(Tratamiento t) {
        return new TratamientoDTO(t.getIdTratamiento(), t.getDescripcion(), t.getDuracionDias(), t.getIndicaciones(), t.getFechaInicio(),
                t.getFechaFin(), t.getIdDiagnostico(), t.getActivo());
    }
}
