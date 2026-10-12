package com.example.hospital.medicamentos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicamentosRepository extends JpaRepository<Medicamentos, Long> {

    List<Medicamentos> findByActivoTrue();

    Optional<Medicamentos> findByNombreComercialIgnoreCase(String nombreComercial);

    boolean existsByNombreComercialIgnoreCase(String nombreComercial);

    boolean existsByNombreComercialIgnoreCaseAndIdMedicamentoNot(String nombreComercial, Long idMedicamento);

    List<Medicamentos> findByNombreComercialContainingIgnoreCaseOrNombreGenericoContainingIgnoreCase(
            String nombreComercial, String nombreGenerico);

    List<Medicamentos> findByStockLessThanEqualAndActivoTrue(Integer stock);
}
