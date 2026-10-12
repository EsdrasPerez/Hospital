package com.example.hospital.triaje;

import com.example.hospital.triaje.dto.TriajeDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/triaje")
public class TriajeController {

    private final TriajeService triajeService;

    public TriajeController(TriajeService triajeService) {
        this.triajeService = triajeService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping
    public List<TriajeDTO> findAll() {
        return triajeService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    public TriajeDTO findById(@PathVariable Long id) {
        return triajeService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @PostMapping
    public ResponseEntity<TriajeDTO> crear(@RequestBody TriajeDTO dto) {
        return new ResponseEntity<>(triajeService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @PutMapping("/{id}")
    public ResponseEntity<TriajeDTO> actualizar(@PathVariable Long id, @RequestBody TriajeDTO dto) {
        return ResponseEntity.ok(triajeService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        triajeService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
