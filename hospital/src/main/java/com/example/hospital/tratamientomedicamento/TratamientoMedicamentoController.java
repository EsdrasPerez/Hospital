package com.example.hospital.tratamientomedicamento;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.tratamientomedicamento.dto.TratamientoMedicamentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tratamiento-medicamento")
@Tag(name = "Tratamiento – medicamentos", description = "Medicamentos recetados dentro de cada tratamiento")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class TratamientoMedicamentoController {

    private final TratamientoMedicamentoService tratamientoMedicamentoService;

    public TratamientoMedicamentoController(TratamientoMedicamentoService tratamientoMedicamentoService) {
        this.tratamientoMedicamentoService = tratamientoMedicamentoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'FARMACEUTICO')")
    @GetMapping
    @Operation(summary = "Listar medicamentos recetados", description = "Devuelve todas las relaciones tratamiento–medicamento (dosis, frecuencia, etc.).\n\nRoles permitidos: ADMIN, MEDICO, FARMACEUTICO.")
    public List<TratamientoMedicamentoDTO> findAll() {
        return tratamientoMedicamentoService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'FARMACEUTICO')")
    @GetMapping("/{idMedicamento}/{idTratamiento}")
    @Operation(summary = "Obtener receta", description = "Devuelve un medicamento recetado en un tratamiento por su llave compuesta (idMedicamento + idTratamiento).\n\nRoles permitidos: ADMIN, MEDICO, FARMACEUTICO.")
    public TratamientoMedicamentoDTO findById(@PathVariable Long idMedicamento,
                                              @PathVariable Long idTratamiento) {
        return tratamientoMedicamentoService.findById(idMedicamento, idTratamiento);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    @Operation(summary = "Recetar medicamento", description = "Agrega un medicamento a un tratamiento.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<TratamientoMedicamentoDTO> crear(@RequestBody TratamientoMedicamentoDTO dto) {
        return new ResponseEntity<>(tratamientoMedicamentoService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @DeleteMapping("/{idMedicamento}/{idTratamiento}")
    @Operation(summary = "Quitar medicamento del tratamiento", description = "Elimina un medicamento de un tratamiento.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<Void> eliminar(@PathVariable Long idMedicamento,
                                         @PathVariable Long idTratamiento) {
        tratamientoMedicamentoService.eliminar(idMedicamento, idTratamiento);
        return ResponseEntity.noContent().build();
    }
}
