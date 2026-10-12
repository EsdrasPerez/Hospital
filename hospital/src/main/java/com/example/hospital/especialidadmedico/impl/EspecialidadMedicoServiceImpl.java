package com.example.hospital.especialidadmedico.impl;

import com.example.hospital.especialidadmedico.EspecialidadMedico;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.especialidadmedico.EspecialidadMedicoRepository;
import com.example.hospital.especialidadmedico.EspecialidadMedicoService;
import com.example.hospital.especialidadmedico.dto.EspecialidadMedicoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EspecialidadMedicoServiceImpl implements EspecialidadMedicoService {

    private final EspecialidadMedicoRepository especialidadMedicoRepository;

    public EspecialidadMedicoServiceImpl(EspecialidadMedicoRepository especialidadMedicoRepository) {
        this.especialidadMedicoRepository = especialidadMedicoRepository;
    }

    private EspecialidadMedicoDTO mapToDTO(EspecialidadMedico e) {
        return new EspecialidadMedicoDTO(
                e.getIdEspecialidad(),
                e.getNombre(),
                e.getDescripcion(),
                e.getActivo()
        );
    }

    private EspecialidadMedico mapToEntity(EspecialidadMedicoDTO dto) {
        EspecialidadMedico e = new EspecialidadMedico();
        e.setNombre(dto.nombre() != null ? dto.nombre().trim() : null);
        e.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        e.setActivo(dto.activo());
        return e;
    }

    private void validarDatos(EspecialidadMedicoDTO dto, Long idActual) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la especialidad médica no pueden ser nulos");
        }

        if (dto.nombre() == null || dto.nombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la especialidad médica es obligatorio");
        }

        String nombreLimpio = dto.nombre().trim();
        boolean duplicado;
        if (idActual == null) {
            duplicado = especialidadMedicoRepository.existsByNombreIgnoreCase(nombreLimpio);
        } else {
            duplicado = especialidadMedicoRepository.existsByNombreIgnoreCaseAndIdEspecialidadNot(nombreLimpio, idActual);
        }

        if (duplicado) {
            throw new IllegalArgumentException("Ya existe una especialidad médica con el nombre: " + nombreLimpio);
        }
    }

    @Override
    public List<EspecialidadMedicoDTO> findAll() {
        return especialidadMedicoRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EspecialidadMedicoDTO> findActivas() {
        return especialidadMedicoRepository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EspecialidadMedicoDTO findById(Long id) {
        EspecialidadMedico entity = especialidadMedicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la especialidad médica con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public EspecialidadMedicoDTO crear(EspecialidadMedicoDTO dto) {
        validarDatos(dto, null);

        EspecialidadMedico entity = mapToEntity(dto);
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }

        EspecialidadMedico guardado = especialidadMedicoRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public EspecialidadMedicoDTO actualizar(EspecialidadMedicoDTO dto, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la especialidad médica es obligatorio");
        }

        EspecialidadMedico existente = especialidadMedicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la especialidad médica con ID: " + id));

        validarDatos(dto, id);

        existente.setNombre(dto.nombre().trim());
        existente.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        EspecialidadMedico actualizado = especialidadMedicoRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la especialidad médica es obligatorio");
        }

        EspecialidadMedico existente = especialidadMedicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la especialidad médica con ID: " + id));

        // Borrado lógico
        existente.setActivo(false);
        especialidadMedicoRepository.save(existente);
    }
}
