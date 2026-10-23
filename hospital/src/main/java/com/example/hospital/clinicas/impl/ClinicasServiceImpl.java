package com.example.hospital.clinicas.impl;

import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.clinicas.Clinicas;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.clinicas.ClinicasRepository;
import com.example.hospital.clinicas.ClinicasService;
import com.example.hospital.clinicas.dto.ClinicasDTO;
import com.example.hospital.departamentos.Departamentos;
import com.example.hospital.departamentos.DepartamentosRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClinicasServiceImpl implements ClinicasService {

    private final ClinicasRepository clinicasRepository;
    private final DepartamentosRepository departamentosRepository;

    private final HospitalizacionesRepository hospitalizacionesRepository;
    // Los estados (Activa) se resuelven por nombre, no por id fijo
    private final EstadosCatalogo estados;

    public ClinicasServiceImpl(ClinicasRepository clinicasRepository,
                               DepartamentosRepository departamentosRepository,
                               HospitalizacionesRepository hospitalizacionesRepository,
                               EstadosCatalogo estados) {
        this.clinicasRepository = clinicasRepository;
        this.departamentosRepository = departamentosRepository;
        this.hospitalizacionesRepository = hospitalizacionesRepository;
        this.estados = estados;
    }

    private ClinicasDTO mapToDTO(Clinicas c) {
        return new ClinicasDTO(
                c.getIdClinica(),
                c.getNombre(),
                c.getDescripcion(),
                c.getUbicacion(),
                c.getTelefono(),
                c.getEstadoClinica(),
                c.getIdDepartamento(),
                c.getIdEspecialidad()
        );
    }

    private Clinicas mapToEntity(ClinicasDTO dto) {
        Clinicas c = new Clinicas();
        c.setNombre(dto.nombre() != null ? dto.nombre().trim() : null);
        c.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        c.setUbicacion(dto.ubicacion() != null ? dto.ubicacion().trim() : null);
        c.setTelefono(dto.telefono() != null ? dto.telefono().trim() : null);
        c.setEstadoClinica(dto.estadoClinica() != null ? dto.estadoClinica() : true); // activo por defecto al crear
        c.setIdDepartamento(dto.idDepartamento());
        c.setIdEspecialidad(dto.idEspecialidad());
        return c;
    }

    private void validarDatos(ClinicasDTO dto, Long idActual) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de la clínica no pueden ser nulos");
        }

        // RN-026: El nombre de la clínica debe ser obligatorio y único
        if (dto.nombre() == null || dto.nombre().trim().isEmpty()) {
            throw new ReglaNegocioException("RN-026: El nombre de la clínica es obligatorio");
        }

        String nombreLimpio = dto.nombre().trim();
        boolean duplicado;
        if (idActual == null) {
            duplicado = clinicasRepository.existsByNombreIgnoreCase(nombreLimpio);
        } else {
            duplicado = clinicasRepository.existsByNombreIgnoreCaseAndIdClinicaNot(nombreLimpio, idActual);
        }

        if (duplicado) {
            throw new DuplicadoException("RN-026: El nombre de la clínica debe ser único. Ya existe una clínica con el nombre: " + nombreLimpio);
        }

        // RN-025: Toda clínica debe pertenecer a un departamento existente
        if (dto.idDepartamento() == null) {
            throw new ReglaNegocioException("RN-025: El departamento es obligatorio. Toda clínica debe pertenecer a un departamento existente");
        }

        Departamentos departamento = departamentosRepository.findById(dto.idDepartamento())
                .orElseThrow(() -> new ReglaNegocioException("RN-025: El departamento con ID " + dto.idDepartamento() + " no existe"));

        if (departamento.getEstado() != null && !departamento.getEstado()) {
            throw new ReglaNegocioException("RN-025: El departamento seleccionado se encuentra inactivo");
        }
    }

    @Override
    public List<ClinicasDTO> findAll() {
        return clinicasRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClinicasDTO> findActivas() {
        return clinicasRepository.findByEstadoClinicaTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClinicasDTO findById(Long id) {
        Clinicas entity = clinicasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la clínica con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public ClinicasDTO crear(ClinicasDTO dto) {
        validarDatos(dto, null);

        Clinicas entity = mapToEntity(dto);
        if (entity.getEstadoClinica() == null) {
            entity.setEstadoClinica(true); // Activa por defecto
        }

        Clinicas guardado = clinicasRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public ClinicasDTO actualizar(ClinicasDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de la clínica es obligatorio");
        }

        Clinicas existente = clinicasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la clínica con ID: " + id));

        validarDatos(dto, id);

        existente.setNombre(dto.nombre().trim());
        existente.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        existente.setUbicacion(dto.ubicacion() != null ? dto.ubicacion().trim() : null);
        existente.setTelefono(dto.telefono() != null ? dto.telefono().trim() : null);
        existente.setIdDepartamento(dto.idDepartamento());
        existente.setIdEspecialidad(dto.idEspecialidad());

        if (dto.estadoClinica() != null) {
            existente.setEstadoClinica(dto.estadoClinica());
        }

        Clinicas actualizado = clinicasRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de la clínica es obligatorio");
        }

        Clinicas existente = clinicasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la clínica con ID: " + id));

        // Conflicto con dependencias: hospitalizaciones en curso en la clínica
        if (hospitalizacionesRepository.existsByIdClinicaAndIdEstadoHosp(id, estados.hospActiva())) {
            throw new DuplicadoException("No se puede eliminar la clínica porque tiene hospitalizaciones activas");
        }

        // RN-028: Eliminación lógica
        existente.setEstadoClinica(false);
        clinicasRepository.save(existente);
    }

    @Override
    public void validarClinicaActivaParaAtencion(Long idClinica) {
        // RN-027: Solo se podrán asignar nuevas atenciones a clínicas activas
        if (idClinica == null) {
            throw new ReglaNegocioException("RN-027: El identificador de la clínica es obligatorio");
        }

        Clinicas clinica = clinicasRepository.findById(idClinica)
                .orElseThrow(() -> new ReglaNegocioException("RN-027: La clínica con ID " + idClinica + " no existe"));

        if (clinica.getEstadoClinica() == null || !clinica.getEstadoClinica()) {
            throw new ReglaNegocioException("RN-027: Solo se podrán asignar nuevas atenciones a clínicas activas. La clínica '" +
                    clinica.getNombre() + "' está inactiva");
        }
    }
}
