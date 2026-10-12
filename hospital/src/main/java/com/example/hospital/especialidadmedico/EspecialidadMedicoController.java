package com.example.hospital.especialidadmedico;

import com.example.hospital.especialidadmedico.dto.EspecialidadMedicoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidad-medico")
public class EspecialidadMedicoController {

    private final EspecialidadMedicoService especialidadMedicoService;

    public EspecialidadMedicoController(EspecialidadMedicoService especialidadMedicoService) {
        this.especialidadMedicoService = especialidadMedicoService;
    }

    @GetMapping
    public List<EspecialidadMedicoDTO> findAll(@RequestParam(required = false, defaultValue = "false") boolean soloActivas) {
        if (soloActivas) {
            return especialidadMedicoService.findActivas();
        }
        return especialidadMedicoService.findAll();
    }

    @GetMapping("/{id}")
    public EspecialidadMedicoDTO findById(@PathVariable Long id) {
        return especialidadMedicoService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EspecialidadMedicoDTO> save(@RequestBody EspecialidadMedicoDTO dto) {
        EspecialidadMedicoDTO creada = especialidadMedicoService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadMedicoDTO> update(
            @PathVariable Long id,
            @RequestBody EspecialidadMedicoDTO dto) {
        EspecialidadMedicoDTO actualizada = especialidadMedicoService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        especialidadMedicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
