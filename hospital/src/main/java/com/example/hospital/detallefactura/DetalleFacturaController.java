package com.example.hospital.detallefactura;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.detallefactura.dto.DetalleFacturaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-factura")
@Tag(name = "Detalle de factura", description = "Líneas o conceptos cobrados dentro de una factura")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class DetalleFacturaController {

    private final DetalleFacturaService detalleFacturaService;

    public DetalleFacturaController(DetalleFacturaService detalleFacturaService) {
        this.detalleFacturaService = detalleFacturaService;
    }

    @GetMapping
    @Operation(summary = "Listar líneas de factura", description = "Devuelve las líneas de detalle. Con `idFactura` devuelve solo las de esa factura.\n\nAcceso: cualquier usuario autenticado.")
    public List<DetalleFacturaDTO> findAll(@RequestParam(required = false) Long idFactura) {
        if (idFactura != null) {
            return detalleFacturaService.findByFactura(idFactura);
        }
        return detalleFacturaService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener línea por ID", description = "Devuelve una línea de detalle (concepto, cantidad, precio unitario e importe).\n\nAcceso: cualquier usuario autenticado.")
    public DetalleFacturaDTO findById(@PathVariable Long id) {
        return detalleFacturaService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @PostMapping
    @Operation(summary = "Agregar línea a factura", description = "Agrega un concepto cobrado a una factura. El importe se calcula como cantidad × precio unitario.\n\nRoles permitidos: ADMIN, CAJERO, RECEPCIONISTA.")
    public ResponseEntity<DetalleFacturaDTO> save(@RequestBody DetalleFacturaDTO dto) {
        DetalleFacturaDTO creado = detalleFacturaService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar línea", description = "Modifica concepto, cantidad o precio de una línea de factura.\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<DetalleFacturaDTO> update(
            @PathVariable Long id,
            @RequestBody DetalleFacturaDTO dto) {
        DetalleFacturaDTO actualizado = detalleFacturaService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar línea", description = "Quita una línea de detalle de la factura.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        detalleFacturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
