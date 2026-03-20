package com.notesapp.controller;

import com.notesapp.entity.Usuario;
import com.notesapp.entity.VerificationToken;
import com.notesapp.exception.BadRequestException;
import com.notesapp.repository.UsuarioRepository;
import com.notesapp.repository.VerificationTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

/**
 * Controlador con @Controller (NO @RestController) para que Thymeleaf
 * pueda renderizar las plantillas HTML de verificación correctamente.
 */
@Controller
@RequestMapping("/api/auth")
public class VerificationController {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UsuarioRepository usuarioRepository;

    public VerificationController(VerificationTokenRepository verificationTokenRepository,
                                  UsuarioRepository usuarioRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ── GET /api/auth/verify?token=... ───────────────────
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, Model model) {
        try {
            VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                    .orElseThrow(() -> new BadRequestException(
                            "El enlace de verificación es inválido o el código ya fue usado."));

            if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new BadRequestException(
                        "El enlace de verificación ha expirado. Por favor solicita uno nuevo.");
            }

            Usuario usuario = verificationToken.getUsuario();
            usuario.setVerified(true);
            usuarioRepository.save(usuario);

            // Borrar el token una vez usado
            verificationTokenRepository.delete(verificationToken);

            return "verify-success"; // Renderiza templates/verify-success.html
        } catch (BadRequestException e) {
            model.addAttribute("error", e.getMessage());
            return "verify-error"; // Renderiza templates/verify-error.html
        }
    }
}
