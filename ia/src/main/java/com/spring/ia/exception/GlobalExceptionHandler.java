package com.spring.ia.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<?> handleRedis(RedisConnectionFailureException ex) {
        return ResponseEntity
                .status(503)
                .body(Map.of(
                        "error", "Servicio no disponible",
                        "message", "El servicio no está disponible por el momento. Intentá de nuevo más tarde."
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity
                .status(400)
                .body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex, HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        log.error("Request failed ({} {}): {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getClass().getSimpleName(), safeLogMessage(ex.getMessage()));
        if (ex.getCause() != null) {
            Throwable cause = ex.getCause();
            log.error("Request failure cause ({}): {}", cause.getClass().getSimpleName(), safeLogMessage(cause.getMessage()));
        }
        if (accept != null && accept.contains("text/event-stream")) {
            return ResponseEntity.internalServerError().build();
        }
        if (ex instanceof ResponseStatusException statusException) {
            String message = statusException.getReason() == null ? "La solicitud no se pudo completar." : statusException.getReason();
            return ResponseEntity.status(statusException.getStatusCode()).body(Map.of(
                    "error", "Solicitud no completada",
                    "message", message
            ));
        }
        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "error", "Error interno",
                        "message", "Ocurrió un problema. Intentá de nuevo más tarde."
                ));
    }

    private String safeLogMessage(String message) {
        if (message == null || message.isBlank()) return "No error message provided";
        String safe = message.replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("(?i)(authorization\\s*[:=]\\s*)(?:[a-z][a-z0-9_-]*\\s+)?[^\\s,;]+", "$1[REDACTED]")
                .replaceAll("(?i)((?:api[-_ ]?key|access[-_ ]?token|refresh[-_ ]?token|password|secret)(\\s*[:=]\\s*))(?:Bearer\\s+)?[^\\s,;]+", "$1[REDACTED]")
                .replaceAll("(?i)(--(?:api[-_ ]?key|access[-_ ]?token|refresh[-_ ]?token|password|secret)(?:=|\\s+))(?:Bearer\\s+)?[^\\s]+", "$1[REDACTED]")
                .replaceAll("(?i)([?&](?:token|api[-_ ]?key|password|secret)=)[^&\\s]+", "$1[REDACTED]")
                .replaceAll("(?i)Bearer\\s+\\S+", "Bearer [REDACTED]");
        return safe.length() > 500 ? safe.substring(0, 500) : safe;
    }
}
