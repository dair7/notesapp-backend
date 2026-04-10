package com.notesapp.service.impl;

import com.notesapp.service.interfaz.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.from:nubeart73@gmail.com}")
    private String fromEmail;

    @Value("${brevo.from-name:Notes Pro}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public EmailServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    // ── Métodos públicos ────────────────────────────────────────────────────────

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verifyUrl = appBaseUrl + "/api/auth/verify?token=" + token;

        String html = "<!DOCTYPE html>"
            + "<html><head><style>"
            + "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5; margin: 0; padding: 0; }"
            + "  .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }"
            + "  .header { background-color: #2A9D8F; padding: 30px 20px; text-align: center; }"
            + "  .header h1 { color: #ffffff; margin: 0; font-size: 28px; }"
            + "  .content { padding: 40px 30px; text-align: center; color: #334155; }"
            + "  .content h2 { color: #1e293b; font-size: 22px; margin-top: 0; }"
            + "  .content p { font-size: 16px; line-height: 1.5; margin-bottom: 30px; }"
            + "  .button { display: inline-block; padding: 14px 32px; background-color: #2A9D8F; color: #ffffff !important; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px; }"
            + "  .footer { padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; background-color: #f8fafc; border-top: 1px solid #e2e8f0; }"
            + "</style></head><body>"
            + "  <div class='container'>"
            + "    <div class='header'><h1>Notes Pro</h1></div>"
            + "    <div class='content'>"
            + "      <h2>&#161;Bienvenido a tu nuevo espacio!</h2>"
            + "      <p>Estamos muy felices de que te unas a nosotros. Solo falta un peque&ntilde;o paso, por favor verifica tu correo electr&oacute;nico haciendo clic en el bot&oacute;n de abajo para activar tu cuenta.</p>"
            + "      <a href='" + verifyUrl + "' class='button'>Verificar mi cuenta</a>"
            + "      <p style='margin-top: 30px; font-size: 14px; color: #64748b;'>Si no has creado una cuenta en Notes Pro, puedes ignorar este correo.</p>"
            + "    </div>"
            + "    <div class='footer'>&copy; 2026 Notes Pro. Todos los derechos reservados.</div>"
            + "  </div>"
            + "</body></html>";

        sendHtmlEmail(to, "Notes Pro - Confirma tu cuenta", html);
    }

    @Override
    public void sendWelcomeEmail(String to, String nombre) {
        String html = "<!DOCTYPE html>"
            + "<html><head><style>"
            + "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5; margin: 0; padding: 0; }"
            + "  .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }"
            + "  .header { background-color: #2A9D8F; padding: 30px 20px; text-align: center; }"
            + "  .header h1 { color: #ffffff; margin: 0; font-size: 28px; }"
            + "  .content { padding: 40px 30px; text-align: center; color: #334155; }"
            + "  .content h2 { color: #1e293b; font-size: 22px; margin-top: 0; }"
            + "  .content p { font-size: 16px; line-height: 1.5; margin-bottom: 20px; }"
            + "  .emoji { font-size: 48px; margin-bottom: 16px; display: block; }"
            + "  .highlight { color: #2A9D8F; font-weight: bold; }"
            + "  .features { background-color: #f8fafc; border-radius: 10px; padding: 20px 30px; margin: 20px 0; text-align: left; }"
            + "  .features li { font-size: 15px; color: #334155; margin-bottom: 10px; list-style: none; padding-left: 4px; }"
            + "  .footer { padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; background-color: #f8fafc; border-top: 1px solid #e2e8f0; }"
            + "</style></head><body>"
            + "  <div class='container'>"
            + "    <div class='header'><h1>Notes Pro</h1></div>"
            + "    <div class='content'>"
            + "      <span class='emoji'>&#127881;</span>"
            + "      <h2>&#161;Bienvenido, <span class='highlight'>" + nombre + "</span>!</h2>"
            + "      <p>Tu cuenta ha sido activada exitosamente. Ahora formas parte de <strong>Notes Pro</strong>, tu espacio personal para organizar ideas, notas y recordatorios.</p>"
            + "      <div class='features'><ul>"
            + "        <li>&#128221; Crea y organiza tus notas f&aacute;cilmente</li>"
            + "        <li>&#9200; Configura recordatorios para no olvidar nada</li>"
            + "        <li>&#128274; Tus datos siempre seguros y privados</li>"
            + "        <li>&#9729; Accede desde cualquier lugar</li>"
            + "      </ul></div>"
            + "      <p style='font-size: 14px; color: #64748b;'>&#161;Esperamos que disfrutes la experiencia!</p>"
            + "    </div>"
            + "    <div class='footer'>&copy; 2026 Notes Pro. Todos los derechos reservados.</div>"
            + "  </div>"
            + "</body></html>";

        sendHtmlEmail(to, "¡Bienvenido a Notes Pro!", html);
    }

    @Override
    public void sendCredencialesEmail(String to, String nombre, String password) {
        String html = "<!DOCTYPE html>"
            + "<html><head><style>"
            + "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5; margin: 0; padding: 0; }"
            + "  .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }"
            + "  .header { background-color: #2A9D8F; padding: 30px 20px; text-align: center; }"
            + "  .header h1 { color: #ffffff; margin: 0; font-size: 28px; }"
            + "  .content { padding: 40px 30px; color: #334155; }"
            + "  .content h2 { color: #1e293b; font-size: 22px; margin-top: 0; }"
            + "  .credenciales { background-color: #f0fdf9; border-left: 4px solid #2A9D8F; border-radius: 8px; padding: 20px 24px; margin: 24px 0; }"
            + "  .credenciales p { margin: 8px 0; font-size: 15px; color: #334155; }"
            + "  .credenciales strong { color: #1e293b; }"
            + "  .aviso { font-size: 13px; color: #64748b; margin-top: 24px; }"
            + "  .footer { padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; background-color: #f8fafc; border-top: 1px solid #e2e8f0; }"
            + "</style></head><body>"
            + "  <div class='container'>"
            + "    <div class='header'><h1>Notes Pro</h1></div>"
            + "    <div class='content'>"
            + "      <h2>&#128075; Hola, " + nombre + "</h2>"
            + "      <p>Un administrador ha creado una cuenta para ti en <strong>Notes Pro</strong>. Estas son tus credenciales de acceso:</p>"
            + "      <div class='credenciales'>"
            + "        <p><strong>&#128231; Correo:</strong> " + to + "</p>"
            + "        <p><strong>&#128274; Contrase&ntilde;a:</strong> " + password + "</p>"
            + "      </div>"
            + "      <p class='aviso'>Por seguridad, te recomendamos cambiar tu contrase&ntilde;a despu&eacute;s de tu primer inicio de sesi&oacute;n.</p>"
            + "    </div>"
            + "    <div class='footer'>&copy; 2026 Notes Pro. Todos los derechos reservados.</div>"
            + "  </div>"
            + "</body></html>";

        sendHtmlEmail(to, "Notes Pro - Tu cuenta ha sido creada", html);
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String html = "<!DOCTYPE html>"
            + "<html><head><style>"
            + "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5; margin: 0; padding: 0; }"
            + "  .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }"
            + "  .header { background-color: #2A9D8F; padding: 30px 20px; text-align: center; }"
            + "  .header h1 { color: #ffffff; margin: 0; font-size: 28px; }"
            + "  .content { padding: 40px 30px; text-align: center; color: #334155; }"
            + "  .content h2 { color: #1e293b; margin-top: 0; }"
            + "  .code { background-color: #f0fdf9; border: 2px dashed #2A9D8F; border-radius: 8px; padding: 20px; margin: 24px auto; max-width: 200px; font-size: 28px; font-weight: bold; color: #1e293b; letter-spacing: 4px; }"
            + "  .footer { padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; background-color: #f8fafc; border-top: 1px solid #e2e8f0; }"
            + "</style></head><body>"
            + "  <div class='container'>"
            + "    <div class='header'><h1>Notes Pro</h1></div>"
            + "    <div class='content'>"
            + "      <h2>&#128274; Recuperaci&oacute;n de Contrase&ntilde;a</h2>"
            + "      <p>Has solicitado restablecer tu contrase&ntilde;a. Usa el siguiente c&oacute;digo en la aplicaci&oacute;n:</p>"
            + "      <div class='code'>" + token + "</div>"
            + "      <p style='font-size: 14px; color: #64748b;'>Este c&oacute;digo expira en 1 hora. Si no solicitaste esto, ignora este correo.</p>"
            + "    </div>"
            + "    <div class='footer'>&copy; 2026 Notes Pro. Todos los derechos reservados.</div>"
            + "  </div>"
            + "</body></html>";

        sendHtmlEmail(to, "Recuperación de Contraseña - Notes Pro", html);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        // Convierte texto plano a HTML simple para usar un solo canal de envío
        String html = "<pre style='font-family: sans-serif;'>" + body + "</pre>";
        sendHtmlEmail(to, subject, html);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> payload = Map.of(
            "sender", Map.of("name", fromName, "email", fromEmail),
            "to", List.of(Map.of("email", to)),
            "subject", subject,
            "htmlContent", htmlBody
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Brevo respondió con error {}: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error al enviar email via Brevo: " + response.getBody());
            }
            log.info("Email enviado correctamente a {} via Brevo", to);
        } catch (Exception e) {
            log.error("Fallo al enviar email a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage(), e);
        }
    }
}
