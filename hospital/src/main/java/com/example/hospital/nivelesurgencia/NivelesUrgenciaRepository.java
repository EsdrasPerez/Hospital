package com.example.hospital.nivelesurgencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NivelesUrgenciaRepository extends JpaRepository<NivelesUrgencia, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNivelUrgenciaNot(String codigo, Long idNivelUrgencia);
}
