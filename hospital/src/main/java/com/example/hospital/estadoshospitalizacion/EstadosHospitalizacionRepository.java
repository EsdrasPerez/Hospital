package com.example.hospital.estadoshospitalizacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadosHospitalizacionRepository extends JpaRepository<EstadosHospitalizacion, Long> {

    List<EstadosHospitalizacion> findByActivoTrue();

    Optional<EstadosHospitalizacion> findByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdEstadoHospNot(String descripcion, Long idEstadoHosp);
}
