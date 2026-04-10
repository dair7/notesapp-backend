package com.notesapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Filtro de rate limiting respaldado por Redis.
 * Los contadores persisten entre reinicios del servidor y se comparten
 * entre múltiples instancias (escalado horizontal).
 *
 * Estrategia: ventana fija de WINDOW_MINUTES minutos por IP + endpoint.
 * El TTL de la clave Redis actúa como reset automático de la ventana.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MINUTES = 15;

    // Prefijo para las claves en Redis
    private static final String KEY_PREFIX = "rate_limit:";

    // Endpoints protegidos contra fuerza bruta
    private static final String[] PROTECTED_PATHS = {
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password"
    };

    private final StringRedisTemplate redisTemplate;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if ("POST".equalsIgnoreCase(request.getMethod()) && isProtectedPath(path)) {
            try {
                String clientIp = getClientIp(request);
                String redisKey = KEY_PREFIX + clientIp + ":" + path;

                // Incrementar el contador; si es la primera vez, establecer TTL
                Long intentos = redisTemplate.opsForValue().increment(redisKey);

                if (intentos != null && intentos == 1) {
                    // Primera petición de esta ventana: fijar expiración de 15 minutos
                    redisTemplate.expire(redisKey, WINDOW_MINUTES, TimeUnit.MINUTES);
                }

                if (intentos != null && intentos > MAX_REQUESTS) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"status\":429,\"error\":\"Too Many Requests\","
                                    + "\"mensaje\":\"Demasiados intentos. Espera 15 minutos antes de intentar de nuevo.\"}");
                    return;
                }
            } catch (Exception e) {
                // Si Redis no está disponible, se permite la petición (fail-open)
                logger.warn("Redis no disponible, omitiendo rate limiting: " + e.getMessage());
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
}
