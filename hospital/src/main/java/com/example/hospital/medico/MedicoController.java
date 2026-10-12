package com.example.hospital.medico;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.medico.dto.MedicoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medico")
@Tag(name = "Médicos", description = "Personal médico del hospital")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @Operation(summary = "Listar / buscar médicos", description = "Devuelve los médicos. Filtros opcionales:\n- `criterio`: busca por nombre, apellido o número de colegiado\n- `soloActivos=true`: solo médicos activos\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public List<MedicoDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos,
            @RequestParam(required = false) String criterio) {
        if (criterio != null && !criterio.trim().isEmpty()) {
            return medicoService.buscar(criterio);
        }
        if (soloActivos) {
            return medicoService.findActivos();
        }
        return medicoService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @Operation(summary = "Obtener médico por ID", description = "Devuelve los datos de un médico. Responde 404 si no existe.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public MedicoDTO findById(@PathVariable Long id) {
        return medicoService.findById(id);
    }

    @GetMapping("/colegiado/{noColegiado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @Operation(summary = "Buscar médico por colegiado", description = "Devuelve el médico con el número de colegiado indicado. Responde 404 si no existe.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public MedicoDTO findByNoColegiado(@PathVariable String noColegiado) {
        return medicoService.findByNoColegiado(noColegiado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar médico", description = "Da de alta a un nuevo médico. El número de colegiado debe ser único.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<MedicoDTO> save(@RequestBody MedicoDTO dto) {
        MedicoDTO creado = medicoService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar médico", description = "Modifica los datos de un médico.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<MedicoDTO> update(
            @PathVariable Long id,
            @RequestBody MedicoDTO dto) {
        MedicoDTO actualizado = medicoService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar / desactivar médico", description = "Activa (`nuevoEstado=true`) o desactiva (`false`) a un médico sin borrarlo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<MedicoDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean nuevoEstado) {
        MedicoDTO actualizado = medicoService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar médico", description = "Desactiva a un médico. No se permite si tiene citas activas u hospitalizaciones a su cargo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
