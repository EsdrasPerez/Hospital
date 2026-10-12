package com.example.hospital.turnos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurnosRepository extends JpaRepository<Turnos, Long> {

    // RN-02: Validar duplicados sin distinguir mayúsculas y minúsculas
    boolean existsByDescripcionIgnoreCase(String descripcion);

    // RN-09: Validar duplicados al actualizar excluyendo el propio turno
    boolean existsByDescripcionIgnoreCaseAndIdTurnoNot(String descripcion, Long idTurno);

    Optional<Turnos> findByDescripcionIgnoreCase(String descripcion);

    List<Turnos> findByActivoTrue();
}
