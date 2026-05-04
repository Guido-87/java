package com.spring.ia.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<?> handleRedis(RedisConnectionFailureException ex) {
        return ResponseEntity
                .status(503)
                .body(Map.of(
                        "error", "Servicio no disponible",
                        "message", "El sistema está teniendo problemas temporales"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "error", "Error interno",
                        "message", "Algo salió mal, intentá de nuevo más tarde"
                ));
    }
}