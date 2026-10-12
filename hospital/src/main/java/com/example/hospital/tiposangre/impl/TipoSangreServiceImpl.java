package com.example.hospital.tiposangre.impl;

import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.tiposangre.TipoSangre;
import com.example.hospital.tiposangre.TipoSangreRepository;
import com.example.hospital.tiposangre.TipoSangreService;
import com.example.hospital.tiposangre.dto.TipoSangreDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TipoSangreServiceImpl implements TipoSangreService {

    // RN-TS-01: Valores válidos permitidos para la descripción
    private static final Set<String> TIPOS_VALIDOS = Set.of(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    );

    private final TipoSangreRepository tipoSangreRepository;
    private final PacienteRepository pacienteRepository;

    public TipoSangreServiceImpl(TipoSangreRepository tipoSangreRepository,
                                 PacienteRepository pacienteRepository) {
        this.tipoSangreRepository = tipoSangreRepository;
        this.pacienteRepository = pacienteRepository;
    }

    private TipoSangreDTO mapToDTO(TipoSangre t) {
        return new TipoSangreDTO(
                t.getIdTipoDeSangre(),
                t.getNombre(),
                t.getDescripcion(),
                t.getActivo()
        );
    }

    private TipoSangre mapToEntity(TipoSangreDTO dto) {
        TipoSangre t = new TipoSangre();
        t.setNombre(dto.nombre());
        t.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim().toUpperCase() : null);
        t.setActivo(dto.activo());
        return t;
    }

    /**
     * Valida RN-TS-01: descripción única y dentro de los valores válidos:
     * A+, A-, B+, B-, AB+, AB-, O+, O-.
     */
    private void validarDescripcion(String descripcion, Long idTipoActual) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del tipo de sangre es obligatoria");
        }

        String descNormalizada = descripcion.trim().toUpperCase();

        if (!TIPOS_VALIDOS.contains(descNormalizada)) {
            throw new IllegalArgumentException("Valor de tipo de sangre inválido: '" + descripcion +
                    "'. Solo se admiten los valores: A+, A-, B+, B-, AB+, AB-, O+, O-");
        }

        boolean existe;
        if (idTipoActual == null) {
            existe = tipoSangreRepository.existsByDescripcionIgnoreCase(descNormalizada);
        } else {
            existe = tipoSangreRepository.existsByDescripcionIgnoreCaseAndIdTipoDeSangreNot(descNormalizada, idTipoActual);
        }

        if (existe) {
            throw new IllegalArgumentException("Ya existe un registro para el tipo de sangre: " + descNormalizada);
        }
    }

    @Override
    public List<TipoSangreDTO> findAll() {
        return tipoSangreRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TipoSangreDTO findById(Long id) {
        TipoSangre entity = tipoSangreRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de sangre con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public TipoSangreDTO crear(TipoSangreDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del tipo de sangre son obligatorios");
        }

        validarDescripcion(dto.descripcion(), null);

        TipoSangre entity = mapToEntity(dto);
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }
        if (entity.getNombre() == null || entity.getNombre().isBlank()) {
            entity.setNombre("Tipo de Sangre " + entity.getDescripcion());
        }

        TipoSangre guardado = tipoSangreRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public TipoSangreDTO actualizar(TipoSangreDTO dto, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del tipo de sangre es obligatorio");
        }

        TipoSangre existente = tipoSangreRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de sangre con ID: " + id));

        validarDescripcion(dto.descripcion(), id);

        existente.setDescripcion(dto.descripcion().trim().toUpperCase());
        if (dto.nombre() != null && !dto.nombre().isBlank()) {
            existente.setNombre(dto.nombre());
        }
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        TipoSangre actualizado = tipoSangreRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del tipo de sangre es obligatorio");
        }

        TipoSangre existente = tipoSangreRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de sangre con ID: " + id));

        // RN-TS-02: No se puede eliminar un tipo de sangre asignado a algún paciente
        boolean asignadoAPacientes = pacienteRepository.existsByIdTipoDeSangre(id);
        if (asignadoAPacientes) {
            throw new IllegalArgumentException("No se puede eliminar el tipo de sangre '" + existente.getDescripcion() +
                    "' porque se encuentra asignado a uno o más pacientes");
        }

        // Eliminación lógica
        existente.setActivo(false);
        tipoSangreRepository.save(existente);
    }
}
