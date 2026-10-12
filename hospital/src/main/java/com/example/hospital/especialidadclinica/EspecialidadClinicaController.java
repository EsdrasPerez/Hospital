package com.example.hospital.especialidadclinica;

import com.example.hospital.especialidadclinica.dto.EspecialidadClinicaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidad-clinica")
public class EspecialidadClinicaController {

    private final EspecialidadClinicaService especialidadClinicaService;

    public EspecialidadClinicaController(EspecialidadClinicaService especialidadClinicaService) {
        this.especialidadClinicaService = especialidadClinicaService;
    }

    @GetMapping
    public List<EspecialidadClinicaDTO> findAll(@RequestParam(required = false, defaultValue = "false") boolean soloActivas) {
        if (soloActivas) {
            return especialidadClinicaService.findActivas();
        }
        return especialidadClinicaService.findAll();
    }

    @GetMapping("/{id}")
    public EspecialidadClinicaDTO findById(@PathVariable Long id) {
        return especialidadClinicaService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EspecialidadClinicaDTO> save(@RequestBody EspecialidadClinicaDTO dto) {
        EspecialidadClinicaDTO creada = especialidadClinicaService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadClinicaDTO> update(
            @PathVariable Long id,
            @RequestBody EspecialidadClinicaDTO dto) {
        EspecialidadClinicaDTO actualizada = especialidadClinicaService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        especialidadClinicaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
