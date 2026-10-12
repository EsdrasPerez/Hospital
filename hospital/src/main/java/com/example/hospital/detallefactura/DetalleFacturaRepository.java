package com.example.hospital.detallefactura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Long> {

    List<DetalleFactura> findByIdFactura(Long idFactura);

    boolean existsByIdFactura(Long idFactura);
}
