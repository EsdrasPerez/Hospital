package com.example.hospital.clinicas;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.clinicas.dto.ClinicasDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinicas")
@Tag(name = "Clínicas", description = "Clínicas o unidades de atención del hospital")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class ClinicasController {

    private final ClinicasService clinicasService;

    public ClinicasController(ClinicasService clinicasService) {
        this.clinicasService = clinicasService;
    }

    @GetMapping
    @Operation(summary = "Listar clínicas", description = "Devuelve las clínicas del hospital. Con `soloActivas=true` devuelve únicamente las activas.\n\nAcceso: cualquier usuario autenticado.")
    public List<ClinicasDTO> findAll(@RequestParam(required = false, defaultValue = "false") boolean soloActivas) {
        if (soloActivas) {
            return clinicasService.findActivas();
        }
        return clinicasService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener clínica por ID", description = "Devuelve el detalle de una clínica. Responde 404 si no existe.\n\nAcceso: cualquier usuario autenticado.")
    public ClinicasDTO findById(@PathVariable Long id) {
        return clinicasService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar clínica", description = "Crea una nueva clínica dentro de un departamento.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<ClinicasDTO> save(@Valid @RequestBody ClinicasDTO dto) {
        ClinicasDTO creado = clinicasService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar clínica", description = "Modifica los datos de una clínica existente.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<ClinicasDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ClinicasDTO dto) {
        ClinicasDTO actualizado = clinicasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar clínica", description = "Desactiva una clínica. No se permite si tiene registros activos asociados.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clinicasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
