package com.example.hospital.cuentas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentasRepository extends JpaRepository<Cuentas, Long> {

    // Listar cuentas activas
    List<Cuentas> findByActivoTrue();

    // Listar cuentas por paciente
    List<Cuentas> findByIdPaciente(Long idPaciente);

    boolean existsByIdPacienteAndActivoTrue(Long idPaciente);

    // Listar cuentas por hospitalización
    List<Cuentas> findByIdHospitalizacion(Long idHospitalizacion);

    // Buscar cuenta activa por hospitalización
    Optional<Cuentas> findByIdHospitalizacionAndActivoTrue(Long idHospitalizacion);

    // Validar si existe una cuenta activa para una hospitalización
    boolean existsByIdHospitalizacionAndActivoTrue(Long idHospitalizacion);

    boolean existsByIdHospitalizacionAndActivoTrueAndIdCuentaNot(Long idHospitalizacion, Long idCuenta);
}
