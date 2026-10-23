package com.example.hospital.alergias;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlergiasRepository extends JpaRepository<Alergias, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdAlergiasNot(String nombre, Long idAlergias);
}
