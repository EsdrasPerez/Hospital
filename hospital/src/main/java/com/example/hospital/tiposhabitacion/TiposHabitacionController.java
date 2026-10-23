package com.example.hospital.tiposhabitacion;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.tiposhabitacion.dto.TiposHabitacionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-habitacion")
@Tag(name = "Tipos de habitación", description = "Catálogo de tipos de habitación (individual, compartida, UCI, etc.)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class TiposHabitacionController {

    private final TiposHabitacionService tiposHabitacionService;

    public TiposHabitacionController(TiposHabitacionService tiposHabitacionService) {
        this.tiposHabitacionService = tiposHabitacionService;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de habitación", description = "Devuelve el catálogo de tipos de habitación.\n\nAcceso: cualquier usuario autenticado.")
    public List<TiposHabitacionDTO> findAll() {
        return tiposHabitacionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de habitación por ID", description = "Devuelve el detalle de un tipo de habitación.\n\nAcceso: cualquier usuario autenticado.")
    public TiposHabitacionDTO findById(@PathVariable Long id) {
        return tiposHabitacionService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar tipo de habitación", description = "Agrega un tipo de habitación al catálogo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<TiposHabitacionDTO> crear(@Valid @RequestBody TiposHabitacionDTO dto) {
        TiposHabitacionDTO creado = tiposHabitacionService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de habitación", description = "Modifica un tipo de habitación.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<TiposHabitacionDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TiposHabitacionDTO dto) {
        TiposHabitacionDTO actualizado = tiposHabitacionService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de habitación", description = "Elimina un tipo. No se permite si hay habitaciones de ese tipo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tiposHabitacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
