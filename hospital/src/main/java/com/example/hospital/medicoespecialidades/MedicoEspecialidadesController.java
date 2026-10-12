package com.example.hospital.medicoespecialidades;

import com.example.hospital.medicoespecialidades.dto.MedicoEspecialidadesDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medico-especialidades")
public class MedicoEspecialidadesController {

    private final MedicoEspecialidadesService medicoEspecialidadesService;

    public MedicoEspecialidadesController(MedicoEspecialidadesService medicoEspecialidadesService) {
        this.medicoEspecialidadesService = medicoEspecialidadesService;
    }

    @GetMapping
    public List<MedicoEspecialidadesDTO> findAll() {
        return medicoEspecialidadesService.findAll();
    }

    @GetMapping("/medico/{idMedico}")
    public List<MedicoEspecialidadesDTO> findByMedico(@PathVariable Long idMedico) {
        return medicoEspecialidadesService.findByMedico(idMedico);
    }

    @GetMapping("/especialidad/{idEspecialidad}")
    public List<MedicoEspecialidadesDTO> findByEspecialidad(@PathVariable Long idEspecialidad) {
        return medicoEspecialidadesService.findByEspecialidad(idEspecialidad);
    }

    @GetMapping("/{idEspecialidad}/{idMedico}")
    public MedicoEspecialidadesDTO findById(@PathVariable Long idEspecialidad, @PathVariable Long idMedico) {
        return medicoEspecialidadesService.findById(idEspecialidad, idMedico);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MedicoEspecialidadesDTO> save(@RequestBody MedicoEspecialidadesDTO dto) {
        MedicoEspecialidadesDTO creado = medicoEspecialidadesService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{idEspecialidad}/{idMedico}")
    public ResponseEntity<Void> delete(@PathVariable Long idEspecialidad, @PathVariable Long idMedico) {
        medicoEspecialidadesService.eliminar(idEspecialidad, idMedico);
        return ResponseEntity.noContent().build();
    }
}
