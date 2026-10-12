package com.example.hospital.departamentos.impl;

import com.example.hospital.departamentos.Departamentos;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.departamentos.DepartamentosRepository;
import com.example.hospital.departamentos.DepartamentosService;
import com.example.hospital.departamentos.dto.DepartamentosDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartamentosServiceImpl implements DepartamentosService {

    private final DepartamentosRepository departamentosRepository;

    public DepartamentosServiceImpl(DepartamentosRepository departamentosRepository) {
        this.departamentosRepository = departamentosRepository;
    }

    private DepartamentosDTO mapToDTO(Departamentos d) {
        return new DepartamentosDTO(
                d.getIdDepartamento(),
                d.getNombre(),
                d.getDescripcion(),
                d.getTelefono(),
                d.getEstado()
        );
    }

    private Departamentos mapToEntity(DepartamentosDTO dto) {
        Departamentos d = new Departamentos();
        d.setNombre(dto.nombre() != null ? dto.nombre().trim() : null);
        d.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        d.setTelefono(dto.telefono() != null ? dto.telefono().trim() : null);
        d.setEstado(dto.estado());
        return d;
    }

    private void validarDatos(DepartamentosDTO dto, Long idActual) {
        // RN-022: El nombre del departamento es obligatorio
        if (dto == null || dto.nombre() == null || dto.nombre().trim().isEmpty()) {
            throw new IllegalArgumentException("RN-022: El nombre del departamento es obligatorio");
        }

        String nombreLimpio = dto.nombre().trim();

        // RN-023: No pueden existir departamentos con el mismo nombre
        boolean duplicado;
        if (idActual == null) {
            duplicado = departamentosRepository.existsByNombreIgnoreCase(nombreLimpio);
        } else {
            duplicado = departamentosRepository.existsByNombreIgnoreCaseAndIdDepartamentoNot(nombreLimpio, idActual);
        }

        if (duplicado) {
            throw new IllegalArgumentException("RN-023: Ya existe un departamento con el nombre: " + nombreLimpio);
        }
    }

    @Override
    public List<DepartamentosDTO> findAll() {
        return departamentosRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DepartamentosDTO findById(Long id) {
        Departamentos entity = departamentosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el departamento con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public DepartamentosDTO crear(DepartamentosDTO dto) {
        validarDatos(dto, null);

        Departamentos entity = mapToEntity(dto);
        if (entity.getEstado() == null) {
            entity.setEstado(true); // Activo por defecto
        }

        Departamentos guardado = departamentosRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public DepartamentosDTO actualizar(DepartamentosDTO dto, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del departamento es obligatorio");
        }

        Departamentos existente = departamentosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el departamento con ID: " + id));

        validarDatos(dto, id);

        existente.setNombre(dto.nombre().trim());
        existente.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        existente.setTelefono(dto.telefono() != null ? dto.telefono().trim() : null);
        if (dto.estado() != null) {
            existente.setEstado(dto.estado());
        }

        Departamentos actualizado = departamentosRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del departamento es obligatorio");
        }

        Departamentos existente = departamentosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el departamento con ID: " + id));

        // RN-024: Eliminación lógica
        existente.setEstado(false);
        departamentosRepository.save(existente);
    }
}
