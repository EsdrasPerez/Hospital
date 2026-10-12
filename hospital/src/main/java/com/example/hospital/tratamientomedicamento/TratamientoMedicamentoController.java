package com.example.hospital.tratamientomedicamento;

import com.example.hospital.tratamientomedicamento.dto.TratamientoMedicamentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tratamiento-medicamento")
public class TratamientoMedicamentoController {

    private final TratamientoMedicamentoService tratamientoMedicamentoService;

    public TratamientoMedicamentoController(TratamientoMedicamentoService tratamientoMedicamentoService) {
        this.tratamientoMedicamentoService = tratamientoMedicamentoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'FARMACEUTICO')")
    @GetMapping
    public List<TratamientoMedicamentoDTO> findAll() {
        return tratamientoMedicamentoService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'FARMACEUTICO')")
    @GetMapping("/{idMedicamento}/{idTratamiento}")
    public TratamientoMedicamentoDTO findById(@PathVariable Long idMedicamento,
                                              @PathVariable Long idTratamiento) {
        return tratamientoMedicamentoService.findById(idMedicamento, idTratamiento);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    public ResponseEntity<TratamientoMedicamentoDTO> crear(@RequestBody TratamientoMedicamentoDTO dto) {
        return new ResponseEntity<>(tratamientoMedicamentoService.crear(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @DeleteMapping("/{idMedicamento}/{idTratamiento}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idMedicamento,
                                         @PathVariable Long idTratamiento) {
        tratamientoMedicamentoService.eliminar(idMedicamento, idTratamiento);
        return ResponseEntity.noContent().build();
    }
}
