package com.example.hospital.triaje;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriajeRepository extends JpaRepository<Triaje, Long> {

    List<Triaje> findByActivoTrue();

    boolean existsByNivelDeEmergenciaIgnoreCase(String nivelDeEmergencia);

    boolean existsByNivelDeEmergenciaIgnoreCaseAndIdTriajeNot(String nivelDeEmergencia, Long idTriaje);
}
