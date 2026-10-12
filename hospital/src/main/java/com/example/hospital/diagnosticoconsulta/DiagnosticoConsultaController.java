package com.example.hospital.diagnosticoconsulta;

import com.example.hospital.diagnosticoconsulta.dto.DiagnosticoConsultaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnostico-consulta")
public class DiagnosticoConsultaController {

    private final DiagnosticoConsultaService diagnosticoConsultaService;

    public DiagnosticoConsultaController(DiagnosticoConsultaService diagnosticoConsultaService) {
        this.diagnosticoConsultaService = diagnosticoConsultaService;
    }

    @GetMapping
    public List<DiagnosticoConsultaDTO> findAll() {
        return diagnosticoConsultaService.findAll();
    }

    @GetMapping("/consulta/{idConsulta}")
    public List<DiagnosticoConsultaDTO> findByConsulta(@PathVariable Long idConsulta) {
        return diagnosticoConsultaService.findByConsulta(idConsulta);
    }

    @GetMapping("/diagnostico/{idDiagnostico}")
    public List<DiagnosticoConsultaDTO> findByDiagnostico(@PathVariable Long idDiagnostico) {
        return diagnosticoConsultaService.findByDiagnostico(idDiagnostico);
    }

    @GetMapping("/{idDiagnostico}/{idConsulta}")
    public DiagnosticoConsultaDTO findById(
            @PathVariable Long idDiagnostico,
            @PathVariable Long idConsulta) {
        return diagnosticoConsultaService.findById(idDiagnostico, idConsulta);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    public ResponseEntity<DiagnosticoConsultaDTO> save(@RequestBody DiagnosticoConsultaDTO dto) {
        DiagnosticoConsultaDTO creado = diagnosticoConsultaService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @DeleteMapping("/{idDiagnostico}/{idConsulta}")
    public ResponseEntity<Void> delete(
            @PathVariable Long idDiagnostico,
            @PathVariable Long idConsulta) {
        diagnosticoConsultaService.eliminar(idDiagnostico, idConsulta);
        return ResponseEntity.noContent().build();
    }
}
