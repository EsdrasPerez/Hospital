package com.example.hospital.diagnosticos;

import com.example.hospital.diagnosticos.dto.DiagnosticosDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticosController {

    private final DiagnosticosService diagnosticosService;

    public DiagnosticosController(DiagnosticosService diagnosticosService) {
        this.diagnosticosService = diagnosticosService;
    }

    @GetMapping
    public List<DiagnosticosDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos,
            @RequestParam(required = false) Long idTipoDiagnostico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        if (idTipoDiagnostico != null) {
            return diagnosticosService.findByTipo(idTipoDiagnostico);
        }
        if (fecha != null) {
            return diagnosticosService.findByFecha(fecha);
        }
        if (soloActivos) {
            return diagnosticosService.findActivos();
        }
        return diagnosticosService.findAll();
    }

    @GetMapping("/{id}")
    public DiagnosticosDTO findById(@PathVariable Long id) {
        return diagnosticosService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    public ResponseEntity<DiagnosticosDTO> save(@RequestBody DiagnosticosDTO dto) {
        DiagnosticosDTO creado = diagnosticosService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticosDTO> update(
            @PathVariable Long id,
            @RequestBody DiagnosticosDTO dto) {
        DiagnosticosDTO actualizado = diagnosticosService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        diagnosticosService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
