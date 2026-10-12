package com.example.hospital.estadohabitacion;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.estadohabitacion.dto.EstadoHabitacionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estado-habitacion")
@Tag(name = "Estados de habitación", description = "Catálogo de estados de habitación (Disponible, Ocupada, Mantenimiento)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class EstadoHabitacionController {

    private final EstadoHabitacionService estadoHabitacionService;

    public EstadoHabitacionController(EstadoHabitacionService estadoHabitacionService) {
        this.estadoHabitacionService = estadoHabitacionService;
    }

    // Consulta de libre lectura para usuarios autenticados
    @GetMapping
    @Operation(summary = "Listar estados de habitación", description = "Devuelve los estados posibles de una habitación.\n\nAcceso: cualquier usuario autenticado.")
    public List<EstadoHabitacionDTO> findAll() {
        return estadoHabitacionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado por ID", description = "Devuelve el detalle de un estado de habitación.\n\nAcceso: cualquier usuario autenticado.")
    public EstadoHabitacionDTO findById(@PathVariable Long id) {
        return estadoHabitacionService.findById(id);
    }

    // Solo ADMIN puede modificar catálogos base
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar estado de habitación", description = "Agrega un nuevo estado al catálogo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EstadoHabitacionDTO> crear(@RequestBody EstadoHabitacionDTO dto) {
        EstadoHabitacionDTO creado = estadoHabitacionService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de habitación", description = "Modifica un estado de habitación.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EstadoHabitacionDTO> actualizar(
            @PathVariable Long id,
            @RequestBody EstadoHabitacionDTO dto) {
        EstadoHabitacionDTO actualizado = estadoHabitacionService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado de habitación", description = "Elimina un estado. No se permite si hay habitaciones que lo usan.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estadoHabitacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
