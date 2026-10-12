package com.example.hospital.hospitalizaciones.impl;

import com.example.hospital.clinicas.Clinicas;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.clinicas.ClinicasRepository;
import com.example.hospital.habitacion.Habitacion;
import com.example.hospital.habitacion.HabitacionRepository;
import com.example.hospital.hospitalizaciones.Hospitalizaciones;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.hospitalizaciones.HospitalizacionesService;
import com.example.hospital.hospitalizaciones.dto.HospitalizacionesDTO;
import com.example.hospital.medico.Medico;
import com.example.hospital.medico.MedicoRepository;
import com.example.hospital.paciente.Paciente;
import com.example.hospital.paciente.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalizacionesServiceImpl implements HospitalizacionesService {

    // Constantes de Estados de Hospitalización (según Hosp.sql: 1: Activa, 2: Alta, 3: Traslado, 4: Fallecimiento)
    private static final Long ESTADO_HOSP_ACTIVA = 1L;
    private static final Long ESTADO_HOSP_ALTA = 2L;

    // Constantes de Estados de Habitación (según Hosp.sql: 1: Disponible, 2: Ocupada, 3: Mantenimiento)
    private static final Long ESTADO_HAB_DISPONIBLE = 1L;
    private static final Long ESTADO_HAB_OCUPADA = 2L;
    private static final Long ESTADO_HAB_MANTENIMIENTO = 3L;

    private final HospitalizacionesRepository hospitalizacionesRepository;
    private final PacienteRepository pacienteRepository;
    private final HabitacionRepository habitacionRepository;
    private final MedicoRepository medicoRepository;
    private final ClinicasRepository clinicasRepository;

    public HospitalizacionesServiceImpl(
            HospitalizacionesRepository hospitalizacionesRepository,
            PacienteRepository pacienteRepository,
            HabitacionRepository habitacionRepository,
            MedicoRepository medicoRepository,
            ClinicasRepository clinicasRepository) {
        this.hospitalizacionesRepository = hospitalizacionesRepository;
        this.pacienteRepository = pacienteRepository;
        this.habitacionRepository = habitacionRepository;
        this.medicoRepository = medicoRepository;
        this.clinicasRepository = clinicasRepository;
    }

    private HospitalizacionesDTO mapToDTO(Hospitalizaciones h) {
        return new HospitalizacionesDTO(
                h.getIdHospitalizacion(),
                h.getIdPaciente(),
                h.getIdHabitacion(),
                h.getIdMedicoResponsable(),
                h.getIdClinica(),
                h.getFechaIngreso(),
                h.getFechaAlta(),
                h.getMotivoIngreso(),
                h.getDiagnosticoIngreso(),
                h.getIdEstadoHosp(),
                h.getActivo()
        );
    }

    private Hospitalizaciones mapToEntity(HospitalizacionesDTO dto) {
        Hospitalizaciones h = new Hospitalizaciones();
        h.setIdPaciente(dto.idPaciente());
        h.setIdHabitacion(dto.idHabitacion());
        h.setIdMedicoResponsable(dto.idMedicoResponsable());
        h.setIdClinica(dto.idClinica());
        h.setFechaIngreso(dto.fechaIngreso());
        h.setFechaAlta(dto.fechaAlta());
        h.setMotivoIngreso(dto.motivoIngreso());
        h.setDiagnosticoIngreso(dto.diagnosticoIngreso());
        h.setIdEstadoHosp(dto.idEstadoHosp());
        h.setActivo(dto.activo());
        return h;
    }

    @Override
    public List<HospitalizacionesDTO> findAll() {
        return hospitalizacionesRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HospitalizacionesDTO findById(Long id) {
        Hospitalizaciones h = hospitalizacionesRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la hospitalización con ID: " + id));
        return mapToDTO(h);
    }

    @Override
    @Transactional
    public HospitalizacionesDTO registrar(HospitalizacionesDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la hospitalización son requeridos");
        }

        // RN-01: El paciente debe existir
        if (dto.idPaciente() == null) {
            throw new IllegalArgumentException("El ID del paciente es obligatorio");
        }
        Paciente paciente = pacienteRepository.findById(dto.idPaciente())
                .orElseThrow(() -> new IllegalArgumentException("El paciente con ID " + dto.idPaciente() + " no existe"));

        if (Boolean.FALSE.equals(paciente.getActivo())) {
            throw new IllegalArgumentException("El paciente seleccionado se encuentra inactivo");
        }

        // RN-02: Un paciente no puede tener dos hospitalizaciones activas simultáneamente
        if (hospitalizacionesRepository.existsByIdPacienteAndIdEstadoHosp(dto.idPaciente(), ESTADO_HOSP_ACTIVA)) {
            throw new IllegalArgumentException("El paciente ya cuenta con una hospitalización Activa");
        }

        // RN-03: El médico responsable debe existir y estar activo
        if (dto.idMedicoResponsable() == null) {
            throw new IllegalArgumentException("El médico responsable es obligatorio");
        }
        Medico medico = medicoRepository.findById(dto.idMedicoResponsable())
                .orElseThrow(() -> new IllegalArgumentException("El médico con ID " + dto.idMedicoResponsable() + " no existe"));
        if (Boolean.FALSE.equals(medico.getEstado())) {
            throw new IllegalArgumentException("El médico responsable se encuentra inactivo");
        }

        // RN-04: Toda hospitalización debe tener una habitación asignada
        if (dto.idHabitacion() == null) {
            throw new IllegalArgumentException("Toda hospitalización debe tener una habitación asignada");
        }
        Habitacion habitacion = habitacionRepository.findById(dto.idHabitacion())
                .orElseThrow(() -> new IllegalArgumentException("La habitación con ID " + dto.idHabitacion() + " no existe"));

        // RN-06 / RN-16: No se pueden utilizar habitaciones en mantenimiento
        if (ESTADO_HAB_MANTENIMIENTO.equals(habitacion.getIdEstadoHabitacion())) {
            throw new IllegalArgumentException("La habitación se encuentra en mantenimiento y no puede recibir pacientes");
        }

        // RN-05, RN-11, RN-12, RN-13: Verificar capacidad y ocupación actual
        long ocupacionActual = hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(dto.idHabitacion(), ESTADO_HOSP_ACTIVA);
        int capacidad = habitacion.getCapacidad() != null ? habitacion.getCapacidad() : 1;
        if (ocupacionActual >= capacidad) {
            throw new IllegalArgumentException("La habitación " + habitacion.getNumero() + " ya alcanzó su capacidad máxima (" + capacidad + ")");
        }

        // RN-07: La fecha de ingreso no puede ser posterior a la fecha actual
        LocalDateTime fechaIngreso = dto.fechaIngreso() != null ? dto.fechaIngreso() : LocalDateTime.now();
        if (fechaIngreso.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser posterior a la fecha actual");
        }

        // RN-08: El motivo de ingreso debe ser obligatorio
        if (dto.motivoIngreso() == null || dto.motivoIngreso().trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de ingreso es obligatorio");
        }

        // RN-09: La clínica asignada, cuando exista, debe estar activa
        if (dto.idClinica() != null) {
            Clinicas clinica = clinicasRepository.findById(dto.idClinica())
                    .orElseThrow(() -> new IllegalArgumentException("La clínica con ID " + dto.idClinica() + " no existe"));
            if (Boolean.FALSE.equals(clinica.getEstadoClinica())) {
                throw new IllegalArgumentException("La clínica asignada no se encuentra activa");
            }
        }

        // Construir entidad
        Hospitalizaciones h = mapToEntity(dto);
        h.setFechaIngreso(fechaIngreso);
        // RN-10: Toda hospitalización nueva debe iniciar con estado Activa
        h.setIdEstadoHosp(ESTADO_HOSP_ACTIVA);
        h.setActivo(true);

        Hospitalizaciones guardada = hospitalizacionesRepository.save(h);

        // RN-14: Si con este ingreso la habitación alcanza su capacidad máxima, actualizar a Ocupada
        if ((ocupacionActual + 1) >= capacidad) {
            habitacion.setIdEstadoHabitacion(ESTADO_HAB_OCUPADA);
            habitacionRepository.save(habitacion);
        }

        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public HospitalizacionesDTO actualizar(HospitalizacionesDTO dto, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de hospitalización es obligatorio");
        }

        Hospitalizaciones existente = hospitalizacionesRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la hospitalización con ID: " + id));

        // RN-17 / RN-22: Solo se podrán modificar hospitalizaciones que estén activas
        if (!ESTADO_HOSP_ACTIVA.equals(existente.getIdEstadoHosp())) {
            throw new IllegalArgumentException("No se permite modificar una hospitalización finalizada o no activa");
        }

        // RN-21: No se podrá modificar el paciente asociado a una hospitalización ya registrada
        if (dto.idPaciente() != null && !dto.idPaciente().equals(existente.getIdPaciente())) {
            throw new IllegalArgumentException("No se permite modificar el paciente asociado a una hospitalización registrada");
        }

        // RN-18: El médico responsable podrá cambiarse únicamente por otro médico activo
        if (dto.idMedicoResponsable() != null && !dto.idMedicoResponsable().equals(existente.getIdMedicoResponsable())) {
            Medico nuevoMedico = medicoRepository.findById(dto.idMedicoResponsable())
                    .orElseThrow(() -> new IllegalArgumentException("El médico con ID " + dto.idMedicoResponsable() + " no existe"));
            if (Boolean.FALSE.equals(nuevoMedico.getEstado())) {
                throw new IllegalArgumentException("El nuevo médico responsable no está activo");
            }
            existente.setIdMedicoResponsable(dto.idMedicoResponsable());
        }

        // RN-19 / RN-20: Traslado a otra habitación
        if (dto.idHabitacion() != null && !dto.idHabitacion().equals(existente.getIdHabitacion())) {
            Habitacion nuevaHabitacion = habitacionRepository.findById(dto.idHabitacion())
                    .orElseThrow(() -> new IllegalArgumentException("La nueva habitación no existe"));

            if (ESTADO_HAB_MANTENIMIENTO.equals(nuevaHabitacion.getIdEstadoHabitacion())) {
                throw new IllegalArgumentException("La nueva habitación está en mantenimiento");
            }

            long ocupacionNueva = hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(dto.idHabitacion(), ESTADO_HOSP_ACTIVA);
            int capacidadNueva = nuevaHabitacion.getCapacidad() != null ? nuevaHabitacion.getCapacidad() : 1;
            if (ocupacionNueva >= capacidadNueva) {
                throw new IllegalArgumentException("La nueva habitación ya alcanzó su capacidad máxima");
            }

            // Liberar o ajustar la habitación anterior
            Habitacion habitacionAnterior = habitacionRepository.findById(existente.getIdHabitacion()).orElse(null);
            if (habitacionAnterior != null) {
                long ocupacionAnterior = hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(habitacionAnterior.getIdHabitacion(), ESTADO_HOSP_ACTIVA);
                // Si la ocupación anterior queda en 1 (que era este paciente que se va), pasará a Disponible
                if (ocupacionAnterior <= 1 && !ESTADO_HAB_MANTENIMIENTO.equals(habitacionAnterior.getIdEstadoHabitacion())) {
                    habitacionAnterior.setIdEstadoHabitacion(ESTADO_HAB_DISPONIBLE);
                    habitacionRepository.save(habitacionAnterior);
                }
            }

            // Actualizar la nueva habitación si se llena
            if ((ocupacionNueva + 1) >= capacidadNueva) {
                nuevaHabitacion.setIdEstadoHabitacion(ESTADO_HAB_OCUPADA);
                habitacionRepository.save(nuevaHabitacion);
            }

            existente.setIdHabitacion(dto.idHabitacion());
        }

        if (dto.motivoIngreso() != null && !dto.motivoIngreso().trim().isEmpty()) {
            existente.setMotivoIngreso(dto.motivoIngreso());
        }

        if (dto.diagnosticoIngreso() != null) {
            existente.setDiagnosticoIngreso(dto.diagnosticoIngreso());
        }

        if (dto.idClinica() != null) {
            Clinicas clinica = clinicasRepository.findById(dto.idClinica())
                    .orElseThrow(() -> new IllegalArgumentException("La clínica no existe"));
            if (Boolean.FALSE.equals(clinica.getEstadoClinica())) {
                throw new IllegalArgumentException("La clínica no está activa");
            }
            existente.setIdClinica(dto.idClinica());
        }

        Hospitalizaciones actualizada = hospitalizacionesRepository.save(existente);
        return mapToDTO(actualizada);
    }

    @Override
    @Transactional
    public HospitalizacionesDTO darAlta(Long id, LocalDateTime fechaAlta) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de hospitalización es obligatorio");
        }

        Hospitalizaciones existente = hospitalizacionesRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la hospitalización con ID: " + id));

        // RN-23: Solo se podrá registrar el alta de una hospitalización activa
        // RN-25: No se podrá registrar dos veces el alta de la misma hospitalización
        if (!ESTADO_HOSP_ACTIVA.equals(existente.getIdEstadoHosp())) {
            throw new IllegalArgumentException("Solo se puede dar de alta una hospitalización que se encuentre Activa");
        }

        LocalDateTime fechaAltaEfectiva = fechaAlta != null ? fechaAlta : LocalDateTime.now();

        // RN-24: La fecha de alta no puede ser anterior a la fecha de ingreso
        if (existente.getFechaIngreso() != null && fechaAltaEfectiva.isBefore(existente.getFechaIngreso())) {
            throw new IllegalArgumentException("La fecha de alta no puede ser anterior a la fecha de ingreso");
        }

        // RN-27: Se cambia el estado a Alta (no podrá volver a Activa)
        existente.setIdEstadoHosp(ESTADO_HOSP_ALTA);
        existente.setFechaAlta(fechaAltaEfectiva);

        Hospitalizaciones guardada = hospitalizacionesRepository.save(existente);

        // RN-15 / RN-26: Al registrar el alta, se deberá liberar la ocupación correspondiente de la habitación
        if (existente.getIdHabitacion() != null) {
            Habitacion habitacion = habitacionRepository.findById(existente.getIdHabitacion()).orElse(null);
            if (habitacion != null && !ESTADO_HAB_MANTENIMIENTO.equals(habitacion.getIdEstadoHabitacion())) {
                long ocupacionRestante = hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(habitacion.getIdHabitacion(), ESTADO_HOSP_ACTIVA);
                if (ocupacionRestante == 0) {
                    habitacion.setIdEstadoHabitacion(ESTADO_HAB_DISPONIBLE);
                    habitacionRepository.save(habitacion);
                }
            }
        }

        // RN-29: El historial de hospitalización se conserva en base de datos
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Hospitalizaciones existente = hospitalizacionesRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la hospitalización con ID: " + id));
        // Borrado lógico para conservar historial (RN-29)
        existente.setActivo(false);
        hospitalizacionesRepository.save(existente);
    }
}
