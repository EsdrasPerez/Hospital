package com.example.hospital.nivelesurgencia;

import com.example.hospital.nivelesurgencia.dto.NivelesUrgenciaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/niveles-urgencia")
public class NivelesUrgenciaController {

    private final NivelesUrgenciaService nivelesUrgenciaService;

    public NivelesUrgenciaController(NivelesUrgenciaService nivelesUrgenciaService) {
        this.nivelesUrgenciaService = nivelesUrgenciaService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping
    public List<NivelesUrgenciaDTO> findAll() {
        return nivelesUrgenciaService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    public NivelesUrgenciaDTO findById(@PathVariable Long id) {
        return nivelesUrgenciaService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<NivelesUrgenciaDTO> crear(@RequestBody NivelesUrgenciaDTO dto) {
        return new ResponseEntity<>(nivelesUrgenciaService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<NivelesUrgenciaDTO> actualizar(@PathVariable Long id, @RequestBody NivelesUrgenciaDTO dto) {
        return ResponseEntity.ok(nivelesUrgenciaService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        nivelesUrgenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
