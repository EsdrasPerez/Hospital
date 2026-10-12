package com.example.hospital.departamentos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartamentosRepository extends JpaRepository<Departamentos, Long> {

    // Validar duplicados de nombre sin distinguir mayúsculas/minúsculas
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdDepartamentoNot(String nombre, Long idDepartamento);

    Optional<Departamentos> findByNombreIgnoreCase(String nombre);
}
