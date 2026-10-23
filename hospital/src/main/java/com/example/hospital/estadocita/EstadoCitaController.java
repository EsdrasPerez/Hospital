package com.example.hospital.estadocita;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.estadocita.dto.EstadoCitaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estado-cita")
@Tag(name = "Estados de cita", description = "Catálogo de estados por los que pasa una cita")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class EstadoCitaController {

    private final EstadoCitaService estadoCitaService;

    public EstadoCitaController(EstadoCitaService estadoCitaService) {
        this.estadoCitaService = estadoCitaService;
    }

    @GetMapping
    @Operation(summary = "Listar estados de cita", description = "Devuelve los estados posibles de una cita (Programada, Confirmada, Atendida, Cancelada, etc.).\n\nAcceso: cualquier usuario autenticado.")
    public List<EstadoCitaDTO> findAll() {
        return estadoCitaService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado por ID", description = "Devuelve el detalle de un estado de cita.\n\nAcceso: cualquier usuario autenticado.")
    public EstadoCitaDTO findById(@PathVariable Long id) {
        return estadoCitaService.findById(id);
    }

    // Endpoint auxiliar para verificar si una transición entre estados es válida
    @GetMapping("/validar-transicion")
    @Operation(summary = "Validar cambio de estado", description = "Indica si se permite pasar una cita del estado `origen` al estado `destino` (por descripción). Devuelve `{origen, destino, permitida}`; útil para habilitar/deshabilitar botones en el frontend.\n\nAcceso: cualquier usuario autenticado.")
    public ResponseEntity<Map<String, Object>> validarTransicion(
            @RequestParam String origen,
            @RequestParam String destino) {
        boolean permitida = estadoCitaService.esTransicionPermitida(origen, destino);
        return ResponseEntity.ok(Map.of(
                "origen", origen,
                "destino", destino,
                "permitida", permitida
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar estado de cita", description = "Agrega un nuevo estado al catálogo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EstadoCitaDTO> crear(@Valid @RequestBody EstadoCitaDTO dto) {
        EstadoCitaDTO creado = estadoCitaService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de cita", description = "Modifica un estado de cita.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EstadoCitaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstadoCitaDTO dto) {
        EstadoCitaDTO actualizado = estadoCitaService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado de cita", description = "Elimina un estado. No se permite si hay citas que lo usan.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estadoCitaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
