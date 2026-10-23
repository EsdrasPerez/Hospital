package com.example.hospital.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.hospital.alergias.dto.AlergiasDTO;
import com.example.hospital.clinicas.dto.ClinicasDTO;
import com.example.hospital.departamentos.dto.DepartamentosDTO;
import com.example.hospital.enfermeros.dto.EnfermerosDTO;
import com.example.hospital.especialidadclinica.dto.EspecialidadClinicaDTO;
import com.example.hospital.especialidadmedico.dto.EspecialidadMedicoDTO;
import com.example.hospital.estadocita.dto.EstadoCitaDTO;
import com.example.hospital.estadohabitacion.dto.EstadoHabitacionDTO;
import com.example.hospital.estadosfactura.dto.EstadosFacturaDTO;
import com.example.hospital.estadoshospitalizacion.dto.EstadosHospitalizacionDTO;
import com.example.hospital.medico.dto.MedicoDTO;
import com.example.hospital.medicoespecialidades.dto.MedicoEspecialidadesDTO;
import com.example.hospital.metodospago.dto.MetodosPagoDTO;
import com.example.hospital.nivelesurgencia.dto.NivelesUrgenciaDTO;
import com.example.hospital.rol.dto.RolDTO;
import com.example.hospital.tiposangre.dto.TipoSangreDTO;
import com.example.hospital.tiposdiagnostico.dto.TiposDiagnosticoDTO;
import com.example.hospital.tiposhabitacion.dto.TiposHabitacionDTO;
import com.example.hospital.turnos.dto.TurnosDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Bean Validation (400 con `detalles`) de los DTO de administración, con los límites de las columnas de Hosp_postgres.sql. */
class ValidacionDtosAdministracionTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private <T> Set<String> campos(T dto) {
        return validator.validate(dto).stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private static String x(int n) {
        return "x".repeat(n);
    }

    @Test
    void medico() {
        assertThat(campos(new MedicoDTO(null, null, null, null, null, null, null, null)))
                .containsExactlyInAnyOrder("noColegiado", "nombre", "apellidoPaterno");
        assertThat(campos(new MedicoDTO(null, "COL-1", "Ana", "Perez", null, "55501234", "ana@hospital.test", null))).isEmpty();
        assertThat(campos(new MedicoDTO(null, x(31), x(101), x(101), x(101), "123", "mal", null)))
                .containsExactlyInAnyOrder("noColegiado", "nombre", "apellidoPaterno", "apellidoMaterno", "telefono", "email");
    }

    @Test
    void especialidades_y_asignacion() {
        assertThat(campos(new EspecialidadMedicoDTO(null, " ", null, null))).containsExactly("nombre");
        assertThat(campos(new EspecialidadClinicaDTO(null, x(101), x(256), null))).containsExactlyInAnyOrder("nombre", "descripcion");
        assertThat(campos(new MedicoEspecialidadesDTO(null, null))).containsExactlyInAnyOrder("idEspecialidad", "idMedico");
        assertThat(campos(new MedicoEspecialidadesDTO(1L, 2L))).isEmpty();
    }

    @Test
    void enfermero_turno_departamento_clinica() {
        assertThat(campos(new EnfermerosDTO(null, null, null, null, null, null, null, null)))
                .containsExactlyInAnyOrder("cedulaProfesional", "nombre", "apellidoPaterno", "idDepartamento");
        assertThat(campos(new EnfermerosDTO(null, x(31), "A", "B", null, x(21), 1L, null))).containsExactlyInAnyOrder("cedulaProfesional", "telefono");
        assertThat(campos(new TurnosDTO(null, null, null, null, null))).containsExactlyInAnyOrder("descripcion", "horaInicio", "horaFin");
        assertThat(campos(new TurnosDTO(null, x(31), LocalTime.NOON, LocalTime.MIDNIGHT, null))).containsExactly("descripcion");
        assertThat(campos(new DepartamentosDTO(null, " ", x(256), x(21), null))).containsExactlyInAnyOrder("nombre", "descripcion", "telefono");
        assertThat(campos(new ClinicasDTO(null, null, null, x(256), null, null, null, null))).containsExactlyInAnyOrder("nombre", "ubicacion", "idDepartamento");
    }

    @Test
    void alergia_y_tipoDeSangre() {
        assertThat(campos(new AlergiasDTO(null, null, x(256), null))).containsExactlyInAnyOrder("nombre", "descripcion");
        assertThat(campos(new TipoSangreDTO(null, "AB-", "AB negativo", null))).isEmpty();
        assertThat(campos(new TipoSangreDTO(null, "ab+", null, null))).isEmpty();
        assertThat(campos(new TipoSangreDTO(null, "Z+", null, null))).containsExactly("nombre");
        assertThat(campos(new TipoSangreDTO(null, null, x(201), null))).containsExactlyInAnyOrder("nombre", "descripcion");
    }

    @Test
    void nivelUrgencia_tipos_metodoPago_y_estados() {
        assertThat(campos(new NivelesUrgenciaDTO(null, "N1", "Resucitacion", 0, "Rojo", null))).isEmpty();
        assertThat(campos(new NivelesUrgenciaDTO(null, "X1", null, -1, x(21), null)))
                .containsExactlyInAnyOrder("codigo", "descripcion", "tiempoMaxEsperaMin", "color");
        assertThat(campos(new NivelesUrgenciaDTO(null, "N123456", "d", 1, null, null))).containsExactly("codigo");
        assertThat(campos(new NivelesUrgenciaDTO(null, "N1", "d", null, null, null))).containsExactly("tiempoMaxEsperaMin");

        assertThat(campos(new TiposHabitacionDTO(null, "Suite", new BigDecimal("250.00"), null))).isEmpty();
        assertThat(campos(new TiposHabitacionDTO(null, null, BigDecimal.ZERO, null))).containsExactlyInAnyOrder("descripcion", "costoDia");
        assertThat(campos(new TiposHabitacionDTO(null, "S", new BigDecimal("1.234"), null))).containsExactly("costoDia");
        assertThat(campos(new TiposHabitacionDTO(null, "S", null, null))).containsExactly("costoDia");

        assertThat(campos(new TiposDiagnosticoDTO(null, x(31), null))).containsExactly("descripcion");
        assertThat(campos(new MetodosPagoDTO(null, x(51), null))).containsExactly("descripcion");
        assertThat(campos(new EstadosFacturaDTO(null, x(31), null))).containsExactly("descripcion");
        assertThat(campos(new EstadosHospitalizacionDTO(null, " ", null))).containsExactly("descripcion");
        assertThat(campos(new EstadoCitaDTO(null, x(31), null, null))).containsExactlyInAnyOrder("nombre", "descripcion");
        assertThat(campos(new EstadoHabitacionDTO(null, "N", "Disponible", null))).isEmpty();
    }

    @Test
    void rol() {
        assertThat(campos(new RolDTO(null, null, x(201), null))).containsExactlyInAnyOrder("nombre", "descripcion");
        assertThat(campos(new RolDTO(null, "AUDITOR", "d", null))).isEmpty();
    }
}
