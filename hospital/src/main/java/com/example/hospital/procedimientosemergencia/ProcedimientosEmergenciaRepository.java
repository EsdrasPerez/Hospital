package com.example.hospital.procedimientosemergencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcedimientosEmergenciaRepository extends JpaRepository<ProcedimientosEmergencia, Long> {
    boolean existsByIdEmergencia(Long idEmergencia);

    boolean existsByIdEmergenciaAndActivoTrue(Long idEmergencia);
}
