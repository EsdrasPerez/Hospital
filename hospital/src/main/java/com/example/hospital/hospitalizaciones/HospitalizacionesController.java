package com.example.hospital.hospitalizaciones;

import com.example.hospital.hospitalizaciones.dto.HospitalizacionesDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/hospitalizaciones")
public class HospitalizacionesController {

    private final HospitalizacionesService hospitalizacionesService;

    public HospitalizacionesController(HospitalizacionesService hospitalizacionesService) {
        this.hospitalizacionesService = hospitalizacionesService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping
    public List<HospitalizacionesDTO> findAll() {
        return hospitalizacionesService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    public HospitalizacionesDTO findById(@PathVariable Long id) {
        return hospitalizacionesService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'RECEPCIONISTA')")
    @PostMapping
    public ResponseEntity<HospitalizacionesDTO> registrar(@RequestBody HospitalizacionesDTO dto) {
        HospitalizacionesDTO creado = hospitalizacionesService.registrar(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    public ResponseEntity<HospitalizacionesDTO> update(
            @PathVariable Long id,
            @RequestBody HospitalizacionesDTO dto) {
        HospitalizacionesDTO actualizado = hospitalizacionesService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping("/{id}/alta")
    public ResponseEntity<HospitalizacionesDTO> darAlta(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaAlta) {
        HospitalizacionesDTO conAlta = hospitalizacionesService.darAlta(id, fechaAlta);
        return ResponseEntity.ok(conAlta);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hospitalizacionesService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
