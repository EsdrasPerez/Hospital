package com.example.hospital.medicoespecialidades.impl;

import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.especialidadmedico.EspecialidadMedico;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.especialidadmedico.EspecialidadMedicoRepository;
import com.example.hospital.medico.MedicoRepository;
import com.example.hospital.medicoespecialidades.MedicoEspecialidades;
import com.example.hospital.medicoespecialidades.MedicoEspecialidadesRepository;
import com.example.hospital.medicoespecialidades.MedicoEspecialidadesService;
import com.example.hospital.medicoespecialidades.dto.MedicoEspecialidadesDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicoEspecialidadesServiceImpl implements MedicoEspecialidadesService {

    private final MedicoEspecialidadesRepository medicoEspecialidadesRepository;
    private final MedicoRepository medicoRepository;
    private final EspecialidadMedicoRepository especialidadMedicoRepository;

    public MedicoEspecialidadesServiceImpl(MedicoEspecialidadesRepository medicoEspecialidadesRepository,
                                          MedicoRepository medicoRepository,
                                          EspecialidadMedicoRepository especialidadMedicoRepository) {
        this.medicoEspecialidadesRepository = medicoEspecialidadesRepository;
        this.medicoRepository = medicoRepository;
        this.especialidadMedicoRepository = especialidadMedicoRepository;
    }

    private MedicoEspecialidadesDTO mapToDTO(MedicoEspecialidades m) {
        return new MedicoEspecialidadesDTO(
                m.getIdEspecialidad(),
                m.getIdMedico()
        );
    }

    @Override
    public List<MedicoEspecialidadesDTO> findAll() {
        return medicoEspecialidadesRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicoEspecialidadesDTO> findByMedico(Long idMedico) {
        if (idMedico == null) {
            throw new ReglaNegocioException("El ID del médico es obligatorio");
        }
        return medicoEspecialidadesRepository.findByIdMedico(idMedico)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicoEspecialidadesDTO> findByEspecialidad(Long idEspecialidad) {
        if (idEspecialidad == null) {
            throw new ReglaNegocioException("El ID de la especialidad es obligatorio");
        }
        return medicoEspecialidadesRepository.findByIdEspecialidad(idEspecialidad)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MedicoEspecialidadesDTO findById(Long idEspecialidad, Long idMedico) {
        if (idEspecialidad == null || idMedico == null) {
            throw new ReglaNegocioException("Ambas claves (idEspecialidad e idMedico) son obligatorias");
        }
        MedicoEspecialidades.MedicoEspecialidadesId id = new MedicoEspecialidades.MedicoEspecialidadesId(idEspecialidad, idMedico);
        MedicoEspecialidades entity = medicoEspecialidadesRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la asignación para la especialidad " + idEspecialidad + " y médico " + idMedico));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public MedicoEspecialidadesDTO crear(MedicoEspecialidadesDTO dto) {
        if (dto == null || dto.idEspecialidad() == null || dto.idMedico() == null) {
            throw new ReglaNegocioException("El ID de especialidad y el ID de médico son obligatorios");
        }

        // 1. Validar que el médico exista
        if (!medicoRepository.existsById(dto.idMedico())) {
            throw new ReglaNegocioException("El médico con ID " + dto.idMedico() + " no existe");
        }

        // 2. Validar que la especialidad médica exista y esté activa
        EspecialidadMedico esp = especialidadMedicoRepository.findById(dto.idEspecialidad())
                .orElseThrow(() -> new ReglaNegocioException("La especialidad médica con ID " + dto.idEspecialidad() + " no existe"));

        if (esp.getActivo() != null && !esp.getActivo()) {
            throw new ReglaNegocioException("La especialidad médica '" + esp.getNombre() + "' se encuentra inactiva");
        }

        // 3. Validar duplicado del par (idEspecialidad, idMedico)
        if (medicoEspecialidadesRepository.existsByIdEspecialidadAndIdMedico(dto.idEspecialidad(), dto.idMedico())) {
            throw new DuplicadoException("El médico ya tiene asignada esta especialidad");
        }

        MedicoEspecialidades entity = new MedicoEspecialidades(dto.idEspecialidad(), dto.idMedico());
        MedicoEspecialidades guardado = medicoEspecialidadesRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long idEspecialidad, Long idMedico) {
        if (idEspecialidad == null || idMedico == null) {
            throw new ReglaNegocioException("Ambas claves son obligatorias para eliminar la asignación");
        }

        MedicoEspecialidades.MedicoEspecialidadesId id = new MedicoEspecialidades.MedicoEspecialidadesId(idEspecialidad, idMedico);
        if (!medicoEspecialidadesRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe la asignación de especialidad " + idEspecialidad + " para el médico " + idMedico);
        }

        medicoEspecialidadesRepository.deleteById(id);
    }
}
