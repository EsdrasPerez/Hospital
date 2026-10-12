package com.example.hospital.alergias;

import com.example.hospital.alergias.dto.AlergiasDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alergias")
public class AlergiasController {

    private final AlergiasService alergiasService;

    public AlergiasController(AlergiasService alergiasService) {
        this.alergiasService = alergiasService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping
    public List<AlergiasDTO> findAll() {
        return alergiasService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    public AlergiasDTO findById(@PathVariable Long id) {
        return alergiasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    public ResponseEntity<AlergiasDTO> save(@RequestBody AlergiasDTO dto) {
        AlergiasDTO creado = alergiasService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    public ResponseEntity<AlergiasDTO> update(
            @PathVariable Long id,
            @RequestBody AlergiasDTO dto) {
        AlergiasDTO actualizado = alergiasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alergiasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
