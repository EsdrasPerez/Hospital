package com.example.hospital.procedimientosemergencia;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.procedimientosemergencia.dto.ProcedimientosEmergenciaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procedimientos-emergencia")
@Tag(name = "Procedimientos de emergencia", description = "Procedimientos realizados a pacientes en emergencia")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class ProcedimientosEmergenciaController {

    private final ProcedimientosEmergenciaService procedimientosEmergenciaService;

    public ProcedimientosEmergenciaController(ProcedimientosEmergenciaService procedimientosEmergenciaService) {
        this.procedimientosEmergenciaService = procedimientosEmergenciaService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @GetMapping
    @Operation(summary = "Listar procedimientos", description = "Devuelve los procedimientos realizados en emergencia.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO.")
    public List<ProcedimientosEmergenciaDTO> findAll() {
        return procedimientosEmergenciaService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener procedimiento por ID", description = "Devuelve el detalle de un procedimiento de emergencia.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO.")
    public ProcedimientosEmergenciaDTO findById(@PathVariable Long id) {
        return procedimientosEmergenciaService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    @Operation(summary = "Registrar procedimiento", description = "Registra un procedimiento realizado a un paciente en emergencia.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<ProcedimientosEmergenciaDTO> crear(@RequestBody ProcedimientosEmergenciaDTO dto) {
        return new ResponseEntity<>(procedimientosEmergenciaService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar procedimiento", description = "Modifica un procedimiento de emergencia.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<ProcedimientosEmergenciaDTO> actualizar(@PathVariable Long id, @RequestBody ProcedimientosEmergenciaDTO dto) {
        return ResponseEntity.ok(procedimientosEmergenciaService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar procedimiento", description = "Desactiva un procedimiento de emergencia.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        procedimientosEmergenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
