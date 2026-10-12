package com.example.hospital.clinicas;

import com.example.hospital.clinicas.dto.ClinicasDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinicas")
public class ClinicasController {

    private final ClinicasService clinicasService;

    public ClinicasController(ClinicasService clinicasService) {
        this.clinicasService = clinicasService;
    }

    @GetMapping
    public List<ClinicasDTO> findAll(@RequestParam(required = false, defaultValue = "false") boolean soloActivas) {
        if (soloActivas) {
            return clinicasService.findActivas();
        }
        return clinicasService.findAll();
    }

    @GetMapping("/{id}")
    public ClinicasDTO findById(@PathVariable Long id) {
        return clinicasService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ClinicasDTO> save(@RequestBody ClinicasDTO dto) {
        ClinicasDTO creado = clinicasService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ClinicasDTO> update(
            @PathVariable Long id,
            @RequestBody ClinicasDTO dto) {
        ClinicasDTO actualizado = clinicasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clinicasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
