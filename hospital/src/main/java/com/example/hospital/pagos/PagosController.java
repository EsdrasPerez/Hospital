package com.example.hospital.pagos;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.pagos.dto.PagosDTO;
import jakarta.validation.Valid;
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
    @Operation(summary = "Listar pagos", description = "Devuelve todos los pagos registrados (incluidos los anulados, `activo=false`).\n\nRoles permitidos: ADMIN, CAJERO.")
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
    @Operation(summary = "Registrar pago", description = "Registra un pago sobre una factura con un método de pago. Reglas: el monto debe ser mayor que 0 y NO superar el saldo pendiente; la factura debe estar activa y Pendiente; el método de pago y la cuenta (opcional, del mismo paciente de la factura) deben existir y estar activos; la fecha no puede ser futura. Si el saldo queda en 0 la factura pasa automáticamente a Pagada.\n\nErrores: 400 (campos inválidos, con `detalles`), 422 (cualquier regla anterior).\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<PagosDTO> crear(@Valid @RequestBody PagosDTO dto) {
        return new ResponseEntity<>(pagosService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pago", description = "Corrige SOLO el método de pago y la referencia. Debe enviarse la misma factura y el mismo monto (y, si se envía, la misma cuenta); si se intenta cambiarlos, o modificar/reactivar un pago anulado, responde 422. Para anular use DELETE.\n\nErrores: 400, 404, 422.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<PagosDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PagosDTO dto) {
        return ResponseEntity.ok(pagosService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Anular pago", description = "Anula un pago (borrado lógico). Permitido con la factura Pendiente o Pagada (si estaba Pagada vuelve a Pendiente); con la factura Cancelada o con el pago ya anulado responde 422.\n\nErrores: 404, 422.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/factura/{idFactura}")
    @Operation(summary = "Pagos de una factura", description = "Devuelve los pagos aplicados a una factura. Con `soloActivos=true` oculta los anulados (por defecto se devuelven todos, para ver el historial). Responde 404 si la factura no existe.\n\nRoles permitidos: ADMIN, CAJERO.")
    public List<PagosDTO> findByFactura(@PathVariable Long idFactura,
                                        @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        return pagosService.findByFactura(idFactura, soloActivos);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/cuenta/{idCuenta}")
    @Operation(summary = "Pagos de una cuenta", description = "Devuelve los pagos asociados a una cuenta hospitalaria. Con `soloActivos=true` oculta los anulados (por defecto se devuelven todos). Responde 404 si la cuenta no existe.\n\nRoles permitidos: ADMIN, CAJERO.")
    public List<PagosDTO> findByCuenta(@PathVariable Long idCuenta,
                                       @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        return pagosService.findByCuenta(idCuenta, soloActivos);
    }
}
