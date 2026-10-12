package com.example.hospital.estadohabitacion;

import com.example.hospital.estadohabitacion.dto.EstadoHabitacionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estado-habitacion")
public class EstadoHabitacionController {

    private final EstadoHabitacionService estadoHabitacionService;

    public EstadoHabitacionController(EstadoHabitacionService estadoHabitacionService) {
        this.estadoHabitacionService = estadoHabitacionService;
    }

    // Consulta de libre lectura para usuarios autenticados
    @GetMapping
    public List<EstadoHabitacionDTO> findAll() {
        return estadoHabitacionService.findAll();
    }

    @GetMapping("/{id}")
    public EstadoHabitacionDTO findById(@PathVariable Long id) {
        return estadoHabitacionService.findById(id);
    }

    // Solo ADMIN puede modificar catálogos base
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EstadoHabitacionDTO> crear(@RequestBody EstadoHabitacionDTO dto) {
        EstadoHabitacionDTO creado = estadoHabitacionService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EstadoHabitacionDTO> actualizar(
            @PathVariable Long id,
            @RequestBody EstadoHabitacionDTO dto) {
        EstadoHabitacionDTO actualizado = estadoHabitacionService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estadoHabitacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
