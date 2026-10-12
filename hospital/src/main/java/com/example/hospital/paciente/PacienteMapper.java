package com.example.hospital.paciente;

import com.example.hospital.paciente.dto.PacienteRequest;
import com.example.hospital.paciente.dto.PacienteResponse;
import org.springframework.stereotype.Component;

@Component
public class PacienteMapper {

    public Paciente toEntity(PacienteRequest r) {
        Paciente p = new Paciente();
        updateEntity(p, r);
        return p;
    }

    public void updateEntity(Paciente p, PacienteRequest r) {
        p.setDpiPaciente(r.dpiPaciente());
        p.setNombre(r.nombre());
        p.setApellidoPaterno(r.apellidoPaterno());
        p.setApellidoMaterno(r.apellidoMaterno());
        p.setFechaNacimiento(r.fechaNacimiento());
        p.setSexo(r.sexo());
        p.setTelefono(r.telefono());
        p.setCorreoElectronico(r.correoElectronico());
        p.setDireccion(r.direccion());
        p.setIdAlergias(r.idAlergias());
        p.setIdTipoDeSangre(r.idTipoDeSangre());
    }

    public PacienteResponse toResponse(Paciente p) {
        return new PacienteResponse(p.getIdPaciente(), p.getDpiPaciente(), p.getNombre(),
                p.getApellidoPaterno(), p.getApellidoMaterno(), p.getFechaNacimiento(), p.getSexo(),
                p.getTelefono(), p.getCorreoElectronico(), p.getDireccion(), p.getFechaRegistro(),
                p.getIdAlergias(), p.getIdTipoDeSangre(), p.getActivo());
    }
}
