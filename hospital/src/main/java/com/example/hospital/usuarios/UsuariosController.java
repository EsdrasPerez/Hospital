package com.example.hospital.usuarios;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.usuarios.dto.UsuariosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Usuarios con acceso al sistema")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso")
})
public class UsuariosController {

    private final UsuariosService usuariosService;

    public UsuariosController(UsuariosService usuariosService) {
        this.usuariosService = usuariosService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Devuelve los usuarios del sistema (sin contraseñas).\n\nRoles permitidos: ADMIN.")
    public List<UsuariosDTO> findAll() {
        return usuariosService.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Devuelve el detalle de un usuario.\n\nRoles permitidos: ADMIN.")
    public UsuariosDTO findById(@PathVariable Long id) {
        return usuariosService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Crea un usuario del sistema con su rol.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<UsuariosDTO> save(@RequestBody UsuariosDTO dto) {
        UsuariosDTO creado = usuariosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Modifica los datos o el rol de un usuario.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<UsuariosDTO> update(
            @PathVariable Long id,
            @RequestBody UsuariosDTO dto) {
        UsuariosDTO actualizado = usuariosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Desactiva un usuario del sistema.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuariosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
