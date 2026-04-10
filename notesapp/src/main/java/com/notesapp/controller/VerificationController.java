package com.notesapp.controller;

import com.notesapp.exception.BadRequestException;
import com.notesapp.service.interfaz.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador con @Controller (NO @RestController) para que Thymeleaf
 * pueda renderizar las plantillas HTML de verificación correctamente.
 */
@Controller
@RequestMapping("/api/auth")
public class VerificationController {

    private final AuthService authService;

    public VerificationController(AuthService authService) {
        this.authService = authService;
    }

    // ── GET /api/auth/verify?token=... ───────────────────
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, Model model) {
        try {
            authService.verificarEmail(token);
            return "verify-success"; // Renderiza templates/verify-success.html
        } catch (BadRequestException e) {
            model.addAttribute("error", e.getMessage());
            return "verify-error"; // Renderiza templates/verify-error.html
        }
    }
}
