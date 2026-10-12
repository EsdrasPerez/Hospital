package com.example.hospital.cuentas;

import com.example.hospital.cuentas.dto.CuentasDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentasController {

    private final CuentasService cuentasService;

    public CuentasController(CuentasService cuentasService) {
        this.cuentasService = cuentasService;
    }

    @GetMapping
    public List<CuentasDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) Long idPaciente,
            @RequestParam(required = false) Long idHospitalizacion) {
        if (idPaciente != null) {
            return cuentasService.findByPaciente(idPaciente);
        }
        if (idHospitalizacion != null) {
            return cuentasService.findByHospitalizacion(idHospitalizacion);
        }
        if (soloActivas) {
            return cuentasService.findActivas();
        }
        return cuentasService.findAll();
    }

    @GetMapping("/{id}")
    public CuentasDTO findById(@PathVariable Long id) {
        return cuentasService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping
    public ResponseEntity<CuentasDTO> save(@RequestBody CuentasDTO dto) {
        CuentasDTO creada = cuentasService.crear(dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PutMapping("/{id}")
    public ResponseEntity<CuentasDTO> update(
            @PathVariable Long id,
            @RequestBody CuentasDTO dto) {
        CuentasDTO actualizada = cuentasService.actualizar(dto, id);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PatchMapping("/{id}/ajustar-saldo")
    public ResponseEntity<CuentasDTO> ajustarSaldo(
            @PathVariable Long id,
            @RequestParam BigDecimal monto) {
        CuentasDTO actualizada = cuentasService.ajustarSaldo(id, monto);
        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cuentasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
