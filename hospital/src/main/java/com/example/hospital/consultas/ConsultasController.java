package com.example.hospital.consultas;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.consultas.dto.ConsultasDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@Tag(name = "Consultas", description = "Registro de la atención médica (signos vitales y síntomas) derivada de una cita")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class ConsultasController {

    private final ConsultasService consultasService;

    public ConsultasController(ConsultasService consultasService) {
        this.consultasService = consultasService;
    }

    @GetMapping
    @Operation(summary = "Listar consultas", description = "Devuelve las consultas médicas. Filtros opcionales:\n- `idCita`: la consulta asociada a esa cita\n- `soloActivas=true`: solo consultas activas\n\nAcceso: cualquier usuario autenticado.")
    public List<ConsultasDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idCita) {
        if (idCita != null) {
            return List.of(consultasService.findByCita(idCita));
        }
        if (soloActivas) {
            return consultasService.findActivas();
        }
        return consultasService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener consulta por ID", description = "Devuelve el detalle de una consulta (síntomas, peso, talla, temperatura, presión, frecuencia cardíaca). Responde 404 si no existe.\n\nAcceso: cualquier usuario autenticado.")
    public ConsultasDTO findById(@PathVariable Long id) {
        return consultasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    @Operation(summary = "Registrar consulta", description = "Registra la atención de una cita: síntomas y signos vitales. Una cita solo puede tener una consulta.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<ConsultasDTO> save(@RequestBody ConsultasDTO dto) {
        ConsultasDTO creada = consultasService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar consulta", description = "Corrige los datos registrados en una consulta.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<ConsultasDTO> update(
            @PathVariable Long id,
            @RequestBody ConsultasDTO dto) {
        ConsultasDTO actualizada = consultasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar consulta", description = "Desactiva una consulta (borrado lógico).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        consultasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
