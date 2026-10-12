package com.example.hospital.facturas;

import com.example.hospital.facturas.dto.FacturasDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturasController {

    private final FacturasService facturasService;

    public FacturasController(FacturasService facturasService) {
        this.facturasService = facturasService;
    }

    @GetMapping
    public List<FacturasDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idPaciente,
            @RequestParam(required = false) Long idHospitalizacion) {
        if (idPaciente != null) {
            return facturasService.findByPaciente(idPaciente);
        }
        if (idHospitalizacion != null) {
            return facturasService.findByHospitalizacion(idHospitalizacion);
        }
        if (soloActivas) {
            return facturasService.findActivas();
        }
        return facturasService.findAll();
    }

    @GetMapping("/{id}")
    public FacturasDTO findById(@PathVariable Long id) {
        return facturasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @PostMapping
    public ResponseEntity<FacturasDTO> save(@RequestBody FacturasDTO dto) {
        FacturasDTO creada = facturasService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PutMapping("/{id}")
    public ResponseEntity<FacturasDTO> update(
            @PathVariable Long id,
            @RequestBody FacturasDTO dto) {
        FacturasDTO actualizada = facturasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PatchMapping("/{id}/estado/{nuevoEstadoId}")
    public ResponseEntity<FacturasDTO> cambiarEstado(
            @PathVariable Long id,
            @PathVariable Long nuevoEstadoId) {
        FacturasDTO actualizada = facturasService.cambiarEstado(id, nuevoEstadoId);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facturasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
