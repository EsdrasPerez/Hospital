package com.example.hospital.alergias.impl;

import com.example.hospital.alergias.Alergias;
import com.example.hospital.alergias.AlergiasRepository;
import com.example.hospital.alergias.AlergiasService;
import com.example.hospital.alergias.dto.AlergiasDTO;
import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlergiasServiceImpl implements AlergiasService {

    private final AlergiasRepository alergiasRepository;
    private final PacienteRepository pacienteRepository;

    public AlergiasServiceImpl(AlergiasRepository alergiasRepository, PacienteRepository pacienteRepository) {
        this.alergiasRepository = alergiasRepository;
        this.pacienteRepository = pacienteRepository;
    }

    private AlergiasDTO mapToDTO(Alergias entity) {
        return new AlergiasDTO(entity.getIdAlergias(), entity.getNombre(), entity.getDescripcion(), entity.getActivo());
    }

    /** Nombre obligatorio y único (409 si se repite, la columna es UNIQUE); devuelve el nombre sin espacios sobrantes. */
    private String validarNombre(String nombre, Long idActual) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioException("El nombre de la alergia es obligatorio");
        }
        String limpio = nombre.trim();
        boolean duplicado = idActual == null
                ? alergiasRepository.existsByNombreIgnoreCase(limpio)
                : alergiasRepository.existsByNombreIgnoreCaseAndIdAlergiasNot(limpio, idActual);
        if (duplicado) {
            throw new DuplicadoException("Ya existe una alergia con el nombre: " + limpio);
        }
        return limpio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlergiasDTO> findAll() {
        return alergiasRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AlergiasDTO findById(Long id) {
        return mapToDTO(alergiasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la alergia con id: " + id)));
    }

    @Override
    @Transactional
    public AlergiasDTO crear(AlergiasDTO dto) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de la alergia son obligatorios");
        }
        Alergias entity = new Alergias();
        entity.setNombre(validarNombre(dto.nombre(), null));
        entity.setDescripcion(dto.descripcion());
        // La columna `activo` es NOT NULL: por defecto la alergia nace activa
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return mapToDTO(alergiasRepository.save(entity));
    }

    @Override
    @Transactional
    public AlergiasDTO actualizar(AlergiasDTO dto, Long id) {
        if (dto == null || id == null) {
            throw new ReglaNegocioException("Datos para actualizar inválidos");
        }
        Alergias existente = alergiasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la alergia a actualizar con id: " + id));
        existente.setNombre(validarNombre(dto.nombre(), id));
        existente.setDescripcion(dto.descripcion());
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }
        return mapToDTO(alergiasRepository.save(existente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Alergias existente = alergiasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la alergia a eliminar con id: " + id));
        // Conflicto con dependencias: no se desactiva una alergia asignada a pacientes
        if (pacienteRepository.existsByIdAlergias(id)) {
            throw new DuplicadoException("No se puede eliminar la alergia porque está asignada a uno o más pacientes");
        }
        existente.setActivo(false);
        alergiasRepository.save(existente);
    }
}
