package com.example.hospital.cuentas;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.cuentas.dto.CuentasDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
@Tag(name = "Cuentas", description = "Cuentas hospitalarias donde se acumulan los cargos de un paciente")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class CuentasController {

    private final CuentasService cuentasService;

    public CuentasController(CuentasService cuentasService) {
        this.cuentasService = cuentasService;
    }

    @GetMapping
    @Operation(summary = "Listar cuentas", description = "Devuelve las cuentas hospitalarias. Filtros opcionales:\n- `idPaciente`: cuentas de un paciente\n- `idHospitalizacion`: cuentas de una hospitalización\n- `soloActivas=true`: solo cuentas activas\n\nAcceso: cualquier usuario autenticado.")
    public List<CuentasDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idPaciente,
            @RequestParam(required = false) Long idHospitalizacion) {
        if (idPaciente != null) {
            return cuentasService.findByPaciente(idPaciente);
        }
        if (idHospitalizacion != null) {
            return cuentasService.findByHospitalizacion(idHospitalizacion);
        }
        if (soloActivas) {
            return cuentasService.findActivas();
        }
        return cuentasService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cuenta por ID", description = "Devuelve el detalle de una cuenta (estado y saldo). Responde 404 si no existe.\n\nAcceso: cualquier usuario autenticado.")
    public CuentasDTO findById(@PathVariable Long id) {
        return cuentasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping
    @Operation(summary = "Abrir cuenta", description = "Abre una cuenta hospitalaria para un paciente/hospitalización. Una hospitalización solo puede tener una cuenta activa.\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<CuentasDTO> save(@RequestBody CuentasDTO dto) {
        CuentasDTO creada = cuentasService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cuenta", description = "Modifica los datos de una cuenta existente.\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<CuentasDTO> update(
            @PathVariable Long id,
            @RequestBody CuentasDTO dto) {
        CuentasDTO actualizada = cuentasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PatchMapping("/{id}/ajustar-saldo")
    @Operation(summary = "Ajustar saldo", description = "Suma (monto positivo) o resta (monto negativo) un valor al saldo de la cuenta, por ejemplo al cargar servicios o aplicar abonos. No permite dejar el saldo en negativo.\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<CuentasDTO> ajustarSaldo(
            @PathVariable Long id,
            @RequestParam BigDecimal monto) {
        CuentasDTO actualizada = cuentasService.ajustarSaldo(id, monto);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Cerrar / eliminar cuenta", description = "Desactiva una cuenta hospitalaria (borrado lógico).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cuentasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
