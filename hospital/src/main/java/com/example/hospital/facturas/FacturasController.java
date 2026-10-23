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
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    @Operation(summary = "Listar facturas", description = "Devuelve las facturas. Todos los filtros son opcionales y COMBINABLES:\n- `idPaciente`: facturas de un paciente\n- `idHospitalizacion`: facturas de una hospitalización\n- `idEstadoFactura`: por estado\n- `desde` / `hasta` (YYYY-MM-DD): fecha de emisión, ambos inclusive\n- `soloActivas=true`: oculta las anuladas (por defecto se devuelven también)\n\nErrores: 422 si `desde` es posterior a `hasta`.\n\nAcceso: cualquier usuario autenticado.")
    public List<FacturasDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idPaciente,
            @RequestParam(required = false) Long idHospitalizacion,
            @RequestParam(required = false) Long idEstadoFactura,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return facturasService.buscar(idPaciente, idHospitalizacion, idEstadoFactura, desde, hasta, soloActivas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener factura por ID", description = "Devuelve el encabezado de una factura (subtotal, IVA, total, estado). Subtotal, IVA y total los calcula el servidor. Responde 404 si no existe.\n\nAcceso: cualquier usuario autenticado.")
    public FacturasDTO findById(@PathVariable Long id) {
        return facturasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}/saldo")
    @Operation(summary = "Saldo de una factura", description = "Calcula cuánto se ha pagado de la factura (suma de pagos activos) y cuánto queda pendiente. Indica con `liquidada=true` si ya está completamente pagada. Responde 404 si la factura no existe.\n\nRoles permitidos: ADMIN, CAJERO, RECEPCIONISTA.")
    public SaldoFacturaDTO obtenerSaldo(@PathVariable Long id) {
        return facturasService.obtenerSaldo(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @PostMapping
    @Operation(summary = "Emitir factura", description = "Emite una factura para un paciente (opcionalmente ligada a una hospitalización) con un método de pago. La factura nace con subtotal, IVA y total en 0.00, estado Pendiente y fecha de emisión = ahora: el servidor IGNORA `subtotal`, `iva`, `total`, `idEstadoFactura`, `fechaEmision` y `activo` del cuerpo. El subtotal sale solo de las líneas (`/api/detalle-factura`) y el IVA lo calcula el servidor (tasa `app.facturacion.iva`, 0.12 por defecto): total = subtotal + IVA.\n\nErrores: 400 (paciente o método de pago ausentes, con `detalles`), 422 (paciente, hospitalización o método inexistentes/inactivos; hospitalización de otro paciente).\n\nRoles permitidos: ADMIN, CAJERO, RECEPCIONISTA.")
    public ResponseEntity<FacturasDTO> save(@Valid @RequestBody FacturasDTO dto) {
        FacturasDTO creada = facturasService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar factura", description = "Modifica el método de pago, la hospitalización y (solo si no tiene pagos) el paciente. Subtotal, IVA, total, estado, fecha de emisión y `activo` NO se modifican aquí (se ignoran): los importes salen de las líneas, el estado cambia con `PATCH /estado` o automáticamente, y para anular se usa DELETE. En una factura Pagada o Cancelada solo se puede cambiar el método de pago.\n\nErrores: 400 (campos ausentes), 404, 422 (factura anulada, referencias inválidas, cambios no permitidos).\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<FacturasDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FacturasDTO dto) {
        FacturasDTO actualizada = facturasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PatchMapping("/{id}/estado/{nuevoEstadoId}")
    @Operation(summary = "Cambiar estado de factura", description = "Cambia el estado. Reglas: Pendiente → Pagada solo si el total es mayor a 0 y el saldo es 0; Pendiente → Cancelada solo sin pagos activos; Pagada y Cancelada son terminales; mismo estado → 422. (Pagada vuelve a Pendiente solo de forma automática al anular un pago.)\n\nErrores: 400 (id con formato inválido), 404, 422.\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<FacturasDTO> cambiarEstado(
            @PathVariable Long id,
            @PathVariable Long nuevoEstadoId) {
        FacturasDTO actualizada = facturasService.cambiarEstado(id, nuevoEstadoId);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Anular factura", description = "Anula una factura (borrado lógico).\n\nErrores: 404; 409 si tiene pagos activos (anúlelos primero); 422 si está Pagada o ya anulada.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facturasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
