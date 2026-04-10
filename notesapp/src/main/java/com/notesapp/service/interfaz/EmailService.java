package com.notesapp.service.interfaz;

import java.time.LocalDateTime;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);
    void sendWelcomeEmail(String to, String nombre);
    void sendReminderEmail(String to, String nombreUsuario, String tituloNota, LocalDateTime fecha);
    void sendEmail(String to, String subject, String body);
    void sendHtmlEmail(String to, String subject, String htmlBody);
}
