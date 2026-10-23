package com.example.hospital.departamentos;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.departamentos.dto.DepartamentosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@Tag(name = "Departamentos", description = "Departamentos o áreas del hospital")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class DepartamentosController {

    private final DepartamentosService departamentosService;

    public DepartamentosController(DepartamentosService departamentosService) {
        this.departamentosService = departamentosService;
    }

    @GetMapping
    @Operation(summary = "Listar departamentos", description = "Devuelve todos los departamentos del hospital (p. ej. Pediatría, Cirugía).\n\nAcceso: cualquier usuario autenticado.")
    public List<DepartamentosDTO> findAll() {
        return departamentosService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener departamento por ID", description = "Devuelve el detalle de un departamento. Responde 404 si no existe.\n\nAcceso: cualquier usuario autenticado.")
    public DepartamentosDTO findById(@PathVariable Long id) {
        return departamentosService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar departamento", description = "Crea un nuevo departamento.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<DepartamentosDTO> save(@Valid @RequestBody DepartamentosDTO dto) {
        DepartamentosDTO creado = departamentosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar departamento", description = "Modifica los datos de un departamento.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<DepartamentosDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartamentosDTO dto) {
        DepartamentosDTO actualizado = departamentosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar departamento", description = "Desactiva un departamento. No se permite si tiene clínicas asociadas.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departamentosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
