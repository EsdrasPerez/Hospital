package com.example.hospital.departamentos;

import com.example.hospital.departamentos.dto.DepartamentosDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
public class DepartamentosController {

    private final DepartamentosService departamentosService;

    public DepartamentosController(DepartamentosService departamentosService) {
        this.departamentosService = departamentosService;
    }

    @GetMapping
    public List<DepartamentosDTO> findAll() {
        return departamentosService.findAll();
    }

    @GetMapping("/{id}")
    public DepartamentosDTO findById(@PathVariable Long id) {
        return departamentosService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DepartamentosDTO> save(@RequestBody DepartamentosDTO dto) {
        DepartamentosDTO creado = departamentosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DepartamentosDTO> update(
            @PathVariable Long id,
            @RequestBody DepartamentosDTO dto) {
        DepartamentosDTO actualizado = departamentosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departamentosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
