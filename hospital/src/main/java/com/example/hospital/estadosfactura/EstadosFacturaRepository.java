package com.example.hospital.estadosfactura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadosFacturaRepository extends JpaRepository<EstadosFactura, Long> {

    List<EstadosFactura> findByActivoTrue();

    Optional<EstadosFactura> findByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdEstadoFacturaNot(String descripcion, Long idEstadoFactura);
}
