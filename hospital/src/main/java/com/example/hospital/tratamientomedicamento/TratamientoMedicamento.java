package com.example.hospital.tratamientomedicamento;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "tratamiento_medicamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TratamientoMedicamento.TratamientoMedicamentoId.class)
public class TratamientoMedicamento {

    @Id
    private Long idMedicamento;
    @Id
    private Long idTratamiento;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TratamientoMedicamentoId implements Serializable {
        private Long idMedicamento;
        private Long idTratamiento;
    }
}
