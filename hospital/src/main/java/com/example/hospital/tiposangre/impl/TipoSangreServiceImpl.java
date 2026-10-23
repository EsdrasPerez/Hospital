package com.example.hospital.tiposangre.impl;

import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.tiposangre.TipoSangre;
import com.example.hospital.tiposangre.TipoSangreRepository;
import com.example.hospital.tiposangre.TipoSangreService;
import com.example.hospital.tiposangre.dto.TipoSangreDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Regla (coherente con los datos precargados: nombre "A+", descripción "A positivo"):
 * - `nombre` es el tipo de sangre: obligatorio, uno de A+, A-, B+, B-, AB+, AB-, O+, O- (se guarda en mayúsculas) y único
 *   (409 si se repite). Cabe en la columna VARCHAR(10).
 * - `descripcion` es texto libre opcional de hasta 200 caracteres.
 */
@Service
public class TipoSangreServiceImpl implements TipoSangreService {

    // RN-TS-01: valores válidos permitidos para el nombre
    private static final Set<String> TIPOS_VALIDOS = Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    private final TipoSangreRepository tipoSangreRepository;
    private final PacienteRepository pacienteRepository;

    public TipoSangreServiceImpl(TipoSangreRepository tipoSangreRepository,
                                 PacienteRepository pacienteRepository) {
        this.tipoSangreRepository = tipoSangreRepository;
        this.pacienteRepository = pacienteRepository;
    }

    private TipoSangreDTO mapToDTO(TipoSangre t) {
        return new TipoSangreDTO(t.getIdTipoDeSangre(), t.getNombre(), t.getDescripcion(), t.getActivo());
    }

    /** Valida el nombre (valor permitido y único, 409 si se repite) y lo devuelve normalizado en mayúsculas. */
    private String validarNombre(String nombre, Long idActual) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioException("El nombre del tipo de sangre es obligatorio");
        }
        String normalizado = nombre.trim().toUpperCase();
        if (!TIPOS_VALIDOS.contains(normalizado)) {
            throw new ReglaNegocioException("Valor de tipo de sangre inválido: '" + nombre.trim()
                    + "'. Solo se admiten los valores: A+, A-, B+, B-, AB+, AB-, O+, O-");
        }
        boolean existe = idActual == null
                ? tipoSangreRepository.existsByNombreIgnoreCase(normalizado)
                : tipoSangreRepository.existsByNombreIgnoreCaseAndIdTipoDeSangreNot(normalizado, idActual);
        if (existe) {
            throw new DuplicadoException("Ya existe un registro para el tipo de sangre: " + normalizado);
        }
        return normalizado;
    }

    private String descripcionLimpia(String descripcion) {
        return descripcion == null || descripcion.trim().isEmpty() ? null : descripcion.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoSangreDTO> findAll() {
        return tipoSangreRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoSangreDTO findById(Long id) {
        return mapToDTO(tipoSangreRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de sangre con ID: " + id)));
    }

    @Override
    @Transactional
    public TipoSangreDTO crear(TipoSangreDTO dto) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos del tipo de sangre son obligatorios");
        }
        String nombre = validarNombre(dto.nombre(), null);
        TipoSangre entity = new TipoSangre();
        entity.setNombre(nombre);
        entity.setDescripcion(descripcionLimpia(dto.descripcion()));
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return mapToDTO(tipoSangreRepository.save(entity));
    }

    @Override
    @Transactional
    public TipoSangreDTO actualizar(TipoSangreDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del tipo de sangre es obligatorio");
        }
        TipoSangre existente = tipoSangreRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de sangre con ID: " + id));
        if (dto == null) {
            throw new ReglaNegocioException("Los datos del tipo de sangre son obligatorios");
        }
        existente.setNombre(validarNombre(dto.nombre(), id));
        existente.setDescripcion(descripcionLimpia(dto.descripcion()));
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }
        return mapToDTO(tipoSangreRepository.save(existente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del tipo de sangre es obligatorio");
        }
        TipoSangre existente = tipoSangreRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de sangre con ID: " + id));

        // RN-TS-02: no se desactiva un tipo de sangre asignado a algún paciente
        if (pacienteRepository.existsByIdTipoDeSangre(id)) {
            throw new DuplicadoException("No se puede eliminar el tipo de sangre '" + existente.getNombre()
                    + "' porque se encuentra asignado a uno o más pacientes");
        }
        existente.setActivo(false);
        tipoSangreRepository.save(existente);
    }
}
