package com.example.hospital.asignacionenfermeros;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.asignacionenfermeros.dto.AsignacionEnfermerosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignacion-enfermeros")
@Tag(name = "Asignación de enfermeros", description = "Asignación de enfermeros a pacientes hospitalizados por fecha y turno")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class AsignacionEnfermerosController {

    private final AsignacionEnfermerosService asignacionEnfermerosService;

    public AsignacionEnfermerosController(AsignacionEnfermerosService asignacionEnfermerosService) {
        this.asignacionEnfermerosService = asignacionEnfermerosService;
    }

    @GetMapping
    @Operation(summary = "Listar asignaciones", description = "Devuelve todas las asignaciones de enfermeros a hospitalizaciones.\n\nAcceso: cualquier usuario autenticado.")
    public List<AsignacionEnfermerosDTO> findAll() {
        return asignacionEnfermerosService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener asignación por ID", description = "Devuelve el detalle de una asignación específica (enfermero, hospitalización, fecha y turno).\n\nAcceso: cualquier usuario autenticado.")
    public AsignacionEnfermerosDTO findById(@PathVariable Long id) {
        return asignacionEnfermerosService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Asignar enfermero", description = "Asigna un enfermero a una hospitalización activa en una fecha y turno. No permite asignaciones duplicadas (mismo enfermero, hospitalización, fecha y turno).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<AsignacionEnfermerosDTO> save(@RequestBody AsignacionEnfermerosDTO dto) {
        AsignacionEnfermerosDTO creado = asignacionEnfermerosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar asignación", description = "Modifica el enfermero, la fecha o el turno de una asignación existente.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<AsignacionEnfermerosDTO> update(
            @PathVariable Long id,
            @RequestBody AsignacionEnfermerosDTO dto) {
        AsignacionEnfermerosDTO actualizado = asignacionEnfermerosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar asignación", description = "Elimina/desactiva una asignación de enfermero.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        asignacionEnfermerosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/enfermero/{idEnfermero}")
    @Operation(summary = "Asignaciones de un enfermero", description = "**Nuevo.** Devuelve todas las asignaciones de un enfermero; sirve para ver su carga de trabajo o su calendario de turnos. Responde 404 si el enfermero no existe.\n\nAcceso: cualquier usuario autenticado.")
    public List<AsignacionEnfermerosDTO> findByEnfermero(@PathVariable Long idEnfermero) {
        return asignacionEnfermerosService.findByEnfermero(idEnfermero);
    }

    @GetMapping("/hospitalizacion/{idHospitalizacion}")
    @Operation(summary = "Enfermeros de una hospitalización", description = "**Nuevo.** Devuelve los enfermeros asignados a una hospitalización (quién atiende al paciente en cada turno). Responde 404 si la hospitalización no existe.\n\nAcceso: cualquier usuario autenticado.")
    public List<AsignacionEnfermerosDTO> findByHospitalizacion(@PathVariable Long idHospitalizacion) {
        return asignacionEnfermerosService.findByHospitalizacion(idHospitalizacion);
    }
}
