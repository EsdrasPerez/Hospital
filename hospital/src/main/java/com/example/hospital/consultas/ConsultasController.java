package com.example.hospital.consultas;

import com.example.hospital.consultas.dto.ConsultasDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
public class ConsultasController {

    private final ConsultasService consultasService;

    public ConsultasController(ConsultasService consultasService) {
        this.consultasService = consultasService;
    }

    @GetMapping
    public List<ConsultasDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idCita) {
        if (idCita != null) {
            return List.of(consultasService.findByCita(idCita));
        }
        if (soloActivas) {
            return consultasService.findActivas();
        }
        return consultasService.findAll();
    }

    @GetMapping("/{id}")
    public ConsultasDTO findById(@PathVariable Long id) {
        return consultasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    public ResponseEntity<ConsultasDTO> save(@RequestBody ConsultasDTO dto) {
        ConsultasDTO creada = consultasService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    public ResponseEntity<ConsultasDTO> update(
            @PathVariable Long id,
            @RequestBody ConsultasDTO dto) {
        ConsultasDTO actualizada = consultasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        consultasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
