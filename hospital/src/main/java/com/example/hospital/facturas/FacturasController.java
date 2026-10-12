package com.example.hospital.facturas;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.facturas.dto.FacturasDTO;
import com.example.hospital.facturas.dto.SaldoFacturaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@Tag(name = "Facturas", description = "Facturación de servicios a pacientes")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class FacturasController {

    private final FacturasService facturasService;

    public FacturasController(FacturasService facturasService) {
        this.facturasService = facturasService;
    }

    @GetMapping
    @Operation(summary = "Listar facturas", description = "Devuelve las facturas. Filtros opcionales:\n- `idPaciente`: facturas de un paciente\n- `idHospitalizacion`: facturas de una hospitalización\n- `soloActivas=true`: solo facturas activas\n\nAcceso: cualquier usuario autenticado.")
    public List<FacturasDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idPaciente,
            @RequestParam(required = false) Long idHospitalizacion) {
        if (idPaciente != null) {
            return facturasService.findByPaciente(idPaciente);
        }
        if (idHospitalizacion != null) {
            return facturasService.findByHospitalizacion(idHospitalizacion);
        }
        if (soloActivas) {
            return facturasService.findActivas();
        }
        return facturasService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener factura por ID", description = "Devuelve el encabezado de una factura (subtotal, IVA, total, estado). Responde 404 si no existe.\n\nAcceso: cualquier usuario autenticado.")
    public FacturasDTO findById(@PathVariable Long id) {
        return facturasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}/saldo")
    @Operation(summary = "Saldo de una factura", description = "**Nuevo.** Calcula cuánto se ha pagado de la factura (suma de pagos activos) y cuánto queda pendiente. Indica con `liquidada=true` si ya está completamente pagada. Responde 404 si la factura no existe.\n\nRoles permitidos: ADMIN, CAJERO, RECEPCIONISTA.")
    public SaldoFacturaDTO obtenerSaldo(@PathVariable Long id) {
        return facturasService.obtenerSaldo(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @PostMapping
    @Operation(summary = "Emitir factura", description = "Crea una factura para un paciente (opcionalmente ligada a una hospitalización). Calcula el IVA y el total a partir del subtotal.\n\nRoles permitidos: ADMIN, CAJERO, RECEPCIONISTA.")
    public ResponseEntity<FacturasDTO> save(@RequestBody FacturasDTO dto) {
        FacturasDTO creada = facturasService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar factura", description = "Modifica los datos de una factura.\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<FacturasDTO> update(
            @PathVariable Long id,
            @RequestBody FacturasDTO dto) {
        FacturasDTO actualizada = facturasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PatchMapping("/{id}/estado/{nuevoEstadoId}")
    @Operation(summary = "Cambiar estado de factura", description = "Cambia el estado de la factura (p. ej. Pendiente → Pagada o Cancelada).\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<FacturasDTO> cambiarEstado(
            @PathVariable Long id,
            @PathVariable Long nuevoEstadoId) {
        FacturasDTO actualizada = facturasService.cambiarEstado(id, nuevoEstadoId);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Anular factura", description = "Desactiva una factura (borrado lógico).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facturasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
