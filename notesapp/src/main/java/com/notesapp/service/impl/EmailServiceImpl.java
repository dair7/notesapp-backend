package com.notesapp.service.impl;

import com.notesapp.service.interfaz.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl; 

    @Override
    public void sendVerificationEmail(String to, String token) {
        String subject = "Notes Pro - Confirma tu cuenta";
        
        // URL que se abrirá al dar clic en el botón (tu endpoint de validación)
        String verifyUrl = appBaseUrl + "/api/auth/verify?token=" + token;

        String htmlMessage = "<!DOCTYPE html>"
            + "<html>"
            + "<head>"
            + "<style>"
            + "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5; margin: 0; padding: 0; }"
            + "  .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }"
            + "  .header { background-color: #2A9D8F; padding: 30px 20px; text-align: center; }"
            + "  .header h1 { color: #ffffff; margin: 0; font-size: 28px; }"
            + "  .content { padding: 40px 30px; text-align: center; color: #334155; }"
            + "  .content h2 { color: #1e293b; font-size: 22px; margin-top: 0; }"
            + "  .content p { font-size: 16px; line-height: 1.5; margin-bottom: 30px; }"
            + "  .button { display: inline-block; padding: 14px 32px; background-color: #2A9D8F; color: #ffffff !important; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px; }"
            + "  .footer { padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; background-color: #f8fafc; border-top: 1px solid #e2e8f0; }"
            + "</style>"
            + "</head>"
            + "<body>"
            + "  <div class='container'>"
            + "    <div class='header'><h1>Notes Pro</h1></div>"
            + "    <div class='content'>"
            + "      <h2>¡Bienvenido a tu nuevo espacio!</h2>"
            + "      <p>Estamos muy felices de que te unas a nosotros. Solo falta un peque&ntilde;o paso, por favor verifica tu correo electr&oacute;nico haciendo clic en el bot&oacute;n de abajo para activar tu cuenta.</p>"
            + "      <a href='" + verifyUrl + "' class='button'>Verificar mi cuenta</a>"
            + "      <p style='margin-top: 30px; font-size: 14px; color: #64748b;'>Si no has creado una cuenta en Notes Pro, puedes ignorar este correo.</p>"
            + "    </div>"
            + "    <div class='footer'>&copy; 2026 Notes Pro. Todos los derechos reservados.</div>"
            + "  </div>"
            + "</body>"
            + "</html>";

        sendHtmlEmail(to, subject, htmlMessage);
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Recuperación de Contraseña - Notes Pro";
        String message = "Has solicitado restablecer tu contraseña. Usa el siguiente código temporal en la aplicación: \n\n" 
                + token + "\n\nEste código expira en 1 hora. Si no solicitaste esto, ignora este correo.";
        sendEmail(to, subject, message);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates html
            
            mailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Error al enviar el correo HTML: " + e.getMessage(), e);
        }
    }
}
