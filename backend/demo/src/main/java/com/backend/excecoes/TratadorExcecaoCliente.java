package com.backend.excecoes;

import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.backend.model.service.ServCliente.RespostaClienteInvalida;

@RestControllerAdvice(assignableTypes = com.backend.controller.ControladorCliente.class)
public class TratadorExcecaoCliente {
    @ExceptionHandler(RespostaClienteInvalida.class)
    public ResponseEntity<RespostaErro> tratar(RespostaClienteInvalida e, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new RespostaErro(Instant.now(), 400, "Bad Request", e.getMessage(), request.getRequestURI()));
    }
    public record RespostaErro(Instant timestamp, int status, String error, String message, String path) {}
}
