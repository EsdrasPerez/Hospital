

CREATE DATABASE "Hospital"
  ;
  CREATE DATABASE hospital_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
USE hospital_db;

-- =====================================================
-- CATÁLOGOS (eliminan dependencias transitivas → 3FN)
-- =====================================================

CREATE TABLE tipos_sangre (
    id_tipo_sangre INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(5) NOT NULL UNIQUE
);

CREATE TABLE estados_cita (
    id_estado_cita INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE estados_habitacion (
    id_estado_habitacion INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE tipos_habitacion (
    id_tipo_habitacion INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(50) NOT NULL UNIQUE,
    costo_dia DECIMAL(10,2) NOT NULL
);

CREATE TABLE turnos (
    id_turno INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL UNIQUE,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL
);

CREATE TABLE niveles_urgencia (
    id_nivel_urgencia INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(5) NOT NULL UNIQUE,          -- Ej: N1, N2, N3
    descripcion VARCHAR(100) NOT NULL,
    tiempo_max_espera_min INT NOT NULL,
    color VARCHAR(20)
);

CREATE TABLE estados_hospitalizacion (
    id_estado_hosp INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE tipos_diagnostico (
    id_tipo_diagnostico INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE metodos_pago (
    id_metodo_pago INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE estados_factura (
    id_estado_factura INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE roles_usuario (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL UNIQUE
);

-- =====================================================
-- ESTRUCTURA ORGANIZACIONAL
-- =====================================================

CREATE TABLE departamentos (
    id_departamento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    ubicacion VARCHAR(100),
    telefono VARCHAR(20)
);

-- CLÍNICAS: unidad médica especializada dentro del hospital
CREATE TABLE clinicas (
    id_clinica INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    id_departamento INT NOT NULL,
    ubicacion VARCHAR(100),
    telefono VARCHAR(20),
    activa BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento)
);

CREATE TABLE especialidades (
    id_especialidad INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    id_clinica INT,
    FOREIGN KEY (id_clinica) REFERENCES clinicas(id_clinica)
);

-- =====================================================
-- PERSONAS (Pacientes)
-- =====================================================

CREATE TABLE pacientes (
    id_paciente INT AUTO_INCREMENT PRIMARY KEY,
    numero_expediente VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    sexo CHAR(1) NOT NULL CHECK (sexo IN ('M','F','O')),
    curp VARCHAR(18) UNIQUE,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    id_tipo_sangre INT,
    contacto_emergencia VARCHAR(150),
    telefono_emergencia VARCHAR(20),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_tipo_sangre) REFERENCES tipos_sangre(id_tipo_sangre)
);

-- Alergias: relación N:M (un paciente puede tener varias)
CREATE TABLE alergias (
    id_alergia INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE paciente_alergia (
    id_paciente INT NOT NULL,
    id_alergia INT NOT NULL,
    observaciones VARCHAR(200),
    PRIMARY KEY (id_paciente, id_alergia),
    FOREIGN KEY (id_paciente) REFERENCES pacientes(id_paciente) ON DELETE CASCADE,
    FOREIGN KEY (id_alergia) REFERENCES alergias(id_alergia)
);

-- =====================================================
-- PERSONAL MÉDICO
-- =====================================================

CREATE TABLE medicos (
    id_medico INT AUTO_INCREMENT PRIMARY KEY,
    cedula_profesional VARCHAR(30) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    telefono VARCHAR(20),
    email VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE
);

-- Un médico puede laborar en varias clínicas y con varias especialidades
CREATE TABLE medico_especialidad (
    id_medico INT NOT NULL,
    id_especialidad INT NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id_medico, id_especialidad),
    FOREIGN KEY (id_medico) REFERENCES medicos(id_medico) ON DELETE CASCADE,
    FOREIGN KEY (id_especialidad) REFERENCES especialidades(id_especialidad)
);

CREATE TABLE enfermeros (
    id_enfermero INT AUTO_INCREMENT PRIMARY KEY,
    cedula_profesional VARCHAR(30) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    telefono VARCHAR(20),
    id_departamento INT,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento)
);

-- =====================================================
-- HABITACIONES
-- =====================================================

CREATE TABLE habitaciones (
    id_habitacion INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(10) UNIQUE NOT NULL,
    piso INT NOT NULL,
    id_tipo_habitacion INT NOT NULL,
    capacidad INT NOT NULL DEFAULT 1,
    id_estado_habitacion INT NOT NULL,
    FOREIGN KEY (id_tipo_habitacion) REFERENCES tipos_habitacion(id_tipo_habitacion),
    FOREIGN KEY (id_estado_habitacion) REFERENCES estados_habitacion(id_estado_habitacion)
);

-- =====================================================
-- CITAS Y CONSULTAS
-- =====================================================

CREATE TABLE citas (
    id_cita INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    id_medico INT NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    motivo VARCHAR(255),
    id_estado_cita INT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cita (id_medico, fecha, hora),
    FOREIGN KEY (id_paciente) REFERENCES pacientes(id_paciente),
    FOREIGN KEY (id_medico) REFERENCES medicos(id_medico),
    FOREIGN KEY (id_estado_cita) REFERENCES estados_cita(id_estado_cita)
);

CREATE TABLE consultas (
    id_consulta INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    id_medico INT NOT NULL,
    id_cita INT,
    id_clinica INT,
    fecha_hora DATETIME NOT NULL,
    sintomas TEXT,
    peso DECIMAL(5,2),
    talla DECIMAL(5,2),
    temperatura DECIMAL(4,2),
    presion_arterial VARCHAR(20),
    frecuencia_cardiaca INT,
    notas TEXT,
    FOREIGN KEY (id_paciente) REFERENCES pacientes(id_paciente),
    FOREIGN KEY (id_medico) REFERENCES medicos(id_medico),
    FOREIGN KEY (id_cita) REFERENCES citas(id_cita),
    FOREIGN KEY (id_clinica) REFERENCES clinicas(id_clinica)
);

CREATE TABLE diagnosticos (
    id_diagnostico INT AUTO_INCREMENT PRIMARY KEY,
    id_consulta INT NOT NULL,
    codigo_cie10 VARCHAR(10),
    descripcion TEXT NOT NULL,
    id_tipo_diagnostico INT NOT NULL,
    fecha DATE NOT NULL,
    FOREIGN KEY (id_consulta) REFERENCES consultas(id_consulta),
    FOREIGN KEY (id_tipo_diagnostico) REFERENCES tipos_diagnostico(id_tipo_diagnostico)
);

CREATE TABLE tratamientos (
    id_tratamiento INT AUTO_INCREMENT PRIMARY KEY,
    id_diagnostico INT NOT NULL,
    descripcion TEXT NOT NULL,
    duracion_dias INT,
    indicaciones TEXT,
    fecha_inicio DATE,
    fecha_fin DATE,
    FOREIGN KEY (id_diagnostico) REFERENCES diagnosticos(id_diagnostico)
);

-- =====================================================
-- MÓDULO DE EMERGENCIA
-- =====================================================

CREATE TABLE emergencias (
    id_emergencia INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT,                              -- NULL si es paciente no registrado
    nombre_temporal VARCHAR(150),                 -- Para pacientes sin expediente
    fecha_hora_ingreso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT NOT NULL,
    id_nivel_urgencia INT NOT NULL,
    id_medico_triage INT,
    id_medico_tratante INT,
    signos_vitales TEXT,
    id_habitacion INT,                            -- Área de observación / shock trauma
    fecha_hora_alta DATETIME,
    destino_alta VARCHAR(100),                    -- Domicilio, Hospitalización, Traslado, Defunción
    FOREIGN KEY (id_paciente) REFERENCES pacientes(id_paciente),
    FOREIGN KEY (id_nivel_urgencia) REFERENCES niveles_urgencia(id_nivel_urgencia),
    FOREIGN KEY (id_medico_triage) REFERENCES medicos(id_medico),
    FOREIGN KEY (id_medico_tratante) REFERENCES medicos(id_medico),
    FOREIGN KEY (id_habitacion) REFERENCES habitaciones(id_habitacion)
);

-- Triaje: historial de reevaluaciones del nivel de urgencia
CREATE TABLE triaje_registros (
    id_triaje INT AUTO_INCREMENT PRIMARY KEY,
    id_emergencia INT NOT NULL,
    id_nivel_urgencia INT NOT NULL,
    id_medico INT NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    FOREIGN KEY (id_emergencia) REFERENCES emergencias(id_emergencia) ON DELETE CASCADE,
    FOREIGN KEY (id_nivel_urgencia) REFERENCES niveles_urgencia(id_nivel_urgencia),
    FOREIGN KEY (id_medico) REFERENCES medicos(id_medico)
);

-- Procedimientos realizados en emergencia
CREATE TABLE procedimientos_emergencia (
    id_procedimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_emergencia INT NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_medico INT NOT NULL,
    observaciones TEXT,
    FOREIGN KEY (id_emergencia) REFERENCES emergencias(id_emergencia) ON DELETE CASCADE,
    FOREIGN KEY (id_medico) REFERENCES medicos(id_medico)
);

-- =====================================================
-- FARMACIA
-- =====================================================

CREATE TABLE medicamentos (
    id_medicamento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_comercial VARCHAR(150) NOT NULL,
    nombre_generico VARCHAR(150) NOT NULL,
    presentacion VARCHAR(100),
    concentracion VARCHAR(50),
    laboratorio VARCHAR(100),
    stock INT DEFAULT 0,
    stock_minimo INT DEFAULT 10,
    precio DECIMAL(10,2),
    requiere_receta BOOLEAN DEFAULT TRUE
);

CREATE TABLE recetas (
    id_receta INT AUTO_INCREMENT PRIMARY KEY,
    id_consulta INT,                             -- Puede ser NULL si viene de emergencia
    id_emergencia INT,
    id_medicamento INT NOT NULL,
    dosis VARCHAR(100) NOT NULL,
    frecuencia VARCHAR(100) NOT NULL,
    duracion VARCHAR(50),
    cantidad INT,
    indicaciones TEXT,
    CHECK (id_consulta IS NOT NULL OR id_emergencia IS NOT NULL),
    FOREIGN KEY (id_consulta) REFERENCES consultas(id_consulta),
    FOREIGN KEY (id_emergencia) REFERENCES emergencias(id_emergencia),
    FOREIGN KEY (id_medicamento) REFERENCES medicamentos(id_medicamento)
);

-- =====================================================
-- HOSPITALIZACIÓN
-- =====================================================

CREATE TABLE hospitalizaciones (
    id_hospitalizacion INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    id_habitacion INT NOT NULL,
    id_medico_responsable INT NOT NULL,
    id_clinica INT,
    fecha_ingreso DATETIME NOT NULL,
    fecha_alta DATETIME,
    motivo_ingreso TEXT,
    diagnostico_ingreso TEXT,
    id_estado_hosp INT NOT NULL,
    FOREIGN KEY (id_paciente) REFERENCES pacientes(id_paciente),
    FOREIGN KEY (id_habitacion) REFERENCES habitaciones(id_habitacion),
    FOREIGN KEY (id_medico_responsable) REFERENCES medicos(id_medico),
    FOREIGN KEY (id_clinica) REFERENCES clinicas(id_clinica),
    FOREIGN KEY (id_estado_hosp) REFERENCES estados_hospitalizacion(id_estado_hosp)
);

CREATE TABLE asignacion_enfermeros (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_enfermero INT NOT NULL,
    id_hospitalizacion INT NOT NULL,
    fecha_asignacion DATE NOT NULL,
    id_turno INT NOT NULL,
    FOREIGN KEY (id_enfermero) REFERENCES enfermeros(id_enfermero),
    FOREIGN KEY (id_hospitalizacion) REFERENCES hospitalizaciones(id_hospitalizacion) ON DELETE CASCADE,
    FOREIGN KEY (id_turno) REFERENCES turnos(id_turno)
);

-- =====================================================
-- FACTURACIÓN
-- =====================================================

CREATE TABLE facturas (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    id_hospitalizacion INT,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    iva DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    id_metodo_pago INT NOT NULL,
    id_estado_factura INT NOT NULL,
    FOREIGN KEY (id_paciente) REFERENCES pacientes(id_paciente),
    FOREIGN KEY (id_hospitalizacion) REFERENCES hospitalizaciones(id_hospitalizacion),
    FOREIGN KEY (id_metodo_pago) REFERENCES metodos_pago(id_metodo_pago),
    FOREIGN KEY (id_estado_factura) REFERENCES estados_factura(id_estado_factura)
);

CREATE TABLE detalle_factura (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_factura INT NOT NULL,
    concepto VARCHAR(200) NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10,2) NOT NULL,
    importe DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES facturas(id_factura) ON DELETE CASCADE
);

-- =====================================================
-- USUARIOS DEL SISTEMA
-- =====================================================

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    id_medico INT,
    id_enfermero INT,
    activo BOOLEAN DEFAULT TRUE,
    ultimo_acceso DATETIME,
    FOREIGN KEY (id_rol) REFERENCES roles_usuario(id_rol),
    FOREIGN KEY (id_medico) REFERENCES medicos(id_medico),
    FOREIGN KEY (id_enfermero) REFERENCES enfermeros(id_enfermero)
);

-- =====================================================
-- DATOS INICIALES EN CATÁLOGOS
-- =====================================================

INSERT INTO tipos_sangre (descripcion) VALUES
('A+'),('A-'),('B+'),('B-'),('AB+'),('AB-'),('O+'),('O-');

INSERT INTO estados_cita (descripcion) VALUES
('Programada'),('Confirmada'),('Atendida'),('Cancelada'),('No asistió');

INSERT INTO estados_habitacion (descripcion) VALUES
('Disponible'),('Ocupada'),('Mantenimiento');

INSERT INTO tipos_habitacion (descripcion, costo_dia) VALUES
('Individual', 2500.00),('Doble', 1800.00),('Compartida', 1200.00),
('UCI', 6500.00),('Pediátrica', 2000.00),('Observación', 800.00);

INSERT INTO turnos (descripcion, hora_inicio, hora_fin) VALUES
('Matutino','06:00:00','14:00:00'),
('Vespertino','14:00:00','22:00:00'),
('Nocturno','22:00:00','06:00:00');

INSERT INTO niveles_urgencia (codigo, descripcion, tiempo_max_espera_min, color) VALUES
('N1','Reanimación - Atención inmediata',0,'Rojo'),
('N2','Emergencia - Muy urgente',15,'Naranja'),
('N3','Urgencia - Requiere atención pronta',30,'Amarillo'),
('N4','Urgencia menor',60,'Verde'),
('N5','No urgente - Consulta general',120,'Azul');

INSERT INTO estados_hospitalizacion (descripcion) VALUES
('Activa'),('Alta'),('Traslado'),('Fallecimiento');

INSERT INTO tipos_diagnostico (descripcion) VALUES
('Presuntivo'),('Definitivo'),('Diferencial');

INSERT INTO metodos_pago (descripcion) VALUES
('Efectivo'),('Tarjeta'),('Transferencia'),('Seguro');

INSERT INTO estados_factura (descripcion) VALUES
('Pendiente'),('Pagada'),('Cancelada');

INSERT INTO roles_usuario (descripcion) VALUES
('Admin'),('Medico'),('Enfermero'),('Recepcion'),('Farmacia'),('Emergencia');