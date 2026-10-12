package com.example.hospital.estadocita;

import com.example.hospital.estadocita.dto.EstadoCitaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estado-cita")
public class EstadoCitaController {

    private final EstadoCitaService estadoCitaService;

    public EstadoCitaController(EstadoCitaService estadoCitaService) {
        this.estadoCitaService = estadoCitaService;
    }

    @GetMapping
    public List<EstadoCitaDTO> findAll() {
        return estadoCitaService.findAll();
    }

    @GetMapping("/{id}")
    public EstadoCitaDTO findById(@PathVariable Long id) {
        return estadoCitaService.findById(id);
    }

    // Endpoint auxiliar para verificar si una transición entre estados es válida
    @GetMapping("/validar-transicion")
    public ResponseEntity<Map<String, Object>> validarTransicion(
            @RequestParam String origen,
            @RequestParam String destino) {
        boolean permitida = estadoCitaService.esTransicionPermitida(origen, destino);
        return ResponseEntity.ok(Map.of(
                "origen", origen,
                "destino", destino,
                "permitida", permitida
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EstadoCitaDTO> crear(@RequestBody EstadoCitaDTO dto) {
        EstadoCitaDTO creado = estadoCitaService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EstadoCitaDTO> actualizar(
            @PathVariable Long id,
            @RequestBody EstadoCitaDTO dto) {
        EstadoCitaDTO actualizado = estadoCitaService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estadoCitaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
