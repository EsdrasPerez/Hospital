package com.example.hospital.tratamientomedicamento;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.tratamientomedicamento.dto.TratamientoMedicamentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
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
    @Operation(summary = "Listar medicamentos recetados", description = "Devuelve las relaciones tratamiento–medicamento. Filtros opcionales y COMBINABLES: `idTratamiento` (medicamentos de ese tratamiento) e `idMedicamento` (tratamientos que incluyen ese medicamento).\n\nRoles permitidos: ADMIN, MEDICO, FARMACEUTICO.")
    public List<TratamientoMedicamentoDTO> findAll(
            @RequestParam(required = false) Long idTratamiento,
            @RequestParam(required = false) Long idMedicamento) {
        return tratamientoMedicamentoService.buscar(idTratamiento, idMedicamento);
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
    @Operation(summary = "Recetar medicamento", description = "Agrega un medicamento a un tratamiento. El tratamiento y el medicamento deben existir y estar activos (422 en caso contrario); si la relación ya existe responde 409. Recetar NO descuenta ni exige stock (prescribir no es dispensar): un medicamento sin stock puede recetarse.\n\nErrores: 400 (ids ausentes, con `detalles`), 409 (duplicado), 422 (inexistente o inactivo).\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<TratamientoMedicamentoDTO> crear(@Valid @RequestBody TratamientoMedicamentoDTO dto) {
        return new ResponseEntity<>(tratamientoMedicamentoService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @DeleteMapping("/{idMedicamento}/{idTratamiento}")
    @Operation(summary = "Quitar medicamento del tratamiento", description = "Elimina un medicamento de un tratamiento (borrado físico de la relación).\n\nErrores: 404 si la relación no existe.\n\nRoles permitidos: ADMIN, MEDICO.")
    public ResponseEntity<Void> eliminar(@PathVariable Long idMedicamento,
                                         @PathVariable Long idTratamiento) {
        tratamientoMedicamentoService.eliminar(idMedicamento, idTratamiento);
        return ResponseEntity.noContent().build();
    }
}
