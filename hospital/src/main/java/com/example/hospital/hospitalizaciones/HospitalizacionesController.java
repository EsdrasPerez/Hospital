package com.example.hospital.hospitalizaciones;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.hospitalizaciones.dto.HospitalizacionesDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/hospitalizaciones")
@Tag(name = "Hospitalizaciones", description = "Ingreso, seguimiento y alta de pacientes hospitalizados")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class HospitalizacionesController {

    private final HospitalizacionesService hospitalizacionesService;

    public HospitalizacionesController(HospitalizacionesService hospitalizacionesService) {
        this.hospitalizacionesService = hospitalizacionesService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping
    @Operation(summary = "Listar hospitalizaciones", description = "Devuelve todas las hospitalizaciones (activas e históricas).\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public List<HospitalizacionesDTO> findAll() {
        return hospitalizacionesService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/activas")
    @Operation(summary = "Pacientes hospitalizados actualmente", description = "**Nuevo.** Devuelve solo las hospitalizaciones en curso (estado Activa). Sirve para el censo de pacientes internados.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public List<HospitalizacionesDTO> findActivas() {
        return hospitalizacionesService.findActivas();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/paciente/{idPaciente}")
    @Operation(summary = "Hospitalizaciones de un paciente", description = "**Nuevo.** Devuelve el historial de hospitalizaciones de un paciente. Responde 404 si el paciente no existe.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public List<HospitalizacionesDTO> findByPaciente(@PathVariable Long idPaciente) {
        return hospitalizacionesService.findByPaciente(idPaciente);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener hospitalización por ID", description = "Devuelve el detalle de una hospitalización. Responde 404 si no existe.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public HospitalizacionesDTO findById(@PathVariable Long id) {
        return hospitalizacionesService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'RECEPCIONISTA')")
    @PostMapping
    @Operation(summary = "Ingresar paciente", description = "Registra el ingreso de un paciente a una habitación con su médico responsable y clínica. Valida que el paciente no tenga otra hospitalización activa y que la habitación tenga cupo.\n\nRoles permitidos: ADMIN, MEDICO, RECEPCIONISTA.")
    public ResponseEntity<HospitalizacionesDTO> registrar(@RequestBody HospitalizacionesDTO dto) {
        HospitalizacionesDTO creado = hospitalizacionesService.registrar(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar hospitalización", description = "Modifica datos de una hospitalización (motivo, diagnóstico de ingreso, habitación, médico).\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<HospitalizacionesDTO> update(
            @PathVariable Long id,
            @RequestBody HospitalizacionesDTO dto) {
        HospitalizacionesDTO actualizado = hospitalizacionesService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping("/{id}/alta")
    @Operation(summary = "Dar de alta", description = "Registra el alta del paciente, cierra la hospitalización y libera la habitación. Si no se envía `fechaAlta`, se usa la fecha y hora actual.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<HospitalizacionesDTO> darAlta(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaAlta) {
        HospitalizacionesDTO conAlta = hospitalizacionesService.darAlta(id, fechaAlta);
        return ResponseEntity.ok(conAlta);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar hospitalización", description = "Desactiva una hospitalización (borrado lógico).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hospitalizacionesService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
