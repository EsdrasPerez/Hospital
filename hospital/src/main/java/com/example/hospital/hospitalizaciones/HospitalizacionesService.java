package com.example.hospital.hospitalizaciones;

import com.example.hospital.hospitalizaciones.dto.HospitalizacionesDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface HospitalizacionesService {
    List<HospitalizacionesDTO> findAll();

    HospitalizacionesDTO findById(Long id);

    HospitalizacionesDTO registrar(HospitalizacionesDTO dto);

    HospitalizacionesDTO actualizar(HospitalizacionesDTO dto, Long id);

    HospitalizacionesDTO darAlta(Long id, LocalDateTime fechaAlta);

    void eliminar(Long id);
}
