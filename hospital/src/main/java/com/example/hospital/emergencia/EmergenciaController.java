package com.example.hospital.emergencia;

import com.example.hospital.emergencia.dto.EmergenciaDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/emergencia")
public class EmergenciaController {

    private final EmergenciaService emergenciaService;

    public EmergenciaController(EmergenciaService emergenciaService) {
        this.emergenciaService = emergenciaService;
    }

    @GetMapping
    public List<EmergenciaDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idPaciente) {
        if (idPaciente != null) {
            return emergenciaService.findByPaciente(idPaciente);
        }
        if (soloActivas) {
            return emergenciaService.findActivas();
        }
        return emergenciaService.findAll();
    }

    @GetMapping("/{id}")
    public EmergenciaDTO findById(@PathVariable Long id) {
        return emergenciaService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @PostMapping
    public ResponseEntity<EmergenciaDTO> save(@RequestBody EmergenciaDTO dto) {
        EmergenciaDTO creada = emergenciaService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @PutMapping("/{id}")
    public ResponseEntity<EmergenciaDTO> update(
            @PathVariable Long id,
            @RequestBody EmergenciaDTO dto) {
        EmergenciaDTO actualizada = emergenciaService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @PatchMapping("/{id}/dar-alta")
    public ResponseEntity<EmergenciaDTO> darAlta(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHoraAlta) {
        EmergenciaDTO dadaDeAlta = emergenciaService.darAlta(id, fechaHoraAlta);
        return ResponseEntity.ok(dadaDeAlta);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        emergenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
