package com.example.hospital.estadoshospitalizacion;

import com.example.hospital.estadoshospitalizacion.dto.EstadosHospitalizacionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados-hospitalizacion")
public class EstadosHospitalizacionController {

    private final EstadosHospitalizacionService estadosHospitalizacionService;

    public EstadosHospitalizacionController(EstadosHospitalizacionService estadosHospitalizacionService) {
        this.estadosHospitalizacionService = estadosHospitalizacionService;
    }

    @GetMapping
    public List<EstadosHospitalizacionDTO> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        if (soloActivos) {
            return estadosHospitalizacionService.findActivos();
        }
        return estadosHospitalizacionService.findAll();
    }

    @GetMapping("/{id}")
    public EstadosHospitalizacionDTO findById(@PathVariable Long id) {
        return estadosHospitalizacionService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EstadosHospitalizacionDTO> save(@RequestBody EstadosHospitalizacionDTO dto) {
        EstadosHospitalizacionDTO creado = estadosHospitalizacionService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EstadosHospitalizacionDTO> update(
            @PathVariable Long id,
            @RequestBody EstadosHospitalizacionDTO dto) {
        EstadosHospitalizacionDTO actualizado = estadosHospitalizacionService.actualizar(dto, id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        estadosHospitalizacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
