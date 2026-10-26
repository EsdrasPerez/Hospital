package com.example.hospital;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración: levanta el contexto completo y necesita PostgreSQL.
 * Solo corre si está definida la variable de entorno DB_PASSWORD; sin base de datos
 * se omite, para que `mvnw test` (pruebas unitarias con mocks) pase en cualquier equipo.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class HospitalApplicationTests {

	@Test
	void contextLoads() {
	}

}
