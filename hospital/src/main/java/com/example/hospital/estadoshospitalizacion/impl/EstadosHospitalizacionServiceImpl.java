package com.example.hospital.estadoshospitalizacion.impl;

import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.estadoshospitalizacion.EstadosHospitalizacion;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.estadoshospitalizacion.EstadosHospitalizacionRepository;
import com.example.hospital.estadoshospitalizacion.EstadosHospitalizacionService;
import com.example.hospital.estadoshospitalizacion.dto.EstadosHospitalizacionDTO;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EstadosHospitalizacionServiceImpl implements EstadosHospitalizacionService {

    // Estados base requeridos por los flujos clínicos de hospitalizaciones
    public static final String ACTIVA = "Activa";
    public static final String ALTA = "Alta";
    public static final String TRASLADO = "Traslado";
    public static final String FALLECIMIENTO = "Fallecimiento";

    private static final Set<String> ESTADOS_BASE = Set.of(
            ACTIVA.toUpperCase(),
            ALTA.toUpperCase(),
            TRASLADO.toUpperCase(),
            FALLECIMIENTO.toUpperCase()
    );

    private final EstadosHospitalizacionRepository repository;
    private final HospitalizacionesRepository hospitalizacionesRepository;

    public EstadosHospitalizacionServiceImpl(EstadosHospitalizacionRepository repository,
                                             HospitalizacionesRepository hospitalizacionesRepository) {
        this.repository = repository;
        this.hospitalizacionesRepository = hospitalizacionesRepository;
    }

    private EstadosHospitalizacionDTO mapToDTO(EstadosHospitalizacion e) {
        return new EstadosHospitalizacionDTO(
                e.getIdEstadoHosp(),
                e.getDescripcion(),
                e.getActivo()
        );
    }

    private EstadosHospitalizacion mapToEntity(EstadosHospitalizacionDTO dto) {
        EstadosHospitalizacion e = new EstadosHospitalizacion();
        e.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        e.setActivo(dto.activo() != null ? dto.activo() : true);
        return e;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadosHospitalizacionDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadosHospitalizacionDTO> findActivos() {
        return repository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EstadosHospitalizacionDTO findById(Long id) {
        EstadosHospitalizacion entity = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de hospitalización con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadosHospitalizacionDTO findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ReglaNegocioException("La descripción de búsqueda es obligatoria.");
        }
        EstadosHospitalizacion entity = repository.findByDescripcionIgnoreCase(descripcion.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de hospitalización con descripción: " + descripcion));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public EstadosHospitalizacionDTO crear(EstadosHospitalizacionDTO dto) {
        validarReglasDeNegocio(dto, null);

        EstadosHospitalizacion entity = mapToEntity(dto);
        EstadosHospitalizacion guardado = repository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public EstadosHospitalizacionDTO actualizar(EstadosHospitalizacionDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID es obligatorio para la actualización.");
        }

        EstadosHospitalizacion existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de hospitalización con ID: " + id));

        validarReglasDeNegocio(dto, id);

        // RN-EH-03: Protección de estados base esenciales del sistema
        boolean eraBase = esEstadoBase(existente.getDescripcion());
        if (eraBase && !existente.getDescripcion().equalsIgnoreCase(dto.descripcion().trim())) {
            throw new ReglaNegocioException("No se permite modificar la descripción esencial de los estados base del sistema hospitalario: Activa, Alta, Traslado, Fallecimiento.");
        }

        if (eraBase && Boolean.FALSE.equals(dto.activo())) {
            throw new ReglaNegocioException("No se permite desactivar un estado base esencial del sistema hospitalario.");
        }

        existente.setDescripcion(dto.descripcion().trim());
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        EstadosHospitalizacion actualizado = repository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID no puede ser nulo.");
        }

        EstadosHospitalizacion existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de hospitalización con ID: " + id));

        // RN-EH-04: Protección contra eliminación de estados base
        if (esEstadoBase(existente.getDescripcion())) {
            throw new ReglaNegocioException("No está permitido eliminar los estados base esenciales del sistema hospitalario (" + existente.getDescripcion() + ").");
        }

        // RN-EH-05: Integridad referencial contra hospitalizaciones
        if (hospitalizacionesRepository.existsByIdEstadoHosp(id)) {
            throw new DuplicadoException("No se puede eliminar el estado de hospitalización porque existen pacientes u hospitalizaciones asociadas.");
        }

        // RN-EH-06: Borrado lógico
        existente.setActivo(false);
        repository.save(existente);
    }

    private void validarReglasDeNegocio(EstadosHospitalizacionDTO dto, Long idActualizacion) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos del estado de hospitalización son requeridos.");
        }

        // RN-EH-01: Descripción obligatoria y longitud
        if (dto.descripcion() == null || dto.descripcion().trim().isEmpty()) {
            throw new ReglaNegocioException("La descripción del estado de hospitalización es obligatoria.");
        }

        String desc = dto.descripcion().trim();
        if (desc.length() > 30) {
            throw new ReglaNegocioException("La descripción no puede exceder los 30 caracteres.");
        }

        // RN-EH-02: Unicidad insensible a mayúsculas/minúsculas
        if (idActualizacion == null) {
            if (repository.existsByDescripcionIgnoreCase(desc)) {
                throw new DuplicadoException("Ya existe un estado de hospitalización con la descripción: " + desc);
            }
        } else {
            if (repository.existsByDescripcionIgnoreCaseAndIdEstadoHospNot(desc, idActualizacion)) {
                throw new DuplicadoException("Ya existe otro estado de hospitalización con la descripción: " + desc);
            }
        }
    }

    private boolean esEstadoBase(String descripcion) {
        if (descripcion == null) return false;
        return ESTADOS_BASE.contains(descripcion.trim().toUpperCase());
    }
}
