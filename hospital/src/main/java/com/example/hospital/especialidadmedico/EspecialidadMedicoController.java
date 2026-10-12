package com.example.hospital.especialidadmedico;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.especialidadmedico.dto.EspecialidadMedicoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidad-medico")
@Tag(name = "Especialidades médicas", description = "Catálogo de especialidades que puede tener un médico")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class EspecialidadMedicoController {

    private final EspecialidadMedicoService especialidadMedicoService;

    public EspecialidadMedicoController(EspecialidadMedicoService especialidadMedicoService) {
        this.especialidadMedicoService = especialidadMedicoService;
    }

    @GetMapping
    @Operation(summary = "Listar especialidades médicas", description = "Devuelve el catálogo de especialidades (Cardiología, Pediatría, etc.). Con `soloActivas=true` solo las activas.\n\nAcceso: cualquier usuario autenticado.")
    public List<EspecialidadMedicoDTO> findAll(@RequestParam(required = false, defaultValue = "false") boolean soloActivas) {
        if (soloActivas) {
            return especialidadMedicoService.findActivas();
        }
        return especialidadMedicoService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener especialidad por ID", description = "Devuelve el detalle de una especialidad médica.\n\nAcceso: cualquier usuario autenticado.")
    public EspecialidadMedicoDTO findById(@PathVariable Long id) {
        return especialidadMedicoService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar especialidad", description = "Agrega una nueva especialidad al catálogo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EspecialidadMedicoDTO> save(@RequestBody EspecialidadMedicoDTO dto) {
        EspecialidadMedicoDTO creada = especialidadMedicoService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar especialidad", description = "Modifica una especialidad médica.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<EspecialidadMedicoDTO> update(
            @PathVariable Long id,
            @RequestBody EspecialidadMedicoDTO dto) {
        EspecialidadMedicoDTO actualizada = especialidadMedicoService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar especialidad", description = "Desactiva una especialidad. No se permite si hay médicos que la tienen asignada.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        especialidadMedicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
