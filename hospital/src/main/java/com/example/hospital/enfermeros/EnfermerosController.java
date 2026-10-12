package com.example.hospital.enfermeros;

import com.example.hospital.enfermeros.dto.EnfermerosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enfermeros")
public class EnfermerosController {

    private final EnfermerosService enfermerosService;

    public EnfermerosController(EnfermerosService enfermerosService) {
        this.enfermerosService = enfermerosService;
    }

    @GetMapping
    public List<EnfermerosDTO> findAll(@RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        if (soloActivos) {
            return enfermerosService.findActivos();
        }
        return enfermerosService.findAll();
    }

    @GetMapping("/{id}")
    public EnfermerosDTO findById(@PathVariable Long id) {
        return enfermerosService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EnfermerosDTO> save(@RequestBody EnfermerosDTO dto) {
        EnfermerosDTO creado = enfermerosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EnfermerosDTO> update(
            @PathVariable Long id,
            @RequestBody EnfermerosDTO dto) {
        EnfermerosDTO actualizado = enfermerosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enfermerosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
