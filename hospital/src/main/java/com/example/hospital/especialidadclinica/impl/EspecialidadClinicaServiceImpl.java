package com.example.hospital.especialidadclinica.impl;

import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.clinicas.ClinicasRepository;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.especialidadclinica.EspecialidadClinica;
import com.example.hospital.especialidadclinica.EspecialidadClinicaRepository;
import com.example.hospital.especialidadclinica.EspecialidadClinicaService;
import com.example.hospital.especialidadclinica.dto.EspecialidadClinicaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EspecialidadClinicaServiceImpl implements EspecialidadClinicaService {

    private final EspecialidadClinicaRepository especialidadClinicaRepository;
    private final ClinicasRepository clinicasRepository;

    public EspecialidadClinicaServiceImpl(EspecialidadClinicaRepository especialidadClinicaRepository,
                                         ClinicasRepository clinicasRepository) {
        this.especialidadClinicaRepository = especialidadClinicaRepository;
        this.clinicasRepository = clinicasRepository;
    }

    private EspecialidadClinicaDTO mapToDTO(EspecialidadClinica e) {
        return new EspecialidadClinicaDTO(
                e.getIdEspecialidad(),
                e.getNombre(),
                e.getDescripcion(),
                e.getActivo()
        );
    }

    private EspecialidadClinica mapToEntity(EspecialidadClinicaDTO dto) {
        EspecialidadClinica e = new EspecialidadClinica();
        e.setNombre(dto.nombre() != null ? dto.nombre().trim() : null);
        e.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        e.setActivo(dto.activo() != null ? dto.activo() : true); // activo por defecto al crear
        return e;
    }

    private void validarDatos(EspecialidadClinicaDTO dto, Long idActual) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de la especialidad clínica no pueden ser nulos");
        }

        // Nombre obligatorio
        if (dto.nombre() == null || dto.nombre().trim().isEmpty()) {
            throw new ReglaNegocioException("El nombre de la especialidad clínica es obligatorio");
        }

        String nombreLimpio = dto.nombre().trim();
        boolean duplicado;
        if (idActual == null) {
            duplicado = especialidadClinicaRepository.existsByNombreIgnoreCase(nombreLimpio);
        } else {
            duplicado = especialidadClinicaRepository.existsByNombreIgnoreCaseAndIdEspecialidadNot(nombreLimpio, idActual);
        }

        if (duplicado) {
            throw new DuplicadoException("Ya existe una especialidad clínica con el nombre: " + nombreLimpio);
        }
    }

    @Override
    public List<EspecialidadClinicaDTO> findAll() {
        return especialidadClinicaRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EspecialidadClinicaDTO> findActivas() {
        return especialidadClinicaRepository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EspecialidadClinicaDTO findById(Long id) {
        EspecialidadClinica entity = especialidadClinicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la especialidad clínica con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public EspecialidadClinicaDTO crear(EspecialidadClinicaDTO dto) {
        validarDatos(dto, null);

        EspecialidadClinica entity = mapToEntity(dto);
        if (entity.getActivo() == null) {
            entity.setActivo(true); // Activa por defecto
        }

        EspecialidadClinica guardada = especialidadClinicaRepository.save(entity);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public EspecialidadClinicaDTO actualizar(EspecialidadClinicaDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de la especialidad clínica es obligatorio");
        }

        EspecialidadClinica existente = especialidadClinicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la especialidad clínica con ID: " + id));

        validarDatos(dto, id);

        existente.setNombre(dto.nombre().trim());
        existente.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        EspecialidadClinica actualizada = especialidadClinicaRepository.save(existente);
        return mapToDTO(actualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de la especialidad clínica es obligatorio");
        }

        EspecialidadClinica existente = especialidadClinicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la especialidad clínica con ID: " + id));

        // Conflicto con dependencias: clínicas activas que usan la especialidad
        if (clinicasRepository.existsByIdEspecialidadAndEstadoClinicaTrue(id)) {
            throw new DuplicadoException("No se puede eliminar la especialidad porque la usan clínicas activas");
        }

        // Borrado lógico
        existente.setActivo(false);
        especialidadClinicaRepository.save(existente);
    }
}
