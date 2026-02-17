package com.fallapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Aplicación principal de FallApp
 * 
 * Backend API REST para gestión de fallas valencianas
 * - PostgreSQL 13+
 * - Spring Boot 3.2+
 * - JWT Authentication
 * - OpenAPI/Swagger documentation
 */
@SpringBootApplication
@EnableAsync
public class FallappApplication {

	public static void main(String[] args) {
		SpringApplication.run(FallappApplication.class, args);
		System.out.println("=".repeat(60));
		System.out.println("🎭 FallApp Backend - API REST");
		System.out.println("=".repeat(60));
		System.out.println("✅ Servidor iniciado en: http://localhost:8080");
		System.out.println("📚 Swagger UI: http://localhost:8080/swagger-ui.html");
		System.out.println("📖 API Docs: http://localhost:8080/v3/api-docs");
		System.out.println("=".repeat(60));
	}

}
