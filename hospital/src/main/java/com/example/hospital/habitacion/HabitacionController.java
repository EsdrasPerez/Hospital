package com.example.hospital.habitacion;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.habitacion.dto.HabitacionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habitacion")
@Tag(name = "Habitaciones", description = "Habitaciones y camas del hospital")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class HabitacionController {

    private final HabitacionService habitacionService;

    public HabitacionController(HabitacionService habitacionService) {
        this.habitacionService = habitacionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @Operation(summary = "Listar habitaciones", description = "Devuelve las habitaciones. Filtros opcionales:\n- `soloDisponibles=true`: solo habitaciones en estado Disponible\n- `soloActivas=true`: solo habitaciones activas\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public List<HabitacionDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false, defaultValue = "false") boolean soloDisponibles) {
        if (soloDisponibles) {
            return habitacionService.findDisponibles();
        }
        if (soloActivas) {
            return habitacionService.findActivas();
        }
        return habitacionService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @Operation(summary = "Obtener habitación por ID", description = "Devuelve el detalle de una habitación (número, piso, capacidad, tipo y estado).\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public HabitacionDTO findById(@PathVariable Long id) {
        return habitacionService.findById(id);
    }

    @GetMapping("/numero/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @Operation(summary = "Buscar habitación por número", description = "Devuelve la habitación con el número indicado (p. ej. `203-A`). Responde 404 si no existe.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public HabitacionDTO findByNumero(@PathVariable String numero) {
        return habitacionService.findByNumero(numero);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar habitación", description = "Crea una nueva habitación. El número debe ser único.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<HabitacionDTO> save(@RequestBody HabitacionDTO dto) {
        HabitacionDTO creada = habitacionService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar habitación", description = "Modifica los datos de una habitación.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<HabitacionDTO> update(
            @PathVariable Long id,
            @RequestBody HabitacionDTO dto) {
        HabitacionDTO actualizada = habitacionService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado/{nuevoEstadoId}")
    @Operation(summary = "Cambiar estado de habitación", description = "Cambia el estado de la habitación (Disponible, Ocupada, Mantenimiento).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<HabitacionDTO> cambiarEstado(
            @PathVariable Long id,
            @PathVariable Long nuevoEstadoId) {
        HabitacionDTO actualizada = habitacionService.cambiarEstado(id, nuevoEstadoId);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar habitación", description = "Desactiva una habitación. No se permite si tiene pacientes hospitalizados o en emergencia.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        habitacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
