package com.example.hospital.medico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    // Validar colegiado único
    boolean existsByNoColegiado(String noColegiado);

    boolean existsByNoColegiadoAndIdMedicoNot(String noColegiado, Long idMedico);

    Optional<Medico> findByNoColegiado(String noColegiado);

    List<Medico> findByEstadoTrue();

    // Validar email único si se proporciona
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdMedicoNot(String email, Long idMedico);

    // Búsqueda por nombre o apellido
    List<Medico> findByNombreContainingIgnoreCaseOrApellidoPaternoContainingIgnoreCase(
            String nombre, String apellidoPaterno);
}
