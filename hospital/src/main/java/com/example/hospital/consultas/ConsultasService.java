package com.example.hospital.consultas;

import com.example.hospital.consultas.dto.ConsultasDTO;

import java.util.List;

public interface ConsultasService {

    List<ConsultasDTO> findAll();

    List<ConsultasDTO> findActivas();

    ConsultasDTO findById(Long id);

    ConsultasDTO findByCita(Long idCita);

    /** Consulta de la cita como lista: vacía si la cita todavía no tiene consulta. */
    List<ConsultasDTO> listarPorCita(Long idCita);

    ConsultasDTO crear(ConsultasDTO dto);

    ConsultasDTO actualizar(ConsultasDTO dto, Long id);

    void eliminar(Long id);
}
