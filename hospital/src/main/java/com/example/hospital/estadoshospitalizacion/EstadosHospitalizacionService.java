package com.example.hospital.estadoshospitalizacion;

import com.example.hospital.estadoshospitalizacion.dto.EstadosHospitalizacionDTO;

import java.util.List;

public interface EstadosHospitalizacionService {

    List<EstadosHospitalizacionDTO> findAll();

    List<EstadosHospitalizacionDTO> findActivos();

    EstadosHospitalizacionDTO findById(Long id);

    EstadosHospitalizacionDTO findByDescripcion(String descripcion);

    EstadosHospitalizacionDTO crear(EstadosHospitalizacionDTO dto);

    EstadosHospitalizacionDTO actualizar(EstadosHospitalizacionDTO dto, Long id);

    void eliminar(Long id);
}
