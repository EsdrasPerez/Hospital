package com.example.hospital.citas;

import com.example.hospital.citas.dto.CitasDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitasController {

    private final CitasService citasService;

    public CitasController(CitasService citasService) {
        this.citasService = citasService;
    }

    @GetMapping
    public List<CitasDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idPaciente,
            @RequestParam(required = false) Long idMedico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        if (idPaciente != null) {
            return citasService.findByPaciente(idPaciente);
        }
        if (idMedico != null) {
            return citasService.findByMedico(idMedico);
        }
        if (fecha != null) {
            return citasService.findByFecha(fecha);
        }
        if (soloActivas) {
            return citasService.findActivas();
        }
        return citasService.findAll();
    }

    @GetMapping("/{id}")
    public CitasDTO findById(@PathVariable Long id) {
        return citasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @PostMapping
    public ResponseEntity<CitasDTO> save(@RequestBody CitasDTO dto) {
        CitasDTO creada = citasService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @PutMapping("/{id}")
    public ResponseEntity<CitasDTO> update(
            @PathVariable Long id,
            @RequestBody CitasDTO dto) {
        CitasDTO actualizada = citasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'MEDICO')")
    @PatchMapping("/{id}/estado/{nuevoIdEstado}")
    public ResponseEntity<CitasDTO> cambiarEstado(
            @PathVariable Long id,
            @PathVariable Long nuevoIdEstado) {
        CitasDTO actualizada = citasService.cambiarEstado(id, nuevoIdEstado);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        citasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
