package com.example.hospital.tiposangre;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tipo_sangre")
@Data
public class TipoSangre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoDeSangre;
    private String nombre;
    private String descripcion;
    private Boolean activo;

}
