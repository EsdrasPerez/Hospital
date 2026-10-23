package com.example.hospital.estadosfactura;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.estadosfactura.dto.EstadosFacturaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados-factura")
@Tag(name = "Estados de factura", description = "Catálogo de estados de factura (Pendiente, Pagada, Cancelada)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class EstadosFacturaController {

    private final EstadosFacturaService estadosFacturaService;

    public EstadosFacturaController(EstadosFacturaService estadosFacturaService) {
        this.estadosFacturaService = estadosFacturaService;
    }

    @GetMapping
    @Operation(summary = "Listar estados de factura", description = "Devuelve los estados de factura. Con `soloActivos=true` solo los activos.\n\nAcceso: cualquier usuario autenticado.")
    public List<EstadosFacturaDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        if (soloActivos) {
            return estadosFacturaService.findActivos();
        }
        return estadosFacturaService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado por ID", description = "Devuelve el detalle de un estado de factura.\n\nAcceso: cualquier usuario autenticado.")
    public EstadosFacturaDTO findById(@PathVariable Long id) {
        return estadosFacturaService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar estado de factura", description = "Agrega un nuevo estado al catálogo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EstadosFacturaDTO> save(@Valid @RequestBody EstadosFacturaDTO dto) {
        EstadosFacturaDTO creado = estadosFacturaService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de factura", description = "Modifica un estado de factura.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EstadosFacturaDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EstadosFacturaDTO dto) {
        EstadosFacturaDTO actualizado = estadosFacturaService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado de factura", description = "Desactiva un estado. No se permite si hay facturas que lo usan.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        estadosFacturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
