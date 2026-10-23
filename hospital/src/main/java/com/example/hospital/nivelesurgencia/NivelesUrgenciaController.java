package com.example.hospital.nivelesurgencia;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.nivelesurgencia.dto.NivelesUrgenciaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/niveles-urgencia")
@Tag(name = "Niveles de urgencia", description = "Catálogo de niveles de urgencia para clasificar pacientes")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class NivelesUrgenciaController {

    private final NivelesUrgenciaService nivelesUrgenciaService;

    public NivelesUrgenciaController(NivelesUrgenciaService nivelesUrgenciaService) {
        this.nivelesUrgenciaService = nivelesUrgenciaService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping
    @Operation(summary = "Listar niveles de urgencia", description = "Devuelve los niveles de urgencia del catálogo.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public List<NivelesUrgenciaDTO> findAll() {
        return nivelesUrgenciaService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener nivel por ID", description = "Devuelve el detalle de un nivel de urgencia.\n\nRoles permitidos: ADMIN, MEDICO, ENFERMERO, RECEPCIONISTA.")
    public NivelesUrgenciaDTO findById(@PathVariable Long id) {
        return nivelesUrgenciaService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar nivel de urgencia", description = "Agrega un nuevo nivel de urgencia.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<NivelesUrgenciaDTO> crear(@Valid @RequestBody NivelesUrgenciaDTO dto) {
        return new ResponseEntity<>(nivelesUrgenciaService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nivel de urgencia", description = "Modifica un nivel de urgencia.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<NivelesUrgenciaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody NivelesUrgenciaDTO dto) {
        return ResponseEntity.ok(nivelesUrgenciaService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar nivel de urgencia", description = "Desactiva un nivel de urgencia.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        nivelesUrgenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
