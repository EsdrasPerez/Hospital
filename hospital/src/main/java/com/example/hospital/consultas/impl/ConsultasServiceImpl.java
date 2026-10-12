package com.example.hospital.consultas.impl;

import com.example.hospital.citas.Citas;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.citas.CitasRepository;
import com.example.hospital.consultas.Consultas;
import com.example.hospital.consultas.ConsultasRepository;
import com.example.hospital.consultas.ConsultasService;
import com.example.hospital.consultas.dto.ConsultasDTO;
import com.example.hospital.diagnosticoconsulta.DiagnosticoConsultaRepository;
import com.example.hospital.estadocita.EstadoCita;
import com.example.hospital.estadocita.EstadoCitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultasServiceImpl implements ConsultasService {

    // Rangos clínicos permitidos
    private static final BigDecimal PESO_MINIMO = BigDecimal.ZERO;
    private static final BigDecimal TALLA_MINIMA = BigDecimal.ZERO;
    private static final BigDecimal TEMPERATURA_MINIMA = new BigDecimal("30.0");
    private static final BigDecimal TEMPERATURA_MAXIMA = new BigDecimal("45.0");
    private static final int FRECUENCIA_CARDIACA_MINIMA = 20;
    private static final int FRECUENCIA_CARDIACA_MAXIMA = 250;

    private final ConsultasRepository consultasRepository;
    private final CitasRepository citasRepository;
    private final EstadoCitaRepository estadoCitaRepository;
    private final DiagnosticoConsultaRepository diagnosticoConsultaRepository;

    public ConsultasServiceImpl(ConsultasRepository consultasRepository,
                                CitasRepository citasRepository,
                                EstadoCitaRepository estadoCitaRepository,
                                DiagnosticoConsultaRepository diagnosticoConsultaRepository) {
        this.consultasRepository = consultasRepository;
        this.citasRepository = citasRepository;
        this.estadoCitaRepository = estadoCitaRepository;
        this.diagnosticoConsultaRepository = diagnosticoConsultaRepository;
    }

    private ConsultasDTO mapToDTO(Consultas c) {
        return new ConsultasDTO(
                c.getIdConsulta(),
                c.getFechaHora(),
                c.getSintomas(),
                c.getPeso(),
                c.getTalla(),
                c.getTemperatura(),
                c.getPresionArterial(),
                c.getFrecuenciaCardiaca(),
                c.getIdCita(),
                c.getActivo()
        );
    }

    private Consultas mapToEntity(ConsultasDTO dto) {
        Consultas c = new Consultas();
        c.setFechaHora(dto.fechaHora());
        c.setSintomas(dto.sintomas() != null ? dto.sintomas().trim() : null);
        c.setPeso(dto.peso());
        c.setTalla(dto.talla());
        c.setTemperatura(dto.temperatura());
        c.setPresionArterial(dto.presionArterial() != null ? dto.presionArterial().trim() : null);
        c.setFrecuenciaCardiaca(dto.frecuenciaCardiaca());
        c.setIdCita(dto.idCita());
        c.setActivo(dto.activo());
        return c;
    }

    /**
     * Valida las reglas de negocio de la consulta médica:
     * 1. La cita médica asociada debe existir y estar activa.
     * 2. No duplicar consultas activas para la misma cita médica.
     * 3. Rangos clínicos:
     *    - Peso > 0 (si se informa)
     *    - Talla > 0 (si se informa)
     *    - Temperatura entre 30.0°C y 45.0°C (si se informa)
     *    - Frecuencia cardíaca entre 20 y 250 lpm (si se informa)
     */
    private void validarReglasDeNegocio(ConsultasDTO dto, Long idConsultaActual) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la consulta no pueden ser nulos");
        }

        if (dto.idCita() == null) {
            throw new IllegalArgumentException("El ID de la cita médica asociada es obligatorio");
        }

        // RN-01: La cita debe existir y estar activa
        Citas cita = citasRepository.findById(dto.idCita())
                .orElseThrow(() -> new IllegalArgumentException("La cita médica con ID " + dto.idCita() + " no existe"));

        if (cita.getActivo() != null && !cita.getActivo()) {
            throw new IllegalArgumentException("La cita médica asociada se encuentra inactiva o cancelada");
        }

        // RN-02: No duplicar consultas para la misma cita
        boolean consultaDuplicada;
        if (idConsultaActual == null) {
            consultaDuplicada = consultasRepository.existsByIdCita(dto.idCita());
        } else {
            consultaDuplicada = consultasRepository.existsByIdCitaAndIdConsultaNot(dto.idCita(), idConsultaActual);
        }

        if (consultaDuplicada) {
            throw new IllegalArgumentException("Ya existe una consulta médica registrada para la cita ID: " + dto.idCita());
        }

        // RN-03: Rangos clínicos
        if (dto.peso() != null && dto.peso().compareTo(PESO_MINIMO) <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a cero");
        }

        if (dto.talla() != null && dto.talla().compareTo(TALLA_MINIMA) <= 0) {
            throw new IllegalArgumentException("La talla/estatura debe ser mayor a cero");
        }

        if (dto.temperatura() != null) {
            if (dto.temperatura().compareTo(TEMPERATURA_MINIMA) < 0 || dto.temperatura().compareTo(TEMPERATURA_MAXIMA) > 0) {
                throw new IllegalArgumentException("La temperatura corporal debe encontrarse entre " +
                        TEMPERATURA_MINIMA + "°C y " + TEMPERATURA_MAXIMA + "°C");
            }
        }

        if (dto.frecuenciaCardiaca() != null) {
            if (dto.frecuenciaCardiaca() < FRECUENCIA_CARDIACA_MINIMA || dto.frecuenciaCardiaca() > FRECUENCIA_CARDIACA_MAXIMA) {
                throw new IllegalArgumentException("La frecuencia cardíaca debe encontrarse entre " +
                        FRECUENCIA_CARDIACA_MINIMA + " y " + FRECUENCIA_CARDIACA_MAXIMA + " lpm");
            }
        }
    }

    @Override
    public List<ConsultasDTO> findAll() {
        return consultasRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsultasDTO> findActivas() {
        return consultasRepository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsultasDTO findById(Long id) {
        Consultas entity = consultasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la consulta médica con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    public ConsultasDTO findByCita(Long idCita) {
        if (idCita == null) {
            throw new IllegalArgumentException("El ID de cita es obligatorio");
        }
        Consultas entity = consultasRepository.findByIdCita(idCita)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe consulta registrada para la cita ID: " + idCita));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public ConsultasDTO crear(ConsultasDTO dto) {
        validarReglasDeNegocio(dto, null);

        Consultas entity = mapToEntity(dto);
        // Fecha/hora de atención asignada por el servidor si no viene especificada
        if (entity.getFechaHora() == null) {
            entity.setFechaHora(LocalDateTime.now());
        }
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }

        Consultas guardada = consultasRepository.save(entity);

        // Actualizar el estado de la cita a "Atendida" automáticamente
        citasRepository.findById(dto.idCita()).ifPresent(cita -> {
            estadoCitaRepository.findByDescripcionIgnoreCase("Atendida").ifPresent(estadoAtendida -> {
                cita.setIdEstado(estadoAtendida.getIdEstado());
                citasRepository.save(cita);
            });
        });

        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public ConsultasDTO actualizar(ConsultasDTO dto, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la consulta es obligatorio");
        }

        Consultas existente = consultasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la consulta médica con ID: " + id));

        validarReglasDeNegocio(dto, id);

        if (dto.fechaHora() != null) {
            existente.setFechaHora(dto.fechaHora());
        }
        existente.setSintomas(dto.sintomas() != null ? dto.sintomas().trim() : null);
        existente.setPeso(dto.peso());
        existente.setTalla(dto.talla());
        existente.setTemperatura(dto.temperatura());
        existente.setPresionArterial(dto.presionArterial() != null ? dto.presionArterial().trim() : null);
        existente.setFrecuenciaCardiaca(dto.frecuenciaCardiaca());
        existente.setIdCita(dto.idCita());
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        Consultas actualizada = consultasRepository.save(existente);
        return mapToDTO(actualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la consulta es obligatorio");
        }

        Consultas existente = consultasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la consulta médica con ID: " + id));

        // Integridad referencial: no eliminar consulta si tiene diagnósticos asociados
        if (diagnosticoConsultaRepository.existsByIdConsulta(id)) {
            throw new IllegalStateException("No se puede eliminar la consulta médica porque tiene diagnósticos clínicos asociados");
        }

        // Borrado lógico
        existente.setActivo(false);
        consultasRepository.save(existente);
    }
}
