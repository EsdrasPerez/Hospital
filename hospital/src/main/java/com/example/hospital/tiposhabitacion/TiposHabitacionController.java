package com.example.hospital.tiposhabitacion;

import com.example.hospital.tiposhabitacion.dto.TiposHabitacionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-habitacion")
public class TiposHabitacionController {

    private final TiposHabitacionService tiposHabitacionService;

    public TiposHabitacionController(TiposHabitacionService tiposHabitacionService) {
        this.tiposHabitacionService = tiposHabitacionService;
    }

    @GetMapping
    public List<TiposHabitacionDTO> findAll() {
        return tiposHabitacionService.findAll();
    }

    @GetMapping("/{id}")
    public TiposHabitacionDTO findById(@PathVariable Long id) {
        return tiposHabitacionService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TiposHabitacionDTO> crear(@RequestBody TiposHabitacionDTO dto) {
        TiposHabitacionDTO creado = tiposHabitacionService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TiposHabitacionDTO> actualizar(
            @PathVariable Long id,
            @RequestBody TiposHabitacionDTO dto) {
        TiposHabitacionDTO actualizado = tiposHabitacionService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tiposHabitacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
