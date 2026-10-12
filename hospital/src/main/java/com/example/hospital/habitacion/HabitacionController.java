package com.example.hospital.habitacion;

import com.example.hospital.habitacion.dto.HabitacionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habitacion")
public class HabitacionController {

    private final HabitacionService habitacionService;

    public HabitacionController(HabitacionService habitacionService) {
        this.habitacionService = habitacionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    public List<HabitacionDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false, defaultValue = "false") boolean soloDisponibles) {
        if (soloDisponibles) {
            return habitacionService.findDisponibles();
        }
        if (soloActivas) {
            return habitacionService.findActivas();
        }
        return habitacionService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    public HabitacionDTO findById(@PathVariable Long id) {
        return habitacionService.findById(id);
    }

    @GetMapping("/numero/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    public HabitacionDTO findByNumero(@PathVariable String numero) {
        return habitacionService.findByNumero(numero);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<HabitacionDTO> save(@RequestBody HabitacionDTO dto) {
        HabitacionDTO creada = habitacionService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<HabitacionDTO> update(
            @PathVariable Long id,
            @RequestBody HabitacionDTO dto) {
        HabitacionDTO actualizada = habitacionService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado/{nuevoEstadoId}")
    public ResponseEntity<HabitacionDTO> cambiarEstado(
            @PathVariable Long id,
            @PathVariable Long nuevoEstadoId) {
        HabitacionDTO actualizada = habitacionService.cambiarEstado(id, nuevoEstadoId);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        habitacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
