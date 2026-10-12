package com.example.hospital.tratamientomedicamento.impl;

import com.example.hospital.medicamentos.MedicamentosRepository;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.tratamiento.TratamientoRepository;
import com.example.hospital.tratamientomedicamento.TratamientoMedicamento;
import com.example.hospital.tratamientomedicamento.TratamientoMedicamento.TratamientoMedicamentoId;
import com.example.hospital.tratamientomedicamento.TratamientoMedicamentoRepository;
import com.example.hospital.tratamientomedicamento.TratamientoMedicamentoService;
import com.example.hospital.tratamientomedicamento.dto.TratamientoMedicamentoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TratamientoMedicamentoServiceImpl implements TratamientoMedicamentoService {

    private final TratamientoMedicamentoRepository tratamientoMedicamentoRepository;
    private final TratamientoRepository tratamientoRepository;
    private final MedicamentosRepository medicamentosRepository;

    public TratamientoMedicamentoServiceImpl(TratamientoMedicamentoRepository tratamientoMedicamentoRepository,
                                             TratamientoRepository tratamientoRepository,
                                             MedicamentosRepository medicamentosRepository) {
        this.tratamientoMedicamentoRepository = tratamientoMedicamentoRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.medicamentosRepository = medicamentosRepository;
    }

    private TratamientoMedicamentoDTO mapToDTO(TratamientoMedicamento t) {
        return new TratamientoMedicamentoDTO(t.getIdMedicamento(), t.getIdTratamiento());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TratamientoMedicamentoDTO> findAll() {
        return tratamientoMedicamentoRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TratamientoMedicamentoDTO findById(Long idMedicamento, Long idTratamiento) {
        TratamientoMedicamentoId id = new TratamientoMedicamentoId(idMedicamento, idTratamiento);
        return tratamientoMedicamentoRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la relación tratamiento-medicamento (" + idTratamiento + ", " + idMedicamento + ")"));
    }

    @Override
    @Transactional
    public TratamientoMedicamentoDTO crear(TratamientoMedicamentoDTO dto) {
        if (dto == null || dto.idMedicamento() == null || dto.idTratamiento() == null) {
            throw new ReglaNegocioException("El medicamento y el tratamiento son obligatorios");
        }
        if (!tratamientoRepository.existsById(dto.idTratamiento())) {
            throw new RecursoNoEncontradoException("No existe el tratamiento con ID: " + dto.idTratamiento());
        }
        if (!medicamentosRepository.existsById(dto.idMedicamento())) {
            throw new RecursoNoEncontradoException("No existe el medicamento con ID: " + dto.idMedicamento());
        }
        TratamientoMedicamentoId id = new TratamientoMedicamentoId(dto.idMedicamento(), dto.idTratamiento());
        if (tratamientoMedicamentoRepository.existsById(id)) {
            throw new DuplicadoException("El medicamento ya está asignado a este tratamiento");
        }
        TratamientoMedicamento entity = new TratamientoMedicamento(dto.idMedicamento(), dto.idTratamiento());
        return mapToDTO(tratamientoMedicamentoRepository.save(entity));
    }

    @Override
    @Transactional
    public void eliminar(Long idMedicamento, Long idTratamiento) {
        TratamientoMedicamentoId id = new TratamientoMedicamentoId(idMedicamento, idTratamiento);
        if (!tratamientoMedicamentoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe la relación tratamiento-medicamento indicada");
        }
        tratamientoMedicamentoRepository.deleteById(id);
    }
}
