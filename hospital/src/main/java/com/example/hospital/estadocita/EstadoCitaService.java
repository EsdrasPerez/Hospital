package com.example.hospital.estadocita;

import com.example.hospital.estadocita.dto.EstadoCitaDTO;

import java.util.List;

public interface EstadoCitaService {
    List<EstadoCitaDTO> findAll();

    EstadoCitaDTO findById(Long id);

    EstadoCitaDTO crear(EstadoCitaDTO dto);

    EstadoCitaDTO actualizar(EstadoCitaDTO dto, Long id);

    void eliminar(Long id);

    /**
     * RN-EC-02: Valida si la transición de un estado origen a un estado destino es permitida.
     * @param estadoOrigen Descripcion del estado actual de la cita (ej. "Programada")
     * @param estadoDestino Descripcion del nuevo estado (ej. "Confirmada")
     * @return true si la transición es válida, false en caso contrario.
     */
    boolean esTransicionPermitida(String estadoOrigen, String estadoDestino);

    /**
     * RN-EC-02: Valida la transición y lanza excepción si es inválida.
     */
    void validarTransicion(String estadoOrigen, String estadoDestino);
}
