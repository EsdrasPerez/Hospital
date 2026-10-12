package com.example.hospital.tiposangre;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.tiposangre.dto.TipoSangreDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-sangre")
@Tag(name = "Tipos de sangre", description = "Catálogo de tipos de sangre (A+, O-, etc.)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class TipoSangreController {

    private final TipoSangreService tipoSangreService;

    public TipoSangreController(TipoSangreService tipoSangreService) {
        this.tipoSangreService = tipoSangreService;
    }

    // RN-TS-03: Catálogo de solo lectura para usuarios operativos
    @GetMapping
    @Operation(summary = "Listar tipos de sangre", description = "Devuelve el catálogo de tipos de sangre para asignar a los pacientes.\n\nAcceso: cualquier usuario autenticado.")
    public List<TipoSangreDTO> findAll() {
        return tipoSangreService.findAll();
    }

    // RN-TS-03: Catálogo de solo lectura para usuarios operativos
    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de sangre por ID", description = "Devuelve el detalle de un tipo de sangre.\n\nAcceso: cualquier usuario autenticado.")
    public TipoSangreDTO findById(@PathVariable Long id) {
        return tipoSangreService.findById(id);
    }

    // RN-TS-03: Solo el administrador lo modifica
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar tipo de sangre", description = "Agrega un tipo de sangre al catálogo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<TipoSangreDTO> crear(@RequestBody TipoSangreDTO dto) {
        TipoSangreDTO creado = tipoSangreService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    // RN-TS-03: Solo el administrador lo modifica
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de sangre", description = "Modifica un tipo de sangre.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<TipoSangreDTO> actualizar(
            @PathVariable Long id,
            @RequestBody TipoSangreDTO dto) {
        TipoSangreDTO actualizado = tipoSangreService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    // RN-TS-03: Solo el administrador lo modifica
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de sangre", description = "Desactiva un tipo de sangre. No se permite si hay pacientes que lo tienen asignado.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tipoSangreService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
