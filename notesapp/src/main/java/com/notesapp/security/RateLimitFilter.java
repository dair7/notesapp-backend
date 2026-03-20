package com.notesapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtro simple de rate limiting para proteger endpoints sensibles
 * como login y forgot-password contra ataques de fuerza bruta.
 * 
 * Limita a MAX_REQUESTS intentos por IP en una ventana de WINDOW_MS milisegundos.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Máximo de intentos por ventana de tiempo
    private static final int MAX_REQUESTS = 10;
    // Ventana de tiempo en milisegundos (15 minutos)
    private static final long WINDOW_MS = 15 * 60 * 1000;

    // Almacén de intentos por IP
    private final Map<String, RateLimitEntry> attempts = new ConcurrentHashMap<>();

    // Endpoints protegidos
    private static final String[] PROTECTED_PATHS = {
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Solo aplicar rate limiting a endpoints sensibles y método POST
        if ("POST".equalsIgnoreCase(request.getMethod()) && isProtectedPath(path)) {
            String clientIp = getClientIp(request);
            String key = clientIp + ":" + path;

            RateLimitEntry entry = attempts.compute(key, (k, existing) -> {
                long now = System.currentTimeMillis();
                if (existing == null || (now - existing.windowStart) > WINDOW_MS) {
                    return new RateLimitEntry(now); // Nueva ventana
                }
                existing.count.incrementAndGet();
                return existing;
            });

            if (entry.count.get() > MAX_REQUESTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"status\":429,\"error\":\"Too Many Requests\","
                                + "\"mensaje\":\"Demasiados intentos. Espera 15 minutos antes de intentar de nuevo.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path) {
        for (String p : PROTECTED_PATHS) {
            if (path.equals(p)) return true;
        }
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Estructura interna para rastrear intentos
    private static class RateLimitEntry {
        final long windowStart;
        final AtomicInteger count;

        RateLimitEntry(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }
}
