package com.example.hospital.medicamentos;

import com.example.hospital.medicamentos.dto.MedicamentosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentosController {

    private final MedicamentosService medicamentosService;

    public MedicamentosController(MedicamentosService medicamentosService) {
        this.medicamentosService = medicamentosService;
    }

    @GetMapping
    public List<MedicamentosDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos,
            @RequestParam(required = false, defaultValue = "false") boolean bajoStock,
            @RequestParam(required = false) String criterio) {
        if (bajoStock) {
            return medicamentosService.findBajoStock();
        }
        if (criterio != null && !criterio.trim().isEmpty()) {
            return medicamentosService.buscar(criterio);
        }
        if (soloActivos) {
            return medicamentosService.findActivos();
        }
        return medicamentosService.findAll();
    }

    @GetMapping("/{id}")
    public MedicamentosDTO findById(@PathVariable Long id) {
        return medicamentosService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FARMACEUTICO', 'MEDICO')")
    @PostMapping
    public ResponseEntity<MedicamentosDTO> save(@RequestBody MedicamentosDTO dto) {
        MedicamentosDTO creado = medicamentosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FARMACEUTICO')")
    @PutMapping("/{id}")
    public ResponseEntity<MedicamentosDTO> update(
            @PathVariable Long id,
            @RequestBody MedicamentosDTO dto) {
        MedicamentosDTO actualizado = medicamentosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FARMACEUTICO')")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<MedicamentosDTO> ajustarStock(
            @PathVariable Long id,
            @RequestParam Integer cambio) {
        MedicamentosDTO actualizado = medicamentosService.ajustarStock(id, cambio);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicamentosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
