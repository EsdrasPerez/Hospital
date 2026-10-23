package com.example.hospital.usuarios;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
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
    @Operation(summary = "Crear usuario", description = "Crea un usuario del sistema. Obligatorios: `nombreUsuario`, `password` (mínimo 8 caracteres) e `idRol` (sin rol por defecto). Errores: 400 (campos inválidos, con `detalles`), 409 (nombre de usuario repetido), 422 (rol inexistente o inactivo). La respuesta nunca incluye la contraseña.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<UsuariosDTO> save(@Validated(UsuariosDTO.Crear.class) @RequestBody UsuariosDTO dto) {
        UsuariosDTO creado = usuariosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Modifica el nombre, la contraseña (mínimo 8 caracteres), el estado o el rol; solo cambia lo que se envía. Errores: 400, 404, 409 (nombre repetido), 422 (rol inexistente o inactivo; un ADMIN no puede desactivarse ni quitarse el rol a sí mismo; no se puede desactivar ni degradar al último ADMIN activo).\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<UsuariosDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UsuariosDTO dto) {
        UsuariosDTO actualizado = usuariosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Desactiva un usuario del sistema (no puede iniciar sesión y su token deja de ser válido). 422 si es uno mismo o el último ADMIN activo.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuariosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
