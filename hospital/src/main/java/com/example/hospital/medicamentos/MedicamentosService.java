package com.example.hospital.medicamentos;

import com.example.hospital.medicamentos.dto.MedicamentosDTO;
import java.util.List;

public interface MedicamentosService {
    List<MedicamentosDTO> findAll();
    List<MedicamentosDTO> findActivos();
    MedicamentosDTO findById(Long id);
    List<MedicamentosDTO> buscar(String criterio);
    List<MedicamentosDTO> buscar(String criterio, boolean bajoStock, boolean soloActivos);
    List<MedicamentosDTO> findBajoStock();
    MedicamentosDTO crear(MedicamentosDTO dto);
    MedicamentosDTO actualizar(MedicamentosDTO dto, Long id);
    MedicamentosDTO ajustarStock(Long id, Integer cambioStock);
    void eliminar(Long id);
}
