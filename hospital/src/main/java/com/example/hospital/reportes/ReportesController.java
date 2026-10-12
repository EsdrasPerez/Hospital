package com.example.hospital.reportes;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.reportes.dto.HistorialPacienteDTO;
import com.example.hospital.reportes.dto.ResumenDashboardDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "**Nuevo.** Indicadores y vistas consolidadas para tableros y expedientes")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class ReportesController {

    private final ReportesService reportesService;

    public ReportesController(ReportesService reportesService) {
        this.reportesService = reportesService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA', 'CAJERO')")
    @GetMapping("/resumen")
    @Operation(summary = "Resumen del hospital (dashboard)", description = "**Nuevo.** Devuelve en una sola llamada los indicadores principales del día: pacientes activos, citas de hoy, hospitalizaciones en curso, emergencias sin alta, habitaciones disponibles, medicamentos con bajo stock y facturas pendientes.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA, CAJERO.")
    public ResumenDashboardDTO resumen() {
        return reportesService.resumen();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @GetMapping("/paciente/{idPaciente}/historial")
    @Operation(summary = "Historial completo de un paciente", description = "**Nuevo.** Devuelve el expediente consolidado de un paciente: datos generales, citas, hospitalizaciones, ingresos a emergencia, cuentas y facturas. Evita que el frontend tenga que hacer 6 llamadas. Responde 404 si el paciente no existe o está inactivo.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO.")
    public HistorialPacienteDTO historialPaciente(@PathVariable Long idPaciente) {
        return reportesService.historialPaciente(idPaciente);
    }
}
