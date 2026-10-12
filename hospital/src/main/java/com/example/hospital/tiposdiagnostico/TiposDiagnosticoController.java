package com.example.hospital.tiposdiagnostico;

import com.example.hospital.tiposdiagnostico.dto.TiposDiagnosticoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-diagnostico")
public class TiposDiagnosticoController {

    private final TiposDiagnosticoService tiposDiagnosticoService;

    public TiposDiagnosticoController(TiposDiagnosticoService tiposDiagnosticoService) {
        this.tiposDiagnosticoService = tiposDiagnosticoService;
    }

    @GetMapping
    public List<TiposDiagnosticoDTO> findAll() {
        return tiposDiagnosticoService.findAll();
    }

    @GetMapping("/{id}")
    public TiposDiagnosticoDTO findById(@PathVariable Long id) {
        return tiposDiagnosticoService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TiposDiagnosticoDTO> crear(@RequestBody TiposDiagnosticoDTO dto) {
        TiposDiagnosticoDTO creado = tiposDiagnosticoService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TiposDiagnosticoDTO> actualizar(
            @PathVariable Long id,
            @RequestBody TiposDiagnosticoDTO dto) {
        TiposDiagnosticoDTO actualizado = tiposDiagnosticoService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tiposDiagnosticoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
