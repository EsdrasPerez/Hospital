package com.example.hospital.metodospago;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.metodospago.dto.MetodosPagoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metodos-pago")
@Tag(name = "Métodos de pago", description = "Catálogo de métodos de pago (efectivo, tarjeta, transferencia, seguro)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class MetodosPagoController {

    private final MetodosPagoService metodosPagoService;

    public MetodosPagoController(MetodosPagoService metodosPagoService) {
        this.metodosPagoService = metodosPagoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @GetMapping
    @Operation(summary = "Listar métodos de pago", description = "Devuelve los métodos de pago disponibles.\n\nRoles permitidos: ADMIN, CAJERO, RECEPCIONISTA.")
    public List<MetodosPagoDTO> findAll() {
        return metodosPagoService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener método de pago por ID", description = "Devuelve el detalle de un método de pago.\n\nRoles permitidos: ADMIN, CAJERO, RECEPCIONISTA.")
    public MetodosPagoDTO findById(@PathVariable Long id) {
        return metodosPagoService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar método de pago", description = "Agrega un nuevo método de pago al catálogo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<MetodosPagoDTO> crear(@RequestBody MetodosPagoDTO dto) {
        return new ResponseEntity<>(metodosPagoService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar método de pago", description = "Modifica un método de pago.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<MetodosPagoDTO> actualizar(@PathVariable Long id, @RequestBody MetodosPagoDTO dto) {
        return ResponseEntity.ok(metodosPagoService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar método de pago", description = "Desactiva un método de pago. No se permite si hay facturas o pagos que lo usan.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        metodosPagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
