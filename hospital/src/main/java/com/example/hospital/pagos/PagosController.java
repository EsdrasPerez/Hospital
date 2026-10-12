package com.example.hospital.pagos;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.pagos.dto.PagosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Pagos aplicados a facturas y cuentas")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class PagosController {

    private final PagosService pagosService;

    public PagosController(PagosService pagosService) {
        this.pagosService = pagosService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping
    @Operation(summary = "Listar pagos", description = "Devuelve todos los pagos registrados.\n\nRoles permitidos: ADMIN, CAJERO.")
    public List<PagosDTO> findAll() {
        return pagosService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por ID", description = "Devuelve el detalle de un pago.\n\nRoles permitidos: ADMIN, CAJERO.")
    public PagosDTO findById(@PathVariable Long id) {
        return pagosService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping
    @Operation(summary = "Registrar pago", description = "Registra un pago sobre una factura con un método de pago. El monto debe ser mayor que 0 y la fecha no puede ser futura.\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<PagosDTO> crear(@RequestBody PagosDTO dto) {
        return new ResponseEntity<>(pagosService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pago", description = "Corrige los datos de un pago.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<PagosDTO> actualizar(@PathVariable Long id, @RequestBody PagosDTO dto) {
        return ResponseEntity.ok(pagosService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Anular pago", description = "Desactiva un pago (borrado lógico).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/factura/{idFactura}")
    @Operation(summary = "Pagos de una factura", description = "**Nuevo.** Devuelve todos los pagos aplicados a una factura. Responde 404 si la factura no existe.\n\nRoles permitidos: ADMIN, CAJERO.")
    public List<PagosDTO> findByFactura(@PathVariable Long idFactura) {
        return pagosService.findByFactura(idFactura);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/cuenta/{idCuenta}")
    @Operation(summary = "Pagos de una cuenta", description = "**Nuevo.** Devuelve todos los pagos asociados a una cuenta hospitalaria.\n\nRoles permitidos: ADMIN, CAJERO.")
    public List<PagosDTO> findByCuenta(@PathVariable Long idCuenta) {
        return pagosService.findByCuenta(idCuenta);
    }
}
