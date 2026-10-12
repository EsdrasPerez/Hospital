package com.example.hospital.emergencia;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.emergencia.dto.EmergenciaDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/emergencia")
@Tag(name = "Emergencia", description = "Ingresos y atención en el área de emergencias")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class EmergenciaController {

    private final EmergenciaService emergenciaService;

    public EmergenciaController(EmergenciaService emergenciaService) {
        this.emergenciaService = emergenciaService;
    }

    @GetMapping
    @Operation(summary = "Listar ingresos a emergencia", description = "Devuelve los ingresos a emergencia. Filtros opcionales:\n- `idPaciente`: ingresos de un paciente\n- `soloActivas=true`: solo registros activos\n\nAcceso: cualquier usuario autenticado.")
    public List<EmergenciaDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idPaciente) {
        if (idPaciente != null) {
            return emergenciaService.findByPaciente(idPaciente);
        }
        if (soloActivas) {
            return emergenciaService.findActivas();
        }
        return emergenciaService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ingreso por ID", description = "Devuelve el detalle de un ingreso a emergencia. Responde 404 si no existe.\n\nAcceso: cualquier usuario autenticado.")
    public EmergenciaDTO findById(@PathVariable Long id) {
        return emergenciaService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @PostMapping
    @Operation(summary = "Registrar ingreso a emergencia", description = "Registra la llegada de un paciente a emergencia con su motivo, signos vitales, nivel de triaje, médico y cama. Permite `nombreTemporal` cuando aún no se conoce la identidad del paciente.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO.")
    public ResponseEntity<EmergenciaDTO> save(@RequestBody EmergenciaDTO dto) {
        EmergenciaDTO creada = emergenciaService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ingreso", description = "Modifica los datos de un ingreso a emergencia.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO.")
    public ResponseEntity<EmergenciaDTO> update(
            @PathVariable Long id,
            @RequestBody EmergenciaDTO dto) {
        EmergenciaDTO actualizada = emergenciaService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @PatchMapping("/{id}/dar-alta")
    @Operation(summary = "Dar de alta de emergencia", description = "Registra la salida del paciente de emergencia. Si no se envía `fechaHoraAlta`, se usa la fecha y hora actual.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO.")
    public ResponseEntity<EmergenciaDTO> darAlta(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHoraAlta) {
        EmergenciaDTO dadaDeAlta = emergenciaService.darAlta(id, fechaHoraAlta);
        return ResponseEntity.ok(dadaDeAlta);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ingreso", description = "Desactiva un ingreso a emergencia (borrado lógico).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        emergenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
