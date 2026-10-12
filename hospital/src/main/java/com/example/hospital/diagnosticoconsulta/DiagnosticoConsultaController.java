package com.example.hospital.diagnosticoconsulta;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.diagnosticoconsulta.dto.DiagnosticoConsultaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnostico-consulta")
@Tag(name = "Diagnóstico por consulta", description = "Relación entre consultas y los diagnósticos emitidos en ellas")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class DiagnosticoConsultaController {

    private final DiagnosticoConsultaService diagnosticoConsultaService;

    public DiagnosticoConsultaController(DiagnosticoConsultaService diagnosticoConsultaService) {
        this.diagnosticoConsultaService = diagnosticoConsultaService;
    }

    @GetMapping
    @Operation(summary = "Listar relaciones", description = "Devuelve todas las relaciones diagnóstico–consulta.\n\nAcceso: cualquier usuario autenticado.")
    public List<DiagnosticoConsultaDTO> findAll() {
        return diagnosticoConsultaService.findAll();
    }

    @GetMapping("/consulta/{idConsulta}")
    @Operation(summary = "Diagnósticos de una consulta", description = "Devuelve los diagnósticos emitidos en una consulta.\n\nAcceso: cualquier usuario autenticado.")
    public List<DiagnosticoConsultaDTO> findByConsulta(@PathVariable Long idConsulta) {
        return diagnosticoConsultaService.findByConsulta(idConsulta);
    }

    @GetMapping("/diagnostico/{idDiagnostico}")
    @Operation(summary = "Consultas con un diagnóstico", description = "Devuelve las consultas en las que se emitió un diagnóstico.\n\nAcceso: cualquier usuario autenticado.")
    public List<DiagnosticoConsultaDTO> findByDiagnostico(@PathVariable Long idDiagnostico) {
        return diagnosticoConsultaService.findByDiagnostico(idDiagnostico);
    }

    @GetMapping("/{idDiagnostico}/{idConsulta}")
    @Operation(summary = "Obtener relación", description = "Devuelve una relación específica a partir de su llave compuesta (idDiagnostico + idConsulta).\n\nAcceso: cualquier usuario autenticado.")
    public DiagnosticoConsultaDTO findById(
            @PathVariable Long idDiagnostico,
            @PathVariable Long idConsulta) {
        return diagnosticoConsultaService.findById(idDiagnostico, idConsulta);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    @Operation(summary = "Asociar diagnóstico a consulta", description = "Vincula un diagnóstico a una consulta. No permite duplicados.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<DiagnosticoConsultaDTO> save(@RequestBody DiagnosticoConsultaDTO dto) {
        DiagnosticoConsultaDTO creado = diagnosticoConsultaService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @DeleteMapping("/{idDiagnostico}/{idConsulta}")
    @Operation(summary = "Quitar diagnóstico de consulta", description = "Elimina el vínculo entre un diagnóstico y una consulta.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<Void> delete(
            @PathVariable Long idDiagnostico,
            @PathVariable Long idConsulta) {
        diagnosticoConsultaService.eliminar(idDiagnostico, idConsulta);
        return ResponseEntity.noContent().build();
    }
}
