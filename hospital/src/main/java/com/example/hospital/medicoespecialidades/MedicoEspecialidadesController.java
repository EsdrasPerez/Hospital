package com.example.hospital.medicoespecialidades;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.medicoespecialidades.dto.MedicoEspecialidadesDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medico-especialidades")
@Tag(name = "Médico – especialidades", description = "Especialidades asignadas a cada médico")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class MedicoEspecialidadesController {

    private final MedicoEspecialidadesService medicoEspecialidadesService;

    public MedicoEspecialidadesController(MedicoEspecialidadesService medicoEspecialidadesService) {
        this.medicoEspecialidadesService = medicoEspecialidadesService;
    }

    @GetMapping
    @Operation(summary = "Listar asignaciones", description = "Devuelve todas las relaciones médico–especialidad.\n\nAcceso: cualquier usuario autenticado.")
    public List<MedicoEspecialidadesDTO> findAll() {
        return medicoEspecialidadesService.findAll();
    }

    @GetMapping("/medico/{idMedico}")
    @Operation(summary = "Especialidades de un médico", description = "Devuelve las especialidades que tiene un médico.\n\nAcceso: cualquier usuario autenticado.")
    public List<MedicoEspecialidadesDTO> findByMedico(@PathVariable Long idMedico) {
        return medicoEspecialidadesService.findByMedico(idMedico);
    }

    @GetMapping("/especialidad/{idEspecialidad}")
    @Operation(summary = "Médicos de una especialidad", description = "Devuelve los médicos que tienen una especialidad; útil para elegir médico al programar una cita.\n\nAcceso: cualquier usuario autenticado.")
    public List<MedicoEspecialidadesDTO> findByEspecialidad(@PathVariable Long idEspecialidad) {
        return medicoEspecialidadesService.findByEspecialidad(idEspecialidad);
    }

    @GetMapping("/{idEspecialidad}/{idMedico}")
    @Operation(summary = "Obtener asignación", description = "Devuelve una relación específica por su llave compuesta (idEspecialidad + idMedico).\n\nAcceso: cualquier usuario autenticado.")
    public MedicoEspecialidadesDTO findById(@PathVariable Long idEspecialidad, @PathVariable Long idMedico) {
        return medicoEspecialidadesService.findById(idEspecialidad, idMedico);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Asignar especialidad a médico", description = "Vincula una especialidad a un médico. No permite duplicados.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<MedicoEspecialidadesDTO> save(@Valid @RequestBody MedicoEspecialidadesDTO dto) {
        MedicoEspecialidadesDTO creado = medicoEspecialidadesService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{idEspecialidad}/{idMedico}")
    @Operation(summary = "Quitar especialidad a médico", description = "Elimina el vínculo entre un médico y una especialidad.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long idEspecialidad, @PathVariable Long idMedico) {
        medicoEspecialidadesService.eliminar(idEspecialidad, idMedico);
        return ResponseEntity.noContent().build();
    }
}
