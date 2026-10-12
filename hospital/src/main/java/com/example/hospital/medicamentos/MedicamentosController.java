package com.example.hospital.medicamentos;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.medicamentos.dto.MedicamentosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
@Tag(name = "Medicamentos", description = "Inventario de medicamentos de farmacia")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class MedicamentosController {

    private final MedicamentosService medicamentosService;

    public MedicamentosController(MedicamentosService medicamentosService) {
        this.medicamentosService = medicamentosService;
    }

    @GetMapping
    @Operation(summary = "Listar / buscar medicamentos", description = "Devuelve los medicamentos. Filtros opcionales:\n- `bajoStock=true`: medicamentos con stock igual o menor al mínimo\n- `criterio`: busca por nombre comercial o genérico\n- `soloActivos=true`: solo activos\n\nAcceso: cualquier usuario autenticado.")
    public List<MedicamentosDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos,
            @RequestParam(required = false, defaultValue = "false") boolean bajoStock,
            @RequestParam(required = false) String criterio) {
        if (bajoStock) {
            return medicamentosService.findBajoStock();
        }
        if (criterio != null && !criterio.trim().isEmpty()) {
            return medicamentosService.buscar(criterio);
        }
        if (soloActivos) {
            return medicamentosService.findActivos();
        }
        return medicamentosService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener medicamento por ID", description = "Devuelve el detalle de un medicamento (stock, precio, si requiere receta).\n\nAcceso: cualquier usuario autenticado.")
    public MedicamentosDTO findById(@PathVariable Long id) {
        return medicamentosService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FARMACEUTICO', 'MEDICO')")
    @PostMapping
    @Operation(summary = "Registrar medicamento", description = "Agrega un medicamento al inventario. El nombre comercial debe ser único.\n\nRoles permitidos: ADMIN, FARMACEUTICO, MEDICO.")
    public ResponseEntity<MedicamentosDTO> save(@RequestBody MedicamentosDTO dto) {
        MedicamentosDTO creado = medicamentosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FARMACEUTICO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar medicamento", description = "Modifica los datos de un medicamento.\n\nRoles permitidos: ADMIN, FARMACEUTICO.")
    public ResponseEntity<MedicamentosDTO> update(
            @PathVariable Long id,
            @RequestBody MedicamentosDTO dto) {
        MedicamentosDTO actualizado = medicamentosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FARMACEUTICO')")
    @PatchMapping("/{id}/stock")
    @Operation(summary = "Ajustar stock", description = "Suma (valor positivo, p. ej. una compra) o resta (valor negativo, p. ej. una salida) unidades al stock. No permite stock negativo ni ajustar medicamentos inactivos.\n\nRoles permitidos: ADMIN, FARMACEUTICO.")
    public ResponseEntity<MedicamentosDTO> ajustarStock(
            @PathVariable Long id,
            @RequestParam Integer cambio) {
        MedicamentosDTO actualizado = medicamentosService.ajustarStock(id, cambio);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar medicamento", description = "Desactiva un medicamento. No se permite si está en tratamientos.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicamentosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
