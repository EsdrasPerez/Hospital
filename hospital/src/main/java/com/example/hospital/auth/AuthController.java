package com.example.hospital.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.hospital.auth.dto.LoginRequest;
import com.example.hospital.auth.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión, registro de usuarios y datos del usuario actual")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Recibe usuario y contraseña y, si son válidos, devuelve un token JWT. Ese token se envía en el encabezado `Authorization: Bearer <token>` en el resto de peticiones (en Swagger, botón **Authorize**). Responde 401 si las credenciales son inválidas.\n\nAcceso: público (no requiere token).")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(401).body("Error: Credenciales inválidas. " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario de acceso al sistema con contraseña cifrada. Responde 400 si el nombre de usuario ya existe.\n\nRoles permitidos: ADMIN.")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok("Usuario '" + request.username() + "' creado exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Usuario actual", description = "**Nuevo.** Devuelve el nombre de usuario y los roles del token con el que se hace la petición. Útil para que el frontend sepa quién inició sesión y qué menús mostrar.\n\nAcceso: cualquier usuario autenticado.")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "roles", roles
        ));
    }
}
