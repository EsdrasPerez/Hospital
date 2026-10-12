package com.example.hospital.pagos;

import com.example.hospital.pagos.dto.PagosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagosController {

    private final PagosService pagosService;

    public PagosController(PagosService pagosService) {
        this.pagosService = pagosService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping
    public List<PagosDTO> findAll() {
        return pagosService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/{id}")
    public PagosDTO findById(@PathVariable Long id) {
        return pagosService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping
    public ResponseEntity<PagosDTO> crear(@RequestBody PagosDTO dto) {
        return new ResponseEntity<>(pagosService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PagosDTO> actualizar(@PathVariable Long id, @RequestBody PagosDTO dto) {
        return ResponseEntity.ok(pagosService.actualizar(dto, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
