package com.example.hospital.paciente;

import com.example.hospital.paciente.dto.PacienteRequest;
import com.example.hospital.paciente.dto.PacienteResponse;
import com.example.hospital.shared.constants.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/paciente")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Gestión de pacientes del hospital")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "El rol no tiene permiso")
})
public class PacienteController {

    private final PacienteService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO','RECEPCIONISTA')")
    @Operation(summary = "Listar pacientes activos o buscar por criterio")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes")
    public List<PacienteResponse> listar(
            @RequestParam(required = false, defaultValue = "false") boolean todos,
            @RequestParam(required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return service.buscar(q);
        }
        if (todos) {
            return service.listarTodos();
        }
        return service.listar();
    }

    @GetMapping("/dpi/{dpi}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO','RECEPCIONISTA')")
    @Operation(summary = "Obtener un paciente por su DPI")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "El paciente no existe")
    })
    public PacienteResponse obtenerPorDpi(@PathVariable String dpi) {
        return service.buscarPorDpi(dpi);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO','RECEPCIONISTA')")
    @Operation(summary = "Obtener un paciente por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "El paciente no existe")
    })
    public PacienteResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Registrar un paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El DPI ya existe"),
            @ApiResponse(responseCode = "422", description = "Alergia o tipo de sangre inexistente")
    })
    public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteRequest request) {
        PacienteResponse creado = service.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idPaciente()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Actualizar un paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "El paciente no existe"),
            @ApiResponse(responseCode = "409", description = "El DPI ya pertenece a otro paciente"),
            @ApiResponse(responseCode = "422", description = "Alergia o tipo de sangre inexistente")
    })
    public PacienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar un paciente (borrado lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Paciente desactivado"),
            @ApiResponse(responseCode = "404", description = "El paciente no existe")
    })
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
