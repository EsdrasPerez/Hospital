package com.example.hospital.entity;

import jakarta.persistence.*;
import lombok.Data;

public class Alergias {



        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer idAlergia;
        private String nombre;
        private String descripcion;

}
