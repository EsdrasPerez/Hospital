package com.example.hospital.estadoshospitalizacion;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.estadoshospitalizacion.dto.EstadosHospitalizacionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados-hospitalizacion")
@Tag(name = "Estados de hospitalización", description = "Catálogo de estados de hospitalización (Activa, Alta, etc.)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class EstadosHospitalizacionController {

    private final EstadosHospitalizacionService estadosHospitalizacionService;

    public EstadosHospitalizacionController(EstadosHospitalizacionService estadosHospitalizacionService) {
        this.estadosHospitalizacionService = estadosHospitalizacionService;
    }

    @GetMapping
    @Operation(summary = "Listar estados de hospitalización", description = "Devuelve los estados de hospitalización. Con `soloActivos=true` solo los activos.\n\nAcceso: cualquier usuario autenticado.")
    public List<EstadosHospitalizacionDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        if (soloActivos) {
            return estadosHospitalizacionService.findActivos();
        }
        return estadosHospitalizacionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado por ID", description = "Devuelve el detalle de un estado de hospitalización.\n\nAcceso: cualquier usuario autenticado.")
    public EstadosHospitalizacionDTO findById(@PathVariable Long id) {
        return estadosHospitalizacionService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar estado de hospitalización", description = "Agrega un nuevo estado al catálogo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EstadosHospitalizacionDTO> save(@RequestBody EstadosHospitalizacionDTO dto) {
        EstadosHospitalizacionDTO creado = estadosHospitalizacionService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de hospitalización", description = "Modifica un estado de hospitalización.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EstadosHospitalizacionDTO> update(
            @PathVariable Long id,
            @RequestBody EstadosHospitalizacionDTO dto) {
        EstadosHospitalizacionDTO actualizado = estadosHospitalizacionService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado de hospitalización", description = "Desactiva un estado. No se permite si hay hospitalizaciones que lo usan.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        estadosHospitalizacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
