package com.example.hospital.especialidadclinica;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.especialidadclinica.dto.EspecialidadClinicaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidad-clinica")
@Tag(name = "Especialidad de clínica", description = "Especialidades que se atienden en cada clínica")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class EspecialidadClinicaController {

    private final EspecialidadClinicaService especialidadClinicaService;

    public EspecialidadClinicaController(EspecialidadClinicaService especialidadClinicaService) {
        this.especialidadClinicaService = especialidadClinicaService;
    }

    @GetMapping
    @Operation(summary = "Listar especialidades de clínica", description = "Devuelve las especialidades por clínica. Con `soloActivas=true` solo las activas.\n\nAcceso: cualquier usuario autenticado.")
    public List<EspecialidadClinicaDTO> findAll(@RequestParam(required = false, defaultValue = "false") boolean soloActivas) {
        if (soloActivas) {
            return especialidadClinicaService.findActivas();
        }
        return especialidadClinicaService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener especialidad de clínica por ID", description = "Devuelve el detalle de una especialidad de clínica.\n\nAcceso: cualquier usuario autenticado.")
    public EspecialidadClinicaDTO findById(@PathVariable Long id) {
        return especialidadClinicaService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar especialidad de clínica", description = "Crea una nueva especialidad para una clínica.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EspecialidadClinicaDTO> save(@RequestBody EspecialidadClinicaDTO dto) {
        EspecialidadClinicaDTO creada = especialidadClinicaService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar especialidad de clínica", description = "Modifica una especialidad de clínica.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EspecialidadClinicaDTO> update(
            @PathVariable Long id,
            @RequestBody EspecialidadClinicaDTO dto) {
        EspecialidadClinicaDTO actualizada = especialidadClinicaService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar especialidad de clínica", description = "Desactiva una especialidad de clínica.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        especialidadClinicaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
