package com.example.hospital.reportes.impl;

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

    // Mismos valores que usan los demás servicios (según Hosp.sql)
    private static final Long ESTADO_HOSP_ACTIVA = 1L;
    private static final Long ESTADO_HAB_DISPONIBLE = 1L;
    private static final Long ESTADO_FACTURA_PENDIENTE = 1L;

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
                               FacturasService facturasService) {
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
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenDashboardDTO resumen() {
        LocalDate hoy = LocalDate.now();
        return new ResumenDashboardDTO(
                hoy,
                pacienteRepository.countByActivoTrue(),
                citasRepository.countByFechaAndActivoTrue(hoy),
                hospitalizacionesRepository.countByIdEstadoHosp(ESTADO_HOSP_ACTIVA),
                emergenciaRepository.countByActivoTrueAndFechaHoraAltaIsNull(),
                habitacionRepository.countByIdEstadoHabitacionAndActivoTrue(ESTADO_HAB_DISPONIBLE),
                medicamentosService.findBajoStock().size(),
                facturasRepository.countByIdEstadoFacturaAndActivoTrue(ESTADO_FACTURA_PENDIENTE)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialPacienteDTO historialPaciente(Long idPaciente) {
        // Lanza 404 si el paciente no existe o está inactivo
        PacienteResponse paciente = pacienteService.obtener(idPaciente);
        return new HistorialPacienteDTO(
                paciente,
                citasService.findByPaciente(idPaciente),
                hospitalizacionesService.findByPaciente(idPaciente),
                emergenciaService.findByPaciente(idPaciente),
                cuentasService.findByPaciente(idPaciente),
                facturasService.findByPaciente(idPaciente)
        );
    }
}
