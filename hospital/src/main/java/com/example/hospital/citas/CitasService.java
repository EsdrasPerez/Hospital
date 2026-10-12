package com.example.hospital.citas;

import com.example.hospital.citas.dto.CitasDTO;

import java.time.LocalDate;
import java.util.List;

public interface CitasService {

    List<CitasDTO> findAll();

    List<CitasDTO> findActivas();

    List<CitasDTO> findByPaciente(Long idPaciente);

    List<CitasDTO> findByMedico(Long idMedico);

    List<CitasDTO> findByFecha(LocalDate fecha);

    CitasDTO findById(Long id);

    CitasDTO crear(CitasDTO dto);

    CitasDTO actualizar(CitasDTO dto, Long id);

    CitasDTO cambiarEstado(Long id, Long nuevoIdEstado);

    void eliminar(Long id);

    List<CitasDTO> agendaMedico(Long idMedico, LocalDate fecha);
}
