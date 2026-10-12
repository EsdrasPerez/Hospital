package com.example.hospital.procedimientosemergencia;

import com.example.hospital.procedimientosemergencia.dto.ProcedimientosEmergenciaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procedimientos-emergencia")
public class ProcedimientosEmergenciaController {

    private final ProcedimientosEmergenciaService procedimientosEmergenciaService;

    public ProcedimientosEmergenciaController(ProcedimientosEmergenciaService procedimientosEmergenciaService) {
        this.procedimientosEmergenciaService = procedimientosEmergenciaService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @GetMapping
    public List<ProcedimientosEmergenciaDTO> findAll() {
        return procedimientosEmergenciaService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    @GetMapping("/{id}")
    public ProcedimientosEmergenciaDTO findById(@PathVariable Long id) {
        return procedimientosEmergenciaService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    public ResponseEntity<ProcedimientosEmergenciaDTO> crear(@RequestBody ProcedimientosEmergenciaDTO dto) {
        return new ResponseEntity<>(procedimientosEmergenciaService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    public ResponseEntity<ProcedimientosEmergenciaDTO> actualizar(@PathVariable Long id, @RequestBody ProcedimientosEmergenciaDTO dto) {
        return ResponseEntity.ok(procedimientosEmergenciaService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        procedimientosEmergenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
