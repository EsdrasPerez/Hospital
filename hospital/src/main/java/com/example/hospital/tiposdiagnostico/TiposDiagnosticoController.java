package com.example.hospital.tiposdiagnostico;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.tiposdiagnostico.dto.TiposDiagnosticoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-diagnostico")
@Tag(name = "Tipos de diagnóstico", description = "Catálogo de tipos de diagnóstico (presuntivo, definitivo, etc.)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class TiposDiagnosticoController {

    private final TiposDiagnosticoService tiposDiagnosticoService;

    public TiposDiagnosticoController(TiposDiagnosticoService tiposDiagnosticoService) {
        this.tiposDiagnosticoService = tiposDiagnosticoService;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de diagnóstico", description = "Devuelve el catálogo de tipos de diagnóstico.\n\nAcceso: cualquier usuario autenticado.")
    public List<TiposDiagnosticoDTO> findAll() {
        return tiposDiagnosticoService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de diagnóstico por ID", description = "Devuelve el detalle de un tipo de diagnóstico.\n\nAcceso: cualquier usuario autenticado.")
    public TiposDiagnosticoDTO findById(@PathVariable Long id) {
        return tiposDiagnosticoService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar tipo de diagnóstico", description = "Agrega un tipo de diagnóstico al catálogo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<TiposDiagnosticoDTO> crear(@Valid @RequestBody TiposDiagnosticoDTO dto) {
        TiposDiagnosticoDTO creado = tiposDiagnosticoService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de diagnóstico", description = "Modifica un tipo de diagnóstico.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<TiposDiagnosticoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TiposDiagnosticoDTO dto) {
        TiposDiagnosticoDTO actualizado = tiposDiagnosticoService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de diagnóstico", description = "Elimina un tipo. No se permite si hay diagnósticos que lo usan.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tiposDiagnosticoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
