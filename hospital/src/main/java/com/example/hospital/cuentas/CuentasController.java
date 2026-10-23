package com.example.hospital.cuentas;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.cuentas.dto.CuentasDTO;
import jakarta.validation.Valid;
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
    @Operation(summary = "Listar cuentas", description = "Devuelve las cuentas hospitalarias. Filtros opcionales y COMBINABLES:\n- `idPaciente`\n- `idHospitalizacion`\n- `estado` (Abierta, Cerrada o Liquidada; otro valor responde 422)\n- `soloActivas=true`: oculta las desactivadas (por defecto se devuelven también)\n\nAcceso: cualquier usuario autenticado.")
    public List<CuentasDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idPaciente,
            @RequestParam(required = false) Long idHospitalizacion,
            @RequestParam(required = false) String estado) {
        return cuentasService.buscar(idPaciente, idHospitalizacion, estado, soloActivas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cuenta por ID", description = "Devuelve el detalle de una cuenta (estado y saldo). Responde 404 si no existe.\n\nAcceso: cualquier usuario autenticado.")
    public CuentasDTO findById(@PathVariable Long id) {
        return cuentasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping
    @Operation(summary = "Abrir cuenta", description = "Abre una cuenta hospitalaria para un paciente/hospitalización. Una hospitalización solo puede tener una cuenta activa. La cuenta nace Abierta y activa (se ignoran `estado` y `activo`); el cliente solo define el saldo inicial (>= 0, por defecto 0).\n\nErrores: 400 (campos inválidos, con `detalles`), 409 (la hospitalización ya tiene una cuenta activa), 422 (paciente u hospitalización inexistentes/inactivos o de otro paciente).\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<CuentasDTO> save(@Valid @RequestBody CuentasDTO dto) {
        CuentasDTO creada = cuentasService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cuenta", description = "Modifica paciente, hospitalización y estado (Abierta, Cerrada o Liquidada; Liquidada exige saldo 0). El saldo NO se modifica aquí (se ignora): use `PATCH /ajustar-saldo`. Una cuenta Liquidada puede reabrirse con estado Abierta. No cambia el paciente si la cuenta tiene pagos.\n\nErrores: 400, 404, 409, 422.\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<CuentasDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CuentasDTO dto) {
        CuentasDTO actualizada = cuentasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PatchMapping("/{id}/ajustar-saldo")
    @Operation(summary = "Ajustar saldo", description = "Suma (monto positivo) o resta (monto negativo) un valor al saldo de la cuenta. Reglas (422): monto distinto de 0 y con hasta 2 decimales; el saldo resultante no puede ser negativo ni mayor a 99,999,999.99; no se ajustan cuentas desactivadas ni Cerradas. Con saldo 0 una cuenta Abierta pasa a Liquidada y una Liquidada vuelve a Abierta si el saldo resultante es mayor que 0 (reapertura automática).\n\nErrores: 400 (monto ausente o con formato inválido), 404, 422.\n\nRoles permitidos: ADMIN, CAJERO.")
    public ResponseEntity<CuentasDTO> ajustarSaldo(
            @PathVariable Long id,
            @RequestParam BigDecimal monto) {
        CuentasDTO actualizada = cuentasService.ajustarSaldo(id, monto);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Cerrar / eliminar cuenta", description = "Desactiva una cuenta hospitalaria (borrado lógico).\n\nErrores: 404; 409 si la cuenta tiene pagos registrados; 422 si ya está desactivada.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cuentasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
