package com.example.hospital.tratamiento;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.tratamiento.dto.TratamientoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tratamiento")
@Tag(name = "Tratamientos", description = "Tratamientos indicados a partir de un diagnóstico")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class TratamientoController {

    private final TratamientoService tratamientoService;

    public TratamientoController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    @GetMapping
    @Operation(summary = "Listar tratamientos", description = "Devuelve los tratamientos. Filtros opcionales:\n- `idDiagnostico`: tratamientos de un diagnóstico\n- `soloActivos=true`: solo activos\n\nAcceso: cualquier usuario autenticado.")
    public List<TratamientoDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos,
            @RequestParam(required = false) Long idDiagnostico) {
        return tratamientoService.buscar(idDiagnostico, soloActivos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tratamiento por ID", description = "Devuelve el detalle de un tratamiento.\n\nAcceso: cualquier usuario autenticado.")
    public TratamientoDTO findById(@PathVariable Long id) {
        return tratamientoService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    @Operation(summary = "Registrar tratamiento", description = "Registra un tratamiento para un diagnóstico.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<TratamientoDTO> save(@Valid @RequestBody TratamientoDTO dto) {
        TratamientoDTO creado = tratamientoService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tratamiento", description = "Modifica un tratamiento.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<TratamientoDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TratamientoDTO dto) {
        TratamientoDTO actualizado = tratamientoService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tratamiento", description = "Desactiva un tratamiento (borrado lógico).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tratamientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
