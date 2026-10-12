package com.example.hospital.tratamientomedicamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TratamientoMedicamentoRepository
        extends JpaRepository<TratamientoMedicamento, TratamientoMedicamento.TratamientoMedicamentoId> {
    boolean existsByIdMedicamento(Long idMedicamento);

    boolean existsByIdTratamiento(Long idTratamiento);
}
