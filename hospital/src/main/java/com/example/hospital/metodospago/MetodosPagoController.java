package com.example.hospital.metodospago;

import com.example.hospital.metodospago.dto.MetodosPagoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metodos-pago")
public class MetodosPagoController {

    private final MetodosPagoService metodosPagoService;

    public MetodosPagoController(MetodosPagoService metodosPagoService) {
        this.metodosPagoService = metodosPagoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @GetMapping
    public List<MetodosPagoDTO> findAll() {
        return metodosPagoService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'RECEPCIONISTA')")
    @GetMapping("/{id}")
    public MetodosPagoDTO findById(@PathVariable Long id) {
        return metodosPagoService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MetodosPagoDTO> crear(@RequestBody MetodosPagoDTO dto) {
        return new ResponseEntity<>(metodosPagoService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MetodosPagoDTO> actualizar(@PathVariable Long id, @RequestBody MetodosPagoDTO dto) {
        return ResponseEntity.ok(metodosPagoService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        metodosPagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
