package com.example.hospital.tiposangre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoSangreRepository extends JpaRepository<TipoSangre, Long> {

    // RN-TS-01: descripcion es única (sin distinguir mayúsculas)
    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdTipoDeSangreNot(String descripcion, Long idTipoDeSangre);

    Optional<TipoSangre> findByDescripcionIgnoreCase(String descripcion);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdTipoDeSangreNot(String nombre, Long idTipoDeSangre);

    List<TipoSangre> findByActivoTrue();
}
