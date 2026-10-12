package com.example.hospital.metodospago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodosPagoRepository extends JpaRepository<MetodosPago, Long> {
    List<MetodosPago> findByActivoTrue();

    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdMetodoPagoNot(String descripcion, Long idMetodoPago);
}
