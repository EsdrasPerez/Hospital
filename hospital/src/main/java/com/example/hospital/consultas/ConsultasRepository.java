package com.example.hospital.consultas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultasRepository extends JpaRepository<Consultas, Long> {

    // Listar consultas activas
    List<Consultas> findByActivoTrue();

    // Validar si existe una consulta activa vinculada a una cita
    boolean existsByIdCita(Long idCita);

    boolean existsByIdCitaAndIdConsultaNot(Long idCita, Long idConsulta);

    Optional<Consultas> findByIdCita(Long idCita);
}
