package com.example.hospital.turnos;

import com.example.hospital.turnos.dto.TurnosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
public class TurnosController {

    private final TurnosService turnosService;

    public TurnosController(TurnosService turnosService) {
        this.turnosService = turnosService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping
    public List<TurnosDTO> findAll() {
        return turnosService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    public TurnosDTO findById(@PathVariable Long id) {
        return turnosService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TurnosDTO> registrar(@RequestBody TurnosDTO dto) {
        TurnosDTO creado = turnosService.registrar(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TurnosDTO> update(
            @PathVariable Long id,
            @RequestBody TurnosDTO dto) {
        TurnosDTO actualizado = turnosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        turnosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
