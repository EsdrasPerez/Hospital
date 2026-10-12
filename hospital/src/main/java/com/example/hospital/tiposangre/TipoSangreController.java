package com.example.hospital.tiposangre;

import com.example.hospital.tiposangre.dto.TipoSangreDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-sangre")
public class TipoSangreController {

    private final TipoSangreService tipoSangreService;

    public TipoSangreController(TipoSangreService tipoSangreService) {
        this.tipoSangreService = tipoSangreService;
    }

    // RN-TS-03: Catálogo de solo lectura para usuarios operativos
    @GetMapping
    public List<TipoSangreDTO> findAll() {
        return tipoSangreService.findAll();
    }

    // RN-TS-03: Catálogo de solo lectura para usuarios operativos
    @GetMapping("/{id}")
    public TipoSangreDTO findById(@PathVariable Long id) {
        return tipoSangreService.findById(id);
    }

    // RN-TS-03: Solo el administrador lo modifica
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TipoSangreDTO> crear(@RequestBody TipoSangreDTO dto) {
        TipoSangreDTO creado = tipoSangreService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    // RN-TS-03: Solo el administrador lo modifica
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TipoSangreDTO> actualizar(
            @PathVariable Long id,
            @RequestBody TipoSangreDTO dto) {
        TipoSangreDTO actualizado = tipoSangreService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    // RN-TS-03: Solo el administrador lo modifica
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tipoSangreService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
