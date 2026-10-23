package com.example.hospital.tratamientomedicamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.medicamentos.Medicamentos;
import com.example.hospital.medicamentos.MedicamentosRepository;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.tratamiento.Tratamiento;
import com.example.hospital.tratamiento.TratamientoRepository;
import com.example.hospital.tratamientomedicamento.TratamientoMedicamento.TratamientoMedicamentoId;
import com.example.hospital.tratamientomedicamento.dto.TratamientoMedicamentoDTO;
import com.example.hospital.tratamientomedicamento.impl.TratamientoMedicamentoServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TratamientoMedicamentoServiceImplTest {

    @Mock TratamientoMedicamentoRepository repo;
    @Mock TratamientoRepository tratamientoRepository;
    @Mock MedicamentosRepository medicamentosRepository;
    @InjectMocks TratamientoMedicamentoServiceImpl service;

    private Tratamiento tratamiento(boolean activo) {
        Tratamiento t = new Tratamiento();
        t.setActivo(activo);
        return t;
    }

    private Medicamentos medicamento(boolean activo, int stock) {
        Medicamentos m = new Medicamentos();
        m.setActivo(activo);
        m.setStock(stock);
        return m;
    }

    @Test
    void crear_asignaMedicamentoActivoATratamientoActivo_aunqueNoHayaStock() {
        when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(tratamiento(true)));
        when(medicamentosRepository.findById(2L)).thenReturn(Optional.of(medicamento(true, 0)));
        when(repo.existsById(new TratamientoMedicamentoId(2L, 1L))).thenReturn(false);
        when(repo.save(any(TratamientoMedicamento.class))).thenAnswer(inv -> inv.getArgument(0));

        TratamientoMedicamentoDTO r = service.crear(new TratamientoMedicamentoDTO(2L, 1L));

        assertThat(r.idMedicamento()).isEqualTo(2L);
        assertThat(r.idTratamiento()).isEqualTo(1L);
    }

    @Test
    void crear_lanza422_siElTratamientoNoExisteOEstaInactivo() {
        when(tratamientoRepository.findById(9L)).thenReturn(Optional.empty());
        when(tratamientoRepository.findById(2L)).thenReturn(Optional.of(tratamiento(false)));

        assertThrows(ReglaNegocioException.class, () -> service.crear(new TratamientoMedicamentoDTO(1L, 9L)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(new TratamientoMedicamentoDTO(1L, 2L)));

        verify(repo, never()).save(any());
    }

    @Test
    void crear_lanza422_siElMedicamentoNoExisteOEstaInactivo() {
        when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(tratamiento(true)));
        when(medicamentosRepository.findById(9L)).thenReturn(Optional.empty());
        when(medicamentosRepository.findById(4L)).thenReturn(Optional.of(medicamento(false, 10)));

        assertThrows(ReglaNegocioException.class, () -> service.crear(new TratamientoMedicamentoDTO(9L, 1L)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(new TratamientoMedicamentoDTO(4L, 1L)));

        verify(repo, never()).save(any());
    }

    @Test
    void crear_lanzaDuplicado409_siLaRelacionYaExiste() {
        when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(tratamiento(true)));
        when(medicamentosRepository.findById(1L)).thenReturn(Optional.of(medicamento(true, 5)));
        when(repo.existsById(new TratamientoMedicamentoId(1L, 1L))).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.crear(new TratamientoMedicamentoDTO(1L, 1L)));

        verify(repo, never()).save(any());
    }

    @Test
    void buscar_filtraPorTratamientoYMedicamentoCombinados() {
        when(repo.findAll()).thenReturn(List.of(
                new TratamientoMedicamento(1L, 1L),
                new TratamientoMedicamento(2L, 1L),
                new TratamientoMedicamento(1L, 2L)));

        assertThat(service.buscar(null, null)).hasSize(3);
        assertThat(service.buscar(1L, null)).hasSize(2);
        assertThat(service.buscar(null, 1L)).hasSize(2);
        assertThat(service.buscar(1L, 2L)).hasSize(1);
        assertThat(service.buscar(5L, null)).isEmpty();
    }

    @Test
    void eliminar_lanzaNoEncontrado404_siLaRelacionNoExiste() {
        when(repo.existsById(new TratamientoMedicamentoId(1L, 1L))).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(1L, 1L));
    }
}
