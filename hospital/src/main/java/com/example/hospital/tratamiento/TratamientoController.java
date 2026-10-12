package com.example.hospital.tratamiento;

import com.example.hospital.tratamiento.dto.TratamientoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tratamiento")
public class TratamientoController {

    private final TratamientoService tratamientoService;

    public TratamientoController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    @GetMapping
    public List<TratamientoDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos,
            @RequestParam(required = false) Long idDiagnostico) {
        if (idDiagnostico != null) {
            return tratamientoService.findByDiagnostico(idDiagnostico);
        }
        if (soloActivos) {
            return tratamientoService.findActivos();
        }
        return tratamientoService.findAll();
    }

    @GetMapping("/{id}")
    public TratamientoDTO findById(@PathVariable Long id) {
        return tratamientoService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    public ResponseEntity<TratamientoDTO> save(@RequestBody TratamientoDTO dto) {
        TratamientoDTO creado = tratamientoService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    public ResponseEntity<TratamientoDTO> update(
            @PathVariable Long id,
            @RequestBody TratamientoDTO dto) {
        TratamientoDTO actualizado = tratamientoService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tratamientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
