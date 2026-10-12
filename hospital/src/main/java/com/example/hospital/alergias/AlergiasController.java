package com.example.hospital.alergias;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.alergias.dto.AlergiasDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alergias")
@Tag(name = "Alergias", description = "Catálogo de alergias que se pueden asignar a los pacientes")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class AlergiasController {

    private final AlergiasService alergiasService;

    public AlergiasController(AlergiasService alergiasService) {
        this.alergiasService = alergiasService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping
    @Operation(summary = "Listar alergias", description = "Devuelve todas las alergias registradas en el catálogo. Se usa para llenar listas desplegables al registrar o editar un paciente.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public List<AlergiasDTO> findAll() {
        return alergiasService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener alergia por ID", description = "Devuelve el detalle de una alergia específica. Responde 404 si no existe.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public AlergiasDTO findById(@PathVariable Long id) {
        return alergiasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    @Operation(summary = "Registrar alergia", description = "Agrega una nueva alergia al catálogo. Valida que el nombre no esté duplicado.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<AlergiasDTO> save(@RequestBody AlergiasDTO dto) {
        AlergiasDTO creado = alergiasService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar alergia", description = "Modifica los datos de una alergia existente.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<AlergiasDTO> update(
            @PathVariable Long id,
            @RequestBody AlergiasDTO dto) {
        AlergiasDTO actualizado = alergiasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar alergia", description = "Desactiva una alergia del catálogo. No se permite si hay pacientes que la tienen asignada.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alergiasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
