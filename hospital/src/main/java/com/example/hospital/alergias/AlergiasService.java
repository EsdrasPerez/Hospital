package com.example.hospital.alergias;

import com.example.hospital.alergias.dto.AlergiasDTO;
import java.util.List;

public interface AlergiasService {
    List<AlergiasDTO> findAll();

    AlergiasDTO findById(Long id);

    AlergiasDTO crear(AlergiasDTO dto);

    AlergiasDTO actualizar(AlergiasDTO dto, Long id);

    void eliminar(Long id);
}
