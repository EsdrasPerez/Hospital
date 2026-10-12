package com.example.hospital.habitacion.impl;

import com.example.hospital.emergencia.EmergenciaRepository;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.estadohabitacion.EstadoHabitacion;
import com.example.hospital.estadohabitacion.EstadoHabitacionRepository;
import com.example.hospital.habitacion.Habitacion;
import com.example.hospital.habitacion.HabitacionRepository;
import com.example.hospital.habitacion.HabitacionService;
import com.example.hospital.habitacion.dto.HabitacionDTO;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.tiposhabitacion.TiposHabitacion;
import com.example.hospital.tiposhabitacion.TiposHabitacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HabitacionServiceImpl implements HabitacionService {

    public static final Long ESTADO_HAB_DISPONIBLE = 1L;
    public static final Long ESTADO_HOSP_ACTIVA = 1L;

    private final HabitacionRepository habitacionRepository;
    private final EstadoHabitacionRepository estadoHabitacionRepository;
    private final TiposHabitacionRepository tiposHabitacionRepository;
    private final HospitalizacionesRepository hospitalizacionesRepository;
    private final EmergenciaRepository emergenciaRepository;

    public HabitacionServiceImpl(HabitacionRepository habitacionRepository,
                                 EstadoHabitacionRepository estadoHabitacionRepository,
                                 TiposHabitacionRepository tiposHabitacionRepository,
                                 HospitalizacionesRepository hospitalizacionesRepository,
                                 EmergenciaRepository emergenciaRepository) {
        this.habitacionRepository = habitacionRepository;
        this.estadoHabitacionRepository = estadoHabitacionRepository;
        this.tiposHabitacionRepository = tiposHabitacionRepository;
        this.hospitalizacionesRepository = hospitalizacionesRepository;
        this.emergenciaRepository = emergenciaRepository;
    }

    private HabitacionDTO mapToDTO(Habitacion h) {
        return new HabitacionDTO(
                h.getIdHabitacion(),
                h.getNumero(),
                h.getPiso(),
                h.getCapacidad(),
                h.getIdTipoHabitacion(),
                h.getIdEstadoHabitacion(),
                h.getActivo()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionDTO> findAll() {
        return habitacionRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionDTO> findActivas() {
        return habitacionRepository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HabitacionDTO findById(Long id) {
        Habitacion entity = habitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la habitación con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public HabitacionDTO findByNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new ReglaNegocioException("El número de habitación es obligatorio");
        }
        Habitacion entity = habitacionRepository.findByNumero(numero.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la habitación con número: " + numero));
        return mapToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionDTO> findDisponibles() {
        return habitacionRepository.findByIdEstadoHabitacionAndActivoTrue(ESTADO_HAB_DISPONIBLE)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HabitacionDTO crear(HabitacionDTO dto) {
        validarReglasDeNegocio(dto, null);

        Habitacion entity = new Habitacion();
        entity.setNumero(dto.numero().trim());
        entity.setPiso(dto.piso());
        entity.setCapacidad(dto.capacidad());
        entity.setIdTipoHabitacion(dto.idTipoHabitacion());
        entity.setIdEstadoHabitacion(dto.idEstadoHabitacion());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);

        Habitacion guardada = habitacionRepository.save(entity);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public HabitacionDTO actualizar(HabitacionDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de habitación es obligatorio para la actualización");
        }

        Habitacion existente = habitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la habitación con ID: " + id));

        validarReglasDeNegocio(dto, id);

        existente.setNumero(dto.numero().trim());
        existente.setPiso(dto.piso());
        existente.setCapacidad(dto.capacidad());
        existente.setIdTipoHabitacion(dto.idTipoHabitacion());
        existente.setIdEstadoHabitacion(dto.idEstadoHabitacion());
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        Habitacion actualizada = habitacionRepository.save(existente);
        return mapToDTO(actualizada);
    }

    @Override
    @Transactional
    public HabitacionDTO cambiarEstado(Long id, Long nuevoEstadoId) {
        if (id == null || nuevoEstadoId == null) {
            throw new ReglaNegocioException("El ID de la habitación y el nuevo estado son requeridos");
        }

        Habitacion existente = habitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la habitación con ID: " + id));

        EstadoHabitacion estado = estadoHabitacionRepository.findById(nuevoEstadoId)
                .orElseThrow(() -> new ReglaNegocioException("El estado de habitación con ID " + nuevoEstadoId + " no existe"));

        if (!Boolean.TRUE.equals(estado.getActivo())) {
            throw new ReglaNegocioException("El estado de habitación seleccionado se encuentra inactivo");
        }

        existente.setIdEstadoHabitacion(nuevoEstadoId);
        Habitacion guardada = habitacionRepository.save(existente);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID no puede ser nulo");
        }

        Habitacion existente = habitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la habitación con ID: " + id));

        // RN-HAB-06: Integridad clínica antes de desactivar
        long hospitalizados = hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(id, ESTADO_HOSP_ACTIVA);
        if (hospitalizados > 0) {
            throw new ReglaNegocioException("No se puede desactivar la habitación porque tiene pacientes actualmente hospitalizados en ella.");
        }

        if (!emergenciaRepository.findByIdHabitacionAndActivoTrue(id).isEmpty()) {
            throw new ReglaNegocioException("No se puede desactivar la habitación porque tiene ingresos de emergencia activos asignados.");
        }

        // RN-HAB-07: Borrado lógico sistemático
        existente.setActivo(false);
        habitacionRepository.save(existente);
    }

    private void validarReglasDeNegocio(HabitacionDTO dto, Long idActualizacion) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de la habitación son requeridos");
        }

        // RN-HAB-01: Número obligatorio y único
        if (dto.numero() == null || dto.numero().trim().isEmpty()) {
            throw new ReglaNegocioException("El número de habitación es obligatorio");
        }
        String num = dto.numero().trim();
        if (idActualizacion == null) {
            if (habitacionRepository.existsByNumero(num)) {
                throw new ReglaNegocioException("Ya existe una habitación registrada con el número: " + num);
            }
        } else {
            if (habitacionRepository.existsByNumeroAndIdHabitacionNot(num, idActualizacion)) {
                throw new ReglaNegocioException("Ya existe otra habitación registrada con el número: " + num);
            }
        }

        // RN-HAB-02: Piso obligatorio
        if (dto.piso() == null) {
            throw new ReglaNegocioException("El piso donde se ubica la habitación es obligatorio");
        }

        // RN-HAB-03: Capacidad mayor que cero
        if (dto.capacidad() == null || dto.capacidad() <= 0) {
            throw new ReglaNegocioException("La capacidad de la habitación debe ser mayor que cero");
        }

        // RN-HAB-04: Tipo de habitación existente y activo
        if (dto.idTipoHabitacion() == null) {
            throw new ReglaNegocioException("El tipo de habitación es obligatorio");
        }
        TiposHabitacion tipo = tiposHabitacionRepository.findById(dto.idTipoHabitacion())
                .orElseThrow(() -> new ReglaNegocioException("El tipo de habitación con ID " + dto.idTipoHabitacion() + " no existe"));
        if (!Boolean.TRUE.equals(tipo.getActivo())) {
            throw new ReglaNegocioException("El tipo de habitación seleccionado se encuentra inactivo");
        }

        // RN-HAB-05: Estado de habitación existente y activo
        if (dto.idEstadoHabitacion() == null) {
            throw new ReglaNegocioException("El estado de la habitación es obligatorio");
        }
        EstadoHabitacion estado = estadoHabitacionRepository.findById(dto.idEstadoHabitacion())
                .orElseThrow(() -> new ReglaNegocioException("El estado de habitación con ID " + dto.idEstadoHabitacion() + " no existe"));
        if (!Boolean.TRUE.equals(estado.getActivo())) {
            throw new ReglaNegocioException("El estado de habitación seleccionado se encuentra inactivo");
        }
    }
}
