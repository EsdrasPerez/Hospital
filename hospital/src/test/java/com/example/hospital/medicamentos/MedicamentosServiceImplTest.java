package com.example.hospital.medicamentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.medicamentos.dto.MedicamentosDTO;
import com.example.hospital.medicamentos.impl.MedicamentosServiceImpl;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.tratamientomedicamento.TratamientoMedicamentoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MedicamentosServiceImplTest {

    @Mock MedicamentosRepository medicamentosRepository;
    @Mock TratamientoMedicamentoRepository tratamientoMedicamentoRepository;
    @InjectMocks MedicamentosServiceImpl service;

    private Medicamentos medicamento(Long id, int stock, int stockMinimo, boolean activo) {
        Medicamentos m = new Medicamentos();
        m.setIdMedicamento(id);
        m.setNombreComercial("Paracetamol");
        m.setStock(stock);
        m.setStockMinimo(stockMinimo);
        m.setPrecio(new BigDecimal("10.00"));
        m.setActivo(activo);
        return m;
    }

    private MedicamentosDTO dto(String nombre, String precio) {
        return new MedicamentosDTO(null, nombre, "Acetaminofen", "Tabletas 500mg", "Lab X",
                null, null, precio == null ? null : new BigDecimal(precio), null, null);
    }

    @Test
    void crear_inicializaStockEnCero_yActivo_yGuarda() {
        when(medicamentosRepository.existsByNombreComercialIgnoreCase("Paracetamol")).thenReturn(false);
        when(medicamentosRepository.save(any(Medicamentos.class))).thenAnswer(inv -> {
            Medicamentos m = inv.getArgument(0);
            m.setIdMedicamento(1L);
            return m;
        });

        MedicamentosDTO r = service.crear(dto("  Paracetamol  ", "15.50"));

        assertThat(r.idMedicamento()).isEqualTo(1L);
        assertThat(r.nombreComercial()).isEqualTo("Paracetamol");
        assertThat(r.stock()).isZero();
        assertThat(r.stockMinimo()).isZero();
        assertThat(r.requiereReceta()).isFalse();
        assertThat(r.activo()).isTrue();
    }

    @Test
    void crear_lanzaDuplicado409_siElNombreComercialYaExiste_yNoGuarda() {
        when(medicamentosRepository.existsByNombreComercialIgnoreCase("Paracetamol")).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.crear(dto("Paracetamol", "10.00")));

        verify(medicamentosRepository, never()).save(any());
    }

    @Test
    void crear_usaElStockInicialDelDto() {
        when(medicamentosRepository.existsByNombreComercialIgnoreCase("Paracetamol")).thenReturn(false);
        when(medicamentosRepository.save(any(Medicamentos.class))).thenAnswer(inv -> inv.getArgument(0));
        MedicamentosDTO d = new MedicamentosDTO(null, "Paracetamol", null, null, null, 40, 10,
                new BigDecimal("5.00"), null, null);

        MedicamentosDTO r = service.crear(d);

        assertThat(r.stock()).isEqualTo(40);
        assertThat(r.stockMinimo()).isEqualTo(10);
    }

    @Test
    void actualizar_noModificaElStock_yConservaElActual() {
        Medicamentos m = medicamento(1L, 77, 5, true);
        when(medicamentosRepository.findById(1L)).thenReturn(Optional.of(m));
        when(medicamentosRepository.existsByNombreComercialIgnoreCaseAndIdMedicamentoNot("Paracetamol", 1L)).thenReturn(false);
        when(medicamentosRepository.save(m)).thenReturn(m);
        MedicamentosDTO d = new MedicamentosDTO(null, "Paracetamol", null, null, null, 0, 9,
                new BigDecimal("12.00"), null, null);

        MedicamentosDTO r = service.actualizar(d, 1L);

        assertThat(r.stock()).isEqualTo(77);
        assertThat(r.stockMinimo()).isEqualTo(9);
        assertThat(r.precio()).isEqualByComparingTo("12.00");
    }

    @Test
    void actualizar_lanzaNoEncontrado404_siNoExiste() {
        when(medicamentosRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(dto("X", "1.00"), 9L));

        verify(medicamentosRepository, never()).save(any());
    }

    @Test
    void actualizar_lanzaDuplicado409_siOtroTieneElMismoNombre() {
        when(medicamentosRepository.findById(1L)).thenReturn(Optional.of(medicamento(1L, 1, 1, true)));
        when(medicamentosRepository.existsByNombreComercialIgnoreCaseAndIdMedicamentoNot("Paracetamol", 1L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.actualizar(dto("Paracetamol", "1.00"), 1L));

        verify(medicamentosRepository, never()).save(any());
    }

    @Test
    void findById_lanzaNoEncontrado404_siNoExiste() {
        when(medicamentosRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.findById(9L));
    }

    @Test
    void ajustarStock_y_eliminar_lanzanNoEncontrado404_siNoExiste() {
        when(medicamentosRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ajustarStock(9L, 1));
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(9L));
    }

    private Medicamentos conNombre(Long id, String comercial, String generico, int stock, int min, boolean activo) {
        Medicamentos m = medicamento(id, stock, min, activo);
        m.setNombreComercial(comercial);
        m.setNombreGenerico(generico);
        return m;
    }

    @Test
    void buscar_porCriterio_tambienCoincideConElLaboratorio() {
        Medicamentos amoxi = conNombre(2L, "Amoxicilina 500", "Amoxicilina", 25, 10, true);
        amoxi.setLaboratorio("Farma SA");
        Medicamentos para = conNombre(1L, "Paracetamol 500", "Paracetamol", 130, 20, true);
        para.setLaboratorio("Laboratorios Gt");
        when(medicamentosRepository.findAll()).thenReturn(List.of(amoxi, para));

        assertThat(service.buscar("farma", false, false))
                .extracting(MedicamentosDTO::idMedicamento).containsExactly(2L);
    }

    @Test
    void buscar_combinaCriterioBajoStockYSoloActivos() {
        when(medicamentosRepository.findAll()).thenReturn(List.of(
                conNombre(1L, "Paracetamol", "Acetaminofen", 130, 20, true),
                conNombre(2L, "Amoxicilina", "Amoxicilina", 5, 20, true),
                conNombre(3L, "Insulina", null, 0, 10, true),
                conNombre(4L, "Ibuprofeno inactivo", null, 0, 10, false)));

        assertThat(service.buscar(null, false, false)).hasSize(4);
        assertThat(service.buscar(null, false, true)).extracting(MedicamentosDTO::idMedicamento).containsExactly(1L, 2L, 3L);
        assertThat(service.buscar(null, true, false)).extracting(MedicamentosDTO::idMedicamento).containsExactly(2L, 3L, 4L);
        assertThat(service.buscar(null, true, true)).extracting(MedicamentosDTO::idMedicamento).containsExactly(2L, 3L);
        assertThat(service.buscar("ACETA", false, true)).extracting(MedicamentosDTO::idMedicamento).containsExactly(1L);
        assertThat(service.buscar("inactivo", false, true)).isEmpty();
        assertThat(service.buscar("inactivo", false, false)).extracting(MedicamentosDTO::idMedicamento).containsExactly(4L);
        assertThat(service.buscar("insu", true, true)).extracting(MedicamentosDTO::idMedicamento).containsExactly(3L);
    }

    @Test
    void ajustarStock_sumaElCambioAlStockActual_yGuarda() {
        Medicamentos m = medicamento(1L, 10, 5, true);
        when(medicamentosRepository.findById(1L)).thenReturn(Optional.of(m));
        when(medicamentosRepository.save(m)).thenReturn(m);

        MedicamentosDTO r = service.ajustarStock(1L, -4);

        assertThat(r.stock()).isEqualTo(6);
        verify(medicamentosRepository).save(m);
    }

    @Test
    void ajustarStock_lanzaReglaNegocio_siElStockQuedariaNegativo_yNoGuarda() {
        Medicamentos m = medicamento(1L, 3, 5, true);
        when(medicamentosRepository.findById(1L)).thenReturn(Optional.of(m));

        assertThrows(ReglaNegocioException.class, () -> service.ajustarStock(1L, -4));

        assertThat(m.getStock()).isEqualTo(3);
        verify(medicamentosRepository, never()).save(any());
    }

    @Test
    void ajustarStock_lanzaReglaNegocio_siElMedicamentoEstaInactivo_yNoGuarda() {
        when(medicamentosRepository.findById(1L)).thenReturn(Optional.of(medicamento(1L, 10, 5, false)));

        assertThrows(ReglaNegocioException.class, () -> service.ajustarStock(1L, 5));

        verify(medicamentosRepository, never()).save(any());
    }

    @Test
    void ajustarStock_lanzaReglaNegocio_siElAjusteEsCero() {
        assertThrows(ReglaNegocioException.class, () -> service.ajustarStock(1L, 0));

        verify(medicamentosRepository, never()).save(any());
    }

    @Test
    void eliminar_lanzaDuplicado409_siEstaAsociadoAUnTratamiento_yNoGuarda() {
        when(medicamentosRepository.findById(1L)).thenReturn(Optional.of(medicamento(1L, 10, 5, true)));
        when(tratamientoMedicamentoRepository.existsByIdMedicamento(1L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.eliminar(1L));

        verify(medicamentosRepository, never()).save(any());
    }

    @Test
    void eliminar_haceBorradoLogico_siNoTieneTratamientos() {
        Medicamentos m = medicamento(1L, 10, 5, true);
        when(medicamentosRepository.findById(1L)).thenReturn(Optional.of(m));
        when(tratamientoMedicamentoRepository.existsByIdMedicamento(1L)).thenReturn(false);

        service.eliminar(1L);

        assertThat(m.getActivo()).isFalse();
        verify(medicamentosRepository).save(m);
        verify(medicamentosRepository, never()).deleteById(any());
    }

    @Test
    void findBajoStock_devuelveSoloLosQueEstanEnOPorDebajoDelMinimo() {
        when(medicamentosRepository.findByActivoTrue()).thenReturn(List.of(
                medicamento(1L, 2, 5, true),
                medicamento(2L, 5, 5, true),
                medicamento(3L, 50, 5, true)));

        List<MedicamentosDTO> r = service.findBajoStock();

        assertThat(r).extracting(MedicamentosDTO::idMedicamento).containsExactly(1L, 2L);
    }
}
