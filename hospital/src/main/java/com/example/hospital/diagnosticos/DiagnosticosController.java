package com.example.hospital.diagnosticos;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.diagnosticos.dto.DiagnosticosDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diagnosticos")
@Tag(name = "Diagnósticos", description = "Diagnósticos médicos emitidos")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class DiagnosticosController {

    private final DiagnosticosService diagnosticosService;

    public DiagnosticosController(DiagnosticosService diagnosticosService) {
        this.diagnosticosService = diagnosticosService;
    }

    @GetMapping
    @Operation(summary = "Listar diagnósticos", description = "Devuelve los diagnósticos. Filtros opcionales:\n- `idTipoDiagnostico`: por tipo\n- `fecha` (yyyy-MM-dd): emitidos ese día\n- `soloActivos=true`: solo activos\n\nAcceso: cualquier usuario autenticado.")
    public List<DiagnosticosDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos,
            @RequestParam(required = false) Long idTipoDiagnostico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        if (idTipoDiagnostico != null) {
            return diagnosticosService.findByTipo(idTipoDiagnostico);
        }
        if (fecha != null) {
            return diagnosticosService.findByFecha(fecha);
        }
        if (soloActivos) {
            return diagnosticosService.findActivos();
        }
        return diagnosticosService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener diagnóstico por ID", description = "Devuelve el detalle de un diagnóstico. Responde 404 si no existe.\n\nAcceso: cualquier usuario autenticado.")
    public DiagnosticosDTO findById(@PathVariable Long id) {
        return diagnosticosService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    @Operation(summary = "Registrar diagnóstico", description = "Registra un nuevo diagnóstico médico.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<DiagnosticosDTO> save(@RequestBody DiagnosticosDTO dto) {
        DiagnosticosDTO creado = diagnosticosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar diagnóstico", description = "Modifica la información de un diagnóstico.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<DiagnosticosDTO> update(
            @PathVariable Long id,
            @RequestBody DiagnosticosDTO dto) {
        DiagnosticosDTO actualizado = diagnosticosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar diagnóstico", description = "Desactiva un diagnóstico (borrado lógico).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        diagnosticosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
