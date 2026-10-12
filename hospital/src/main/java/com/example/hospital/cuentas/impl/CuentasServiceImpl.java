package com.example.hospital.cuentas.impl;

import com.example.hospital.cuentas.Cuentas;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.cuentas.CuentasRepository;
import com.example.hospital.cuentas.CuentasService;
import com.example.hospital.cuentas.dto.CuentasDTO;
import com.example.hospital.hospitalizaciones.Hospitalizaciones;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.paciente.Paciente;
import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.pagos.PagosRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuentasServiceImpl implements CuentasService {

    public static final String ESTADO_ABIERTA = "Abierta";
    public static final String ESTADO_CERRADA = "Cerrada";
    public static final String ESTADO_LIQUIDADA = "Liquidada";

    private final CuentasRepository cuentasRepository;
    private final PacienteRepository pacienteRepository;
    private final HospitalizacionesRepository hospitalizacionesRepository;
    private final PagosRepository pagosRepository;

    public CuentasServiceImpl(CuentasRepository cuentasRepository,
                              PacienteRepository pacienteRepository,
                              HospitalizacionesRepository hospitalizacionesRepository,
                              PagosRepository pagosRepository) {
        this.cuentasRepository = cuentasRepository;
        this.pacienteRepository = pacienteRepository;
        this.hospitalizacionesRepository = hospitalizacionesRepository;
        this.pagosRepository = pagosRepository;
    }

    private CuentasDTO mapToDTO(Cuentas c) {
        return new CuentasDTO(
                c.getIdCuenta(),
                c.getIdPaciente(),
                c.getIdHospitalizacion(),
                c.getFechaApertura(),
                c.getEstado(),
                c.getSaldo(),
                c.getActivo()
        );
    }

    private Cuentas mapToEntity(CuentasDTO dto) {
        Cuentas c = new Cuentas();
        c.setIdPaciente(dto.idPaciente());
        c.setIdHospitalizacion(dto.idHospitalizacion());
        c.setFechaApertura(dto.fechaApertura());
        c.setEstado(dto.estado() != null ? dto.estado().trim() : ESTADO_ABIERTA);
        c.setSaldo(dto.saldo() != null ? dto.saldo() : BigDecimal.ZERO);
        c.setActivo(dto.activo());
        return c;
    }

    /**
     * Valida las reglas de negocio para la gestión de cuentas hospitalarias:
     * 1. El paciente debe existir y estar activo.
     * 2. Si se asocia a hospitalización, debe existir, coincidir con el paciente y no duplicar cuenta activa.
     * 3. El saldo inicial debe ser >= 0.
     */
    private void validarReglasDeNegocio(CuentasDTO dto, Long idCuentaActual) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la cuenta hospitalaria no pueden ser nulos");
        }

        if (dto.idPaciente() == null) {
            throw new IllegalArgumentException("El ID del paciente es obligatorio");
        }

        // RN-01: El paciente debe existir y estar activo
        Paciente paciente = pacienteRepository.findById(dto.idPaciente())
                .orElseThrow(() -> new IllegalArgumentException("El paciente con ID " + dto.idPaciente() + " no existe"));

        if (paciente.getActivo() != null && !paciente.getActivo()) {
            throw new IllegalArgumentException("El paciente seleccionado se encuentra inactivo");
        }

        // RN-02: Hospitalización (si viene informada)
        if (dto.idHospitalizacion() != null) {
            Hospitalizaciones hosp = hospitalizacionesRepository.findById(dto.idHospitalizacion())
                    .orElseThrow(() -> new IllegalArgumentException("La hospitalización con ID " + dto.idHospitalizacion() + " no existe"));

            if (!hosp.getIdPaciente().equals(dto.idPaciente())) {
                throw new IllegalArgumentException("La hospitalización no corresponde al paciente seleccionado");
            }

            boolean cuentaActivaExiste;
            if (idCuentaActual == null) {
                cuentaActivaExiste = cuentasRepository.existsByIdHospitalizacionAndActivoTrue(dto.idHospitalizacion());
            } else {
                cuentaActivaExiste = cuentasRepository.existsByIdHospitalizacionAndActivoTrueAndIdCuentaNot(
                        dto.idHospitalizacion(), idCuentaActual
                );
            }

            if (cuentaActivaExiste) {
                throw new IllegalArgumentException("Ya existe una cuenta hospitalaria activa para esta hospitalización");
            }
        }

        // RN-03: Saldo debe ser mayor o igual a cero
        if (dto.saldo() != null && dto.saldo().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo de la cuenta no puede ser negativo");
        }
    }

    @Override
    public List<CuentasDTO> findAll() {
        return cuentasRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CuentasDTO> findActivas() {
        return cuentasRepository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CuentasDTO> findByPaciente(Long idPaciente) {
        if (idPaciente == null) {
            throw new IllegalArgumentException("El ID del paciente es obligatorio");
        }
        return cuentasRepository.findByIdPaciente(idPaciente)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CuentasDTO> findByHospitalizacion(Long idHospitalizacion) {
        if (idHospitalizacion == null) {
            throw new IllegalArgumentException("El ID de hospitalización es obligatorio");
        }
        return cuentasRepository.findByIdHospitalizacion(idHospitalizacion)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CuentasDTO findById(Long id) {
        Cuentas entity = cuentasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuenta hospitalaria con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public CuentasDTO crear(CuentasDTO dto) {
        validarReglasDeNegocio(dto, null);

        Cuentas entity = mapToEntity(dto);
        // Fecha de apertura asignada por el servidor
        entity.setFechaApertura(LocalDateTime.now());
        if (entity.getEstado() == null || entity.getEstado().trim().isEmpty()) {
            entity.setEstado(ESTADO_ABIERTA);
        }
        if (entity.getSaldo() == null) {
            entity.setSaldo(BigDecimal.ZERO);
        }
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }

        Cuentas guardada = cuentasRepository.save(entity);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public CuentasDTO actualizar(CuentasDTO dto, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la cuenta es obligatorio");
        }

        Cuentas existente = cuentasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuenta hospitalaria con ID: " + id));

        validarReglasDeNegocio(dto, id);

        existente.setIdPaciente(dto.idPaciente());
        existente.setIdHospitalizacion(dto.idHospitalizacion());
        if (dto.estado() != null && !dto.estado().trim().isEmpty()) {
            existente.setEstado(dto.estado().trim());
        }
        if (dto.saldo() != null) {
            existente.setSaldo(dto.saldo());
        }
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        Cuentas actualizada = cuentasRepository.save(existente);
        return mapToDTO(actualizada);
    }

    @Override
    @Transactional
    public CuentasDTO ajustarSaldo(Long id, BigDecimal montoAjuste) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la cuenta es obligatorio");
        }
        if (montoAjuste == null) {
            throw new IllegalArgumentException("El monto de ajuste es obligatorio");
        }

        Cuentas existente = cuentasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuenta hospitalaria con ID: " + id));

        BigDecimal saldoActual = existente.getSaldo() != null ? existente.getSaldo() : BigDecimal.ZERO;
        BigDecimal nuevoSaldo = saldoActual.add(montoAjuste);

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El ajuste resultaría en un saldo negativo no permitido (" + nuevoSaldo + ")");
        }

        existente.setSaldo(nuevoSaldo);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0 && ESTADO_ABIERTA.equalsIgnoreCase(existente.getEstado())) {
            existente.setEstado(ESTADO_LIQUIDADA);
        }

        Cuentas actualizada = cuentasRepository.save(existente);
        return mapToDTO(actualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la cuenta es obligatorio");
        }

        Cuentas existente = cuentasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuenta hospitalaria con ID: " + id));

        // Integridad referencial: no eliminar cuenta si tiene pagos registrados
        if (pagosRepository.existsByIdCuenta(id)) {
            throw new IllegalStateException("No se puede eliminar la cuenta hospitalaria porque contiene pagos registrados");
        }

        // Borrado lógico
        existente.setActivo(false);
        cuentasRepository.save(existente);
    }
}
