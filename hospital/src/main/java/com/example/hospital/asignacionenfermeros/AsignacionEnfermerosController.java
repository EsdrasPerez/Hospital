package com.example.hospital.asignacionenfermeros;

import com.example.hospital.asignacionenfermeros.dto.AsignacionEnfermerosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignacion-enfermeros")
public class AsignacionEnfermerosController {

    private final AsignacionEnfermerosService asignacionEnfermerosService;

    public AsignacionEnfermerosController(AsignacionEnfermerosService asignacionEnfermerosService) {
        this.asignacionEnfermerosService = asignacionEnfermerosService;
    }

    @GetMapping
    public List<AsignacionEnfermerosDTO> findAll() {
        return asignacionEnfermerosService.findAll();
    }

    @GetMapping("/{id}")
    public AsignacionEnfermerosDTO findById(@PathVariable Long id) {
        return asignacionEnfermerosService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AsignacionEnfermerosDTO> save(@RequestBody AsignacionEnfermerosDTO dto) {
        AsignacionEnfermerosDTO creado = asignacionEnfermerosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AsignacionEnfermerosDTO> update(
            @PathVariable Long id,
            @RequestBody AsignacionEnfermerosDTO dto) {
        AsignacionEnfermerosDTO actualizado = asignacionEnfermerosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        asignacionEnfermerosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
