package com.example.hospital.citas.impl;

import com.example.hospital.citas.Citas;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.citas.CitasRepository;
import com.example.hospital.citas.CitasService;
import com.example.hospital.citas.dto.CitasDTO;
import com.example.hospital.consultas.ConsultasRepository;
import com.example.hospital.estadocita.EstadoCita;
import com.example.hospital.estadocita.EstadoCitaRepository;
import com.example.hospital.estadocita.EstadoCitaService;
import com.example.hospital.medico.Medico;
import com.example.hospital.medico.MedicoRepository;
import com.example.hospital.paciente.Paciente;
import com.example.hospital.paciente.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CitasServiceImpl implements CitasService {

    // RN-06: una cita solo puede nacer en uno de estos estados
    private static final Set<String> ESTADOS_INICIALES = Set.of("Programada", "Confirmada");

    private final CitasRepository citasRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final EstadoCitaRepository estadoCitaRepository;
    private final EstadoCitaService estadoCitaService;
    private final ConsultasRepository consultasRepository;

    public CitasServiceImpl(CitasRepository citasRepository,
                            PacienteRepository pacienteRepository,
                            MedicoRepository medicoRepository,
                            EstadoCitaRepository estadoCitaRepository,
                            EstadoCitaService estadoCitaService,
                            ConsultasRepository consultasRepository) {
        this.citasRepository = citasRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
        this.estadoCitaRepository = estadoCitaRepository;
        this.estadoCitaService = estadoCitaService;
        this.consultasRepository = consultasRepository;
    }

    private CitasDTO mapToDTO(Citas c) {
        return new CitasDTO(
                c.getIdCita(),
                c.getFecha(),
                c.getHora(),
                c.getMotivo(),
                c.getFechaCreacion(),
                c.getIdEstado(),
                c.getIdMedico(),
                c.getIdPaciente(),
                c.getActivo()
        );
    }

    private Citas mapToEntity(CitasDTO dto) {
        Citas c = new Citas();
        c.setFecha(dto.fecha());
        c.setHora(dto.hora());
        c.setMotivo(dto.motivo() != null ? dto.motivo().trim() : null);
        c.setFechaCreacion(dto.fechaCreacion());
        c.setIdEstado(dto.idEstado());
        c.setIdMedico(dto.idMedico());
        c.setIdPaciente(dto.idPaciente());
        c.setActivo(dto.activo());
        return c;
    }

    /**
     * Valida las reglas de negocio para la gestión de citas (los campos obligatorios los valida
     * Bean Validation en CitasDTO, que responde 400 con detalles por campo):
     * 1. Paciente debe existir y estar activo.
     * 2. Médico debe existir y estar activo.
     * 3. Estado de cita debe existir y estar activo.
     * 4. La fecha de la cita no puede ser anterior al día actual. En una actualización solo se
     *    exige si cambian la fecha o la hora (así se pueden corregir citas ya pasadas).
     * 5. No crear/actualizar una cita si el médico ya tiene otra cita activa en la misma fecha y hora.
     * 6. Al crear, el estado inicial solo puede ser Programada o Confirmada.
     *
     * @param existente cita actual (null al crear)
     */
    private void validarReglasDeNegocio(CitasDTO dto, Citas existente) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de la cita médica no pueden ser nulos");
        }
        Long idCitaActual = existente != null ? existente.getIdCita() : null;

        // RN-01: La fecha de la cita no puede ser en el pasado (si es nueva o si se reprograma)
        boolean reprogramada = existente == null
                || !dto.fecha().equals(existente.getFecha())
                || !dto.hora().equals(existente.getHora());
        if (reprogramada && dto.fecha().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de la cita no puede ser anterior a la fecha actual (" + LocalDate.now() + ")");
        }

        // RN-02: El paciente debe existir y estar activo
        Paciente paciente = pacienteRepository.findById(dto.idPaciente())
                .orElseThrow(() -> new ReglaNegocioException("El paciente con ID " + dto.idPaciente() + " no existe"));

        if (paciente.getActivo() != null && !paciente.getActivo()) {
            throw new ReglaNegocioException("El paciente seleccionado se encuentra inactivo");
        }

        // RN-03: El médico debe existir y estar activo
        Medico medico = medicoRepository.findById(dto.idMedico())
                .orElseThrow(() -> new ReglaNegocioException("El médico con ID " + dto.idMedico() + " no existe"));

        if (medico.getEstado() != null && !medico.getEstado()) {
            throw new ReglaNegocioException("El médico seleccionado se encuentra inactivo");
        }

        // RN-04: El estado de la cita debe existir y estar activo
        EstadoCita estado = estadoCitaRepository.findById(dto.idEstado())
                .orElseThrow(() -> new ReglaNegocioException("El estado de cita con ID " + dto.idEstado() + " no existe"));

        if (estado.getActivo() != null && !estado.getActivo()) {
            throw new ReglaNegocioException("El estado de cita '" + estado.getDescripcion() + "' no se encuentra activo");
        }

        // RN-06: Estado inicial solo Programada o Confirmada
        if (existente == null && ESTADOS_INICIALES.stream().noneMatch(e -> e.equalsIgnoreCase(estado.getDescripcion()))) {
            throw new ReglaNegocioException("Una cita nueva solo puede crearse en estado Programada o Confirmada (se indicó '"
                    + estado.getDescripcion() + "')");
        }

        // RN-05: El médico no puede tener otra cita activa en la misma fecha y hora
        boolean medicoOcupado;
        if (idCitaActual == null) {
            medicoOcupado = citasRepository.existsByIdMedicoAndFechaAndHoraAndActivoTrue(
                    dto.idMedico(), dto.fecha(), dto.hora()
            );
        } else {
            medicoOcupado = citasRepository.existsByIdMedicoAndFechaAndHoraAndActivoTrueAndIdCitaNot(
                    dto.idMedico(), dto.fecha(), dto.hora(), idCitaActual
            );
        }

        if (medicoOcupado) {
            throw new DuplicadoException("El médico ya tiene asignada una cita activa en la fecha " +
                    dto.fecha() + " a las " + dto.hora());
        }
    }

    @Override
    public List<CitasDTO> findAll() {
        return citasRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitasDTO> findActivas() {
        return citasRepository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitasDTO> buscar(Long idPaciente, Long idMedico, LocalDate fecha, boolean soloActivas) {
        return citasRepository.findAll()
                .stream()
                .filter(c -> idPaciente == null || idPaciente.equals(c.getIdPaciente()))
                .filter(c -> idMedico == null || idMedico.equals(c.getIdMedico()))
                .filter(c -> fecha == null || fecha.equals(c.getFecha()))
                .filter(c -> !soloActivas || Boolean.TRUE.equals(c.getActivo()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitasDTO> findByPaciente(Long idPaciente) {
        if (idPaciente == null) {
            throw new ReglaNegocioException("El ID del paciente es obligatorio");
        }
        return citasRepository.findByIdPaciente(idPaciente)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitasDTO> findByMedico(Long idMedico) {
        if (idMedico == null) {
            throw new ReglaNegocioException("El ID del médico es obligatorio");
        }
        return citasRepository.findByIdMedico(idMedico)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitasDTO> findByFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new ReglaNegocioException("La fecha de consulta es obligatoria");
        }
        return citasRepository.findByFecha(fecha)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CitasDTO findById(Long id) {
        Citas entity = citasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cita médica con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public CitasDTO crear(CitasDTO dto) {
        validarReglasDeNegocio(dto, null);

        Citas entity = mapToEntity(dto);
        // La fecha de creación es asignada por el servidor
        entity.setFechaCreacion(LocalDateTime.now());
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }

        Citas guardada = citasRepository.save(entity);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public CitasDTO actualizar(CitasDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de la cita médica es obligatorio");
        }

        Citas existente = citasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cita médica con ID: " + id));

        validarReglasDeNegocio(dto, existente);

        // Si cambia el estado, validar la máquina de estados con EstadoCitaService
        if (!existente.getIdEstado().equals(dto.idEstado())) {
            EstadoCita estadoOrigen = estadoCitaRepository.findById(existente.getIdEstado()).orElse(null);
            EstadoCita estadoDestino = estadoCitaRepository.findById(dto.idEstado()).orElse(null);
            if (estadoOrigen != null && estadoDestino != null) {
                estadoCitaService.validarTransicion(estadoOrigen.getDescripcion(), estadoDestino.getDescripcion());
            }
            existente.setIdEstado(dto.idEstado());
        }

        existente.setFecha(dto.fecha());
        existente.setHora(dto.hora());
        existente.setMotivo(dto.motivo() != null ? dto.motivo().trim() : null);
        existente.setIdMedico(dto.idMedico());
        existente.setIdPaciente(dto.idPaciente());
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        Citas actualizada = citasRepository.save(existente);
        return mapToDTO(actualizada);
    }

    @Override
    @Transactional
    public CitasDTO cambiarEstado(Long id, Long nuevoIdEstado) {
        if (id == null || nuevoIdEstado == null) {
            throw new ReglaNegocioException("El ID de la cita y el nuevo ID de estado son obligatorios");
        }

        Citas existente = citasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cita médica con ID: " + id));

        EstadoCita estadoOrigen = estadoCitaRepository.findById(existente.getIdEstado())
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado actual de la cita no encontrado"));
        EstadoCita estadoDestino = estadoCitaRepository.findById(nuevoIdEstado)
                .orElseThrow(() -> new RecursoNoEncontradoException("El nuevo estado con ID " + nuevoIdEstado + " no existe"));

        // Validar flujo permitido de estados según RN-EC-02
        estadoCitaService.validarTransicion(estadoOrigen.getDescripcion(), estadoDestino.getDescripcion());

        existente.setIdEstado(nuevoIdEstado);
        Citas guardada = citasRepository.save(existente);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de la cita es obligatorio");
        }

        Citas existente = citasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cita médica con ID: " + id));

        // Integridad referencial: no eliminar cita si ya tiene una consulta registrada
        if (consultasRepository.existsByIdCita(id)) {
            throw new IllegalStateException("No se puede eliminar la cita médica porque ya cuenta con una consulta médica registrada");
        }

        // Borrado lógico
        existente.setActivo(false);
        citasRepository.save(existente);
    }

    @Override
    public List<CitasDTO> agendaMedico(Long idMedico, LocalDate fecha) {
        if (idMedico == null) {
            throw new ReglaNegocioException("El ID del médico es obligatorio");
        }
        if (!medicoRepository.existsById(idMedico)) {
            throw new RecursoNoEncontradoException("No existe el médico con ID: " + idMedico);
        }
        LocalDate dia = (fecha != null) ? fecha : LocalDate.now();
        return citasRepository.findByIdMedicoAndFechaAndActivoTrueOrderByHoraAsc(idMedico, dia)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
