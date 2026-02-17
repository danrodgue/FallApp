package com.fallapp.controller;

import com.fallapp.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para probar el envío de emails
 * SOLO PARA DESARROLLO - Eliminar o proteger en producción
 */
@RestController
@RequestMapping("/api/test-email")
public class TestEmailController {

    @Autowired
    private EmailService emailService;

    /**
     * Enviar email simple de prueba
     * GET http://localhost:8080/api/test-email/simple?to=destino@ejemplo.com
     */
    @GetMapping("/simple")
    public ResponseEntity<?> sendSimpleEmail(
            @RequestParam String to,
            @RequestParam(defaultValue = "Email de prueba") String subject,
            @RequestParam(defaultValue = "Este es un email de prueba desde FallApp") String text) {
        try {
            emailService.sendSimpleEmail(to, subject, text);
            return ResponseEntity.ok(createResponse("Email simple enviado correctamente a " + to));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    /**
     * Enviar email HTML de prueba
     * GET http://localhost:8080/api/test-email/html?to=destino@ejemplo.com
     */
    @GetMapping("/html")
    public ResponseEntity<?> sendHtmlEmail(
            @RequestParam String to,
            @RequestParam(defaultValue = "Email HTML de prueba") String subject) {
        try {
            String htmlContent = """
                <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2 style="color: #FF6B35;">Email de prueba HTML</h2>
                        <p>Este es un <strong>email HTML</strong> de prueba desde FallApp.</p>
                        <div style="background-color: #f0f0f0; padding: 20px; border-radius: 5px;">
                            <p>✅ Configuración correcta</p>
                            <p>📧 Brevo funcionando</p>
                        </div>
                    </body>
                </html>
                """;
            emailService.sendHtmlEmail(to, subject, htmlContent);
            return ResponseEntity.ok(createResponse("Email HTML enviado correctamente a " + to));
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    /**
     * Enviar email de bienvenida de prueba
     * GET http://localhost:8080/api/test-email/welcome?to=destino@ejemplo.com&username=Juan
     */
    @GetMapping("/welcome")
    public ResponseEntity<?> sendWelcomeEmail(
            @RequestParam String to,
            @RequestParam(defaultValue = "Usuario") String username) {
        try {
            emailService.sendWelcomeEmail(to, username);
            return ResponseEntity.ok(createResponse("Email de bienvenida enviado a " + to));
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    /**
     * Enviar email de recuperación de contraseña de prueba
     * GET http://localhost:8080/api/test-email/reset?to=destino@ejemplo.com
     */
    @GetMapping("/reset")
    public ResponseEntity<?> sendPasswordResetEmail(
            @RequestParam String to,
            @RequestParam(defaultValue = "TEST_TOKEN_123456") String token) {
        try {
            emailService.sendPasswordResetEmail(to, token);
            return ResponseEntity.ok(createResponse("Email de recuperación enviado a " + to));
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    /**
     * Enviar email de notificación de comentario de prueba
     * GET http://localhost:8080/api/test-email/comment?to=destino@ejemplo.com
     */
    @GetMapping("/comment")
    public ResponseEntity<?> sendCommentNotificationEmail(
            @RequestParam String to,
            @RequestParam(defaultValue = "Falla Central") String fallaName,
            @RequestParam(defaultValue = "Juan Pérez") String author,
            @RequestParam(defaultValue = "¡Me encanta esta falla!") String comment) {
        try {
            emailService.sendCommentNotificationEmail(to, fallaName, author, comment);
            return ResponseEntity.ok(createResponse("Email de notificación enviado a " + to));
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    /**
     * Enviar email de verificación de cuenta de prueba
     * GET http://localhost:8080/api/test-email/verify?to=destino@ejemplo.com
     */
    @GetMapping("/verify")
    public ResponseEntity<?> sendVerificationEmail(
            @RequestParam String to,
            @RequestParam(defaultValue = "VERIFY_TOKEN_789") String token) {
        try {
            emailService.sendVerificationEmail(to, token);
            return ResponseEntity.ok(createResponse("Email de verificación enviado a " + to));
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    /**
     * Información sobre los endpoints disponibles
     * GET http://localhost:8080/api/test-email/info
     */
    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("message", "Endpoints disponibles para probar emails");
        info.put("endpoints", new String[]{
            "GET /api/test-email/simple?to=EMAIL",
            "GET /api/test-email/html?to=EMAIL",
            "GET /api/test-email/welcome?to=EMAIL&username=NOMBRE",
            "GET /api/test-email/reset?to=EMAIL&token=TOKEN",
            "GET /api/test-email/comment?to=EMAIL&fallaName=FALLA&author=AUTOR&comment=TEXTO",
            "GET /api/test-email/verify?to=EMAIL&token=TOKEN"
        });
        info.put("warning", "⚠️ Estos endpoints son SOLO para desarrollo. Proteger o eliminar en producción.");
        return ResponseEntity.ok(info);
    }

    // Métodos auxiliares para respuestas JSON
    private Map<String, String> createResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }

    private Map<String, String> createError(String error) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", error);
        return response;
    }
}
