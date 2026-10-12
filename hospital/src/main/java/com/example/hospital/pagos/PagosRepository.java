package com.example.hospital.pagos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagosRepository extends JpaRepository<Pagos, Long> {

    // Validar si existen pagos asociados a una cuenta
    boolean existsByIdCuenta(Long idCuenta);

    List<Pagos> findByIdCuenta(Long idCuenta);

    List<Pagos> findByIdFactura(Long idFactura);
}
