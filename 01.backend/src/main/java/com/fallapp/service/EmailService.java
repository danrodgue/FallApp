package com.fallapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para envío de emails usando Brevo (SMTP)
 * Configurado en application.properties
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    /**
     * Enviar email simple (solo texto)
     * @param to Email destinatario
     * @param subject Asunto
     * @param text Contenido en texto plano
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            logger.info("Email simple enviado correctamente a: {}", to);
        } catch (Exception e) {
            logger.error("Error al enviar email simple a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    /**
     * Enviar email HTML
     * @param to Email destinatario
     * @param subject Asunto
     * @param htmlContent Contenido HTML
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = es HTML
            
            mailSender.send(message);
            logger.info("Email HTML enviado correctamente a: {}", to);
        } catch (Exception e) {
            logger.error("Error al enviar email HTML a {}: {}", to, e.getMessage());
            throw new MessagingException("Error al enviar email HTML: " + e.getMessage(), e);
        }
    }

    /**
     * Enviar email de bienvenida al registrarse
     * @param to Email del nuevo usuario
     * @param username Nombre de usuario
     */
    public void sendWelcomeEmail(String to, String username) throws MessagingException {
        String subject = "¡Bienvenido a FallApp!";
        String htmlContent = """
            <html>
                <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;">
                        <h2 style="color: #FF6B35;">¡Hola %s!</h2>
                        <p style="font-size: 16px;">Gracias por registrarte en <strong>FallApp</strong>.</p>
                        <p style="font-size: 16px;">Tu plataforma para descubrir y votar las mejores fallas de Valencia.</p>
                        <div style="margin: 30px 0; padding: 20px; background-color: #FFF3E0; border-radius: 5px;">
                            <p style="margin: 0; font-size: 14px;">🎭 Explora fallas</p>
                            <p style="margin: 0; font-size: 14px;">🗳️ Vota tus favoritas</p>
                            <p style="margin: 0; font-size: 14px;">💬 Comenta y participa</p>
                        </div>
                        <p style="font-size: 14px; color: #666;">
                            Estamos encantados de tenerte con nosotros.
                        </p>
                        <br>
                        <p style="font-size: 14px;">
                            Saludos,<br>
                            <strong>El equipo de FallApp</strong>
                        </p>
                    </div>
                </body>
            </html>
            """.formatted(username);
        
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Enviar email de recuperación de contraseña
     * @param to Email del usuario
     * @param resetToken Token de recuperación
     */
    public void sendPasswordResetEmail(String to, String resetToken) throws MessagingException {
        String subject = "Recuperación de contraseña - FallApp";
        // TODO: Cambiar URL según entorno (dev/prod)
        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + resetToken;
        
        String htmlContent = """
            <html>
                <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;">
                        <h2 style="color: #FF6B35;">Recuperación de contraseña</h2>
                        <p style="font-size: 16px;">Has solicitado restablecer tu contraseña en FallApp.</p>
                        <p style="font-size: 16px;">Haz clic en el siguiente botón para continuar:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" 
                               style="background-color: #FF6B35; 
                                      color: white; 
                                      padding: 12px 30px; 
                                      text-decoration: none; 
                                      border-radius: 5px;
                                      display: inline-block;
                                      font-weight: bold;">
                                Restablecer contraseña
                            </a>
                        </div>
                        <p style="font-size: 12px; color: #999;">
                            <small>⚠️ Este enlace expira en 1 hora por seguridad.</small>
                        </p>
                        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;">
                            <p style="font-size: 14px; color: #666;">
                                Si no solicitaste este cambio, ignora este email. 
                                Tu contraseña permanecerá sin cambios.
                            </p>
                        </div>
                    </div>
                </body>
            </html>
            """.formatted(resetLink);
        
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Enviar notificación cuando reciben un comentario
     * @param to Email del propietario de la falla
     * @param fallaName Nombre de la falla
     * @param commentAuthor Autor del comentario
     * @param commentText Texto del comentario
     */
    public void sendCommentNotificationEmail(String to, String fallaName, String commentAuthor, String commentText) throws MessagingException {
        String subject = "Nuevo comentario en tu falla - FallApp";
        String htmlContent = """
            <html>
                <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;">
                        <h2 style="color: #FF6B35;">💬 Nuevo comentario</h2>
                        <p style="font-size: 16px;">
                            <strong>%s</strong> ha comentado en tu falla <strong>%s</strong>
                        </p>
                        <div style="background-color: #f9f9f9; padding: 15px; border-left: 4px solid #FF6B35; margin: 20px 0;">
                            <p style="margin: 0; font-style: italic;">"%s"</p>
                        </div>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="http://localhost:8080" 
                               style="background-color: #FF6B35; 
                                      color: white; 
                                      padding: 10px 20px; 
                                      text-decoration: none; 
                                      border-radius: 5px;
                                      display: inline-block;">
                                Ver comentario
                            </a>
                        </div>
                    </div>
                </body>
            </html>
            """.formatted(commentAuthor, fallaName, commentText);
        
        sendHtmlEmail(to, subject, htmlContent);
    }

}
