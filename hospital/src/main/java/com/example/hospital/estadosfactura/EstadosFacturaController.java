package com.example.hospital.estadosfactura;

import com.example.hospital.estadosfactura.dto.EstadosFacturaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados-factura")
public class EstadosFacturaController {

    private final EstadosFacturaService estadosFacturaService;

    public EstadosFacturaController(EstadosFacturaService estadosFacturaService) {
        this.estadosFacturaService = estadosFacturaService;
    }

    @GetMapping
    public List<EstadosFacturaDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        if (soloActivos) {
            return estadosFacturaService.findActivos();
        }
        return estadosFacturaService.findAll();
    }

    @GetMapping("/{id}")
    public EstadosFacturaDTO findById(@PathVariable Long id) {
        return estadosFacturaService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EstadosFacturaDTO> save(@RequestBody EstadosFacturaDTO dto) {
        EstadosFacturaDTO creado = estadosFacturaService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EstadosFacturaDTO> update(
            @PathVariable Long id,
            @RequestBody EstadosFacturaDTO dto) {
        EstadosFacturaDTO actualizado = estadosFacturaService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        estadosFacturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
