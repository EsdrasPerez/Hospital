package com.example.hospital.rol;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.rol.dto.RolDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rol")
@Tag(name = "Roles", description = "Roles de usuario del sistema (ADMIN, MEDICO, ENFERMERO, etc.)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Listar roles", description = "Devuelve todos los roles del sistema.\n\nRoles permitidos: ADMIN.")
    public List<RolDTO> findAll() {
        return rolService.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID", description = "Devuelve el detalle de un rol.\n\nRoles permitidos: ADMIN.")
    public RolDTO findById(@PathVariable Long id) {
        return rolService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Registrar rol", description = "Crea un nuevo rol.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<RolDTO> crear(@RequestBody RolDTO dto) {
        return new ResponseEntity<>(rolService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol", description = "Modifica un rol.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<RolDTO> actualizar(@PathVariable Long id, @RequestBody RolDTO dto) {
        return ResponseEntity.ok(rolService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol", description = "Elimina un rol. No se permite si hay usuarios con ese rol.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
