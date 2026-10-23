package com.example.hospital.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuariosRepository extends JpaRepository<Usuarios, Long> {
    Optional<Usuarios> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByNombreUsuarioIgnoreCaseAndIdUsuarioNot(String nombreUsuario, Long idUsuario);

    /** Usuarios activos con el rol indicado (para proteger al último ADMIN y al eliminar roles). */
    long countByIdRolAndEstadoTrue(Long idRol);

    boolean existsByIdRolAndEstadoTrue(Long idRol);
}
