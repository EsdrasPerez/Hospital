package com.example.hospital.enfermeros;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.enfermeros.dto.EnfermerosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enfermeros")
@Tag(name = "Enfermeros", description = "Personal de enfermería")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class EnfermerosController {

    private final EnfermerosService enfermerosService;

    public EnfermerosController(EnfermerosService enfermerosService) {
        this.enfermerosService = enfermerosService;
    }

    @GetMapping
    @Operation(summary = "Listar enfermeros", description = "Devuelve el personal de enfermería. Con `soloActivos=true` devuelve solo los activos.\n\nAcceso: cualquier usuario autenticado.")
    public List<EnfermerosDTO> findAll(@RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        if (soloActivos) {
            return enfermerosService.findActivos();
        }
        return enfermerosService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener enfermero por ID", description = "Devuelve los datos de un enfermero. Responde 404 si no existe.\n\nAcceso: cualquier usuario autenticado.")
    public EnfermerosDTO findById(@PathVariable Long id) {
        return enfermerosService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar enfermero", description = "Da de alta a un nuevo enfermero.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EnfermerosDTO> save(@Valid @RequestBody EnfermerosDTO dto) {
        EnfermerosDTO creado = enfermerosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar enfermero", description = "Modifica los datos de un enfermero.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EnfermerosDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EnfermerosDTO dto) {
        EnfermerosDTO actualizado = enfermerosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar enfermero", description = "Desactiva a un enfermero. No se permite si tiene asignaciones registradas.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enfermerosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
