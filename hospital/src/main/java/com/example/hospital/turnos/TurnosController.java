package com.example.hospital.turnos;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.turnos.dto.TurnosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@Tag(name = "Turnos", description = "Turnos de trabajo del personal (mañana, tarde, noche)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class TurnosController {

    private final TurnosService turnosService;

    public TurnosController(TurnosService turnosService) {
        this.turnosService = turnosService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping
    @Operation(summary = "Listar turnos", description = "Devuelve los turnos de trabajo con su hora de inicio y fin.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public List<TurnosDTO> findAll() {
        return turnosService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener turno por ID", description = "Devuelve el detalle de un turno.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public TurnosDTO findById(@PathVariable Long id) {
        return turnosService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar turno", description = "Crea un nuevo turno. La descripción no puede repetirse.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<TurnosDTO> registrar(@Valid @RequestBody TurnosDTO dto) {
        TurnosDTO creado = turnosService.registrar(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar turno", description = "Modifica un turno.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<TurnosDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TurnosDTO dto) {
        TurnosDTO actualizado = turnosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar turno", description = "Desactiva un turno. No se permite si tiene asignaciones vigentes o futuras.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        turnosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
