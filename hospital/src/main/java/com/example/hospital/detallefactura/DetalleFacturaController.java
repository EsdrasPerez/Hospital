package com.example.hospital.detallefactura;

import com.example.hospital.detallefactura.dto.DetalleFacturaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-factura")
public class DetalleFacturaController {

    private final DetalleFacturaService detalleFacturaService;

    public DetalleFacturaController(DetalleFacturaService detalleFacturaService) {
        this.detalleFacturaService = detalleFacturaService;
    }

    @GetMapping
    public List<DetalleFacturaDTO> findAll(@RequestParam(required = false) Long idFactura) {
        if (idFactura != null) {
            return detalleFacturaService.findByFactura(idFactura);
        }
        return detalleFacturaService.findAll();
    }

    @GetMapping("/{id}")
    public DetalleFacturaDTO findById(@PathVariable Long id) {
        return detalleFacturaService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @PostMapping
    public ResponseEntity<DetalleFacturaDTO> save(@RequestBody DetalleFacturaDTO dto) {
        DetalleFacturaDTO creado = detalleFacturaService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PutMapping("/{id}")
    public ResponseEntity<DetalleFacturaDTO> update(
            @PathVariable Long id,
            @RequestBody DetalleFacturaDTO dto) {
        DetalleFacturaDTO actualizado = detalleFacturaService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        detalleFacturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
