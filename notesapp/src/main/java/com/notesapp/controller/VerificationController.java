package com.notesapp.controller;

import com.notesapp.exception.BadRequestException;
import com.notesapp.service.interfaz.AuthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class VerificationController {

    private final AuthService authService;

    public VerificationController(AuthService authService) {
        this.authService = authService;
    }

    // ── GET /api/auth/verify?token=... ───────────────────
    @GetMapping(value = "/verify", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            authService.verificarEmail(token);
            return ResponseEntity.ok(htmlExito());
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(htmlError(e.getMessage()));
        }
    }

    // ── HTML de éxito ────────────────────────────────────
    private String htmlExito() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
            + "<style>"
            + "  body { font-family: 'Segoe UI', sans-serif; background:#f4f4f5; display:flex; justify-content:center; align-items:center; height:100vh; margin:0; }"
            + "  .card { background:#fff; border-radius:16px; padding:48px 40px; text-align:center; box-shadow:0 4px 20px rgba(0,0,0,0.08); max-width:420px; }"
            + "  .icon { font-size:56px; margin-bottom:16px; }"
            + "  h1 { color:#1e293b; font-size:24px; margin:0 0 12px; }"
            + "  p { color:#64748b; font-size:15px; line-height:1.5; }"
            + "  .badge { display:inline-block; background:#dcfce7; color:#16a34a; border-radius:8px; padding:6px 16px; font-weight:600; margin-bottom:20px; }"
            + "</style></head><body>"
            + "<div class='card'>"
            + "  <div class='icon'>&#9989;</div>"
            + "  <span class='badge'>Verificado</span>"
            + "  <h1>&#161;Cuenta activada!</h1>"
            + "  <p>Tu correo ha sido verificado exitosamente. Ya puedes iniciar sesi&oacute;n en <strong>Notes Pro</strong>.</p>"
            + "</div>"
            + "</body></html>";
    }

    // ── HTML de error ─────────────────────────────────────
    private String htmlError(String mensaje) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
            + "<style>"
            + "  body { font-family: 'Segoe UI', sans-serif; background:#f4f4f5; display:flex; justify-content:center; align-items:center; height:100vh; margin:0; }"
            + "  .card { background:#fff; border-radius:16px; padding:48px 40px; text-align:center; box-shadow:0 4px 20px rgba(0,0,0,0.08); max-width:420px; }"
            + "  .icon { font-size:56px; margin-bottom:16px; }"
            + "  h1 { color:#1e293b; font-size:24px; margin:0 0 12px; }"
            + "  p { color:#64748b; font-size:15px; line-height:1.5; }"
            + "  .badge { display:inline-block; background:#fee2e2; color:#dc2626; border-radius:8px; padding:6px 16px; font-weight:600; margin-bottom:20px; }"
            + "</style></head><body>"
            + "<div class='card'>"
            + "  <div class='icon'>&#10060;</div>"
            + "  <span class='badge'>Error</span>"
            + "  <h1>Verificaci&oacute;n fallida</h1>"
            + "  <p>" + mensaje + "</p>"
            + "</div>"
            + "</body></html>";
    }
}
